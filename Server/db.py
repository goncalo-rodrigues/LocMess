import MySQLdb
import Crypto.Random.random
from json_creator import *


class Database:
    def __init__(self, in_user='locmess_account', in_passwd='FDvlalaland129&&', in_host='localhost', in_db='cmu_locmess'):
        self.conn = MySQLdb.connect(user=in_user, passwd=in_passwd, host=in_host, db=in_db)

    def __insert(self, cursor, tb, cols, vals):
        query = "INSERT INTO " + tb + "(" + cols[0]
        val_refs = "%s"

        rem_cols = cols[1:]
        for col in rem_cols:
            query += "," + col
            val_refs += ",%s"

        query += ") VALUES (" + val_refs + ")"
        cursor.execute(query, vals)

    # The wh_left must contain the %s, in order to use this with LIKE
    def __select(self, cursor, col, tb, wh_left, wh_right):
        query = "SELECT " + col + " FROM " + tb[0]

        rem_tbs = tb[1:]
        for t in rem_tbs:
            query += "," + t

        query += " WHERE"

        for el in wh_left:
            query += " " + el

        cursor.execute(query, wh_right)

    def __delete(self, cursor, tb, wh_left, wh_right):
        query = "DELETE FROM " + tb + " WHERE"

        for el in wh_left:
            query += " " + el

        cursor.execute(query, wh_right)

    def __create_random_str(self, length):
        class_random = Crypto.Random.random.StrongRandom()
        lst_rand = [chr(class_random.getrandbits(7)) for _ in range(length)]
        return "".join(lst_rand)

    def __create_session(self, cursor, username):
        SESSION_ID_SIZE = 128
        error = ""
        id = ""

        while error is not None:
            error = None
            id = self.__create_random_str(SESSION_ID_SIZE)
            try:
                self.__insert(cursor, "Sessions", ("SessionID", "Username"), [id, username])
            except MySQLdb.Error, e:
                error = e

        return id

    """-------------------------------------------------------------------------
        From this point on, the functions correspond to each available service
       -------------------------------------------------------------------------"""

    def signup(self, username, password):
        cursor = self.conn.cursor()
        try:
            self.__insert(cursor, "Users", ("Username", "Password"), [username, password])
        except MySQLdb.Error:
            cursor.close()
            return create_error_json(error_username_exists)

        id = self.__create_session(cursor, username)
        cursor.close()
        self.conn.commit()
        return create_json(["session_id"], [id])

    def login(self, username, password):
        cursor = self.conn.cursor()

        self.__select(cursor, "Password", ["Users"], ["Username = %s"], [username])
        if cursor.rowcount == 0:
            cursor.close()
            return create_error_json(error_username_doesnt_exist)

        q_res = cursor.fetchone()
        if q_res == password:
            cursor.close()
            return create_error_json(error_password_wrong)

        # The user is authenticated
        id = self.__create_session(cursor, username)
        cursor.close()
        self.conn.commit()
        return create_json(["session_id", "filters"], [id, self.__get_login_filters(id)])

    def __get_login_filters(self, session_id):
        cursor = self.conn.cursor()

        self.__select(cursor, "Username", ["Sessions"], ["SessionID = %s"], [session_id])
        if cursor.rowcount == 0:
            cursor.close()
            return create_error_json(error_session_not_found)

        user = cursor.fetchone()

        self.__select(cursor, "F.FilterKey, F.FilterValue", ["Filters AS F", "UserFilters AS UF"],
                      ["F.FilterID = UF.FilterID AND UF.Username = %s"], [user])

        result = []
        q_res = cursor.fetchall()
        for row in q_res:
            key = row[0]
            val = row[1]
            result.append({"key": key, "value": val})

        return result

    def logout(self, session_id):
        cursor = self.conn.cursor()

        self.__select(cursor, "*", ["Sessions"], ["SessionID = %s"], [session_id])
        if cursor.rowcount == 0:
            cursor.close()
            return create_error_json(error_session_not_found)

        self.__delete(cursor, "Sessions", ["SessionID = %s"], [session_id])
        cursor.close()
        self.conn.commit()
        return create_json(["resp"], ["ok"])

    def request_locations(self, session_id, startswith):
        cursor = self.conn.cursor()

        self.__select(cursor, "*", ["Sessions"], ["SessionID = %s"], [session_id])
        if cursor.rowcount == 0:
            cursor.close()
            return create_error_json(error_session_not_found)

        self.__select(cursor, "Name", ["Locations"], ["Name LIKE %s"], ["%" + startswith + "%"])
        all_locs = []
        if(cursor.rowcount != 0):
            all_locs = [item[0] for item in cursor.fetchall()]

        cursor.close()
        self.conn.commit()
        return create_json(["locations"], [all_locs])

    def create_gps_location(self, session_id, name, gps):
        cursor = self.conn.cursor()

        self.__select(cursor, "*", ["Sessions"], ["SessionID = %s"], [session_id])
        if cursor.rowcount == 0:
            cursor.close()
            return create_error_json(error_session_not_found)

        try:
            self.__insert(cursor, "Locations", ["Name"], [name])
            self.__insert(cursor, "GPS", ["Location", "Latitude", "Longitude", "Radius"],
                          [name, gps["lat"], gps["long"], gps["radius"]])
        except MySQLdb.Error:
            cursor.close()
            return create_error_json(error_location_exists)

        cursor.close()
        self.conn.commit()
        return create_json(["resp"], ["ok"])

    def create_ssids_location(self, session_id, name, ssids):
        cursor = self.conn.cursor()

        self.__select(cursor, "*", ["Sessions"], ["SessionID = %s"], [session_id])
        if cursor.rowcount == 0:
            cursor.close()
            return create_error_json(error_session_not_found)

        try:
            self.__insert(cursor, "Locations", ["Name"], [name])
            for el in ssids:
                self.__insert(cursor, "WifiIDs", ["Location", "WifiID"], [name, el])
        except MySQLdb.Error:
            cursor.close()
            return create_error_json(error_location_exists)

        cursor.close()
        self.conn.commit()
        return create_json(["resp"], ["ok"])

    def remove_location(self, session_id, name):
        cursor = self.conn.cursor()

        self.__select(cursor, "*", ["Sessions"], ["SessionID = %s"], [session_id])
        if cursor.rowcount == 0:
            cursor.close()
            return create_error_json(error_session_not_found)

        self.__select(cursor, "*", ["Locations"], ["Name = %s"], [name])
        if cursor.rowcount == 0:
            cursor.close()
            return create_error_json(error_location_not_found)

        self.__delete(cursor, "Locations", ["Name = %s"], [name])
        cursor.close()
        self.conn.commit()
        return create_json(["resp"], ["ok"])

    def set_my_filter(self, session_id, filter):
        cursor = self.conn.cursor()
        key = filter["key"]
        val = filter["value"]

        self.__select(cursor, "Username", ["Sessions"], ["SessionID = %s"], [session_id])
        if cursor.rowcount == 0:
            cursor.close()
            return create_error_json(error_session_not_found)

        user = cursor.fetchone()

        self.__select(cursor, "FilterID", ["Filters"], ["FilterKey = %s AND FilterValue = %s"], [key, val])

        # There is no filter with this specification
        if cursor.rowcount == 0:
            try:
                self.__insert(cursor, "Filters", ["FilterKey", "FilterValue"], [key, val])
            except MySQLdb.Error:
                print "Ignoring error when creating filter..."

            self.__select(cursor, "FilterID", ["Filters"], ["FilterKey = %s AND FilterValue = %s"], [key, val])

        filter_id = cursor.fetchone()

        try:
            self.__insert(cursor, "UserFilters", ["Username", "FilterID"], [user, filter_id])
        except MySQLdb.Error:
            cursor.close()
            return create_error_json(error_cannot_assign_filter)

        cursor.close()
        self.conn.commit()
        return create_json(["resp"], ["ok"])

    def get_keys(self, session_id):
        cursor = self.conn.cursor()

        self.__select(cursor, "Username", ["Sessions"], ["SessionID = %s"], [session_id])
        if cursor.rowcount == 0:
            cursor.close()
            return create_error_json(error_session_not_found)

        self.__select(cursor, "DISTINCT FilterKey", ["Filters"], ["1 = %s"], ["1"])

        result = []
        q_res = cursor.fetchall()
        for row in q_res:
            result.append(row[0])

        return create_json(["keys"], [result])

    def remove_filter(self, session_id, filter):
        cursor = self.conn.cursor()
        key = filter["key"]
        val = filter["value"]

        self.__select(cursor, "Username", ["Sessions"], ["SessionID = %s"], [session_id])
        if cursor.rowcount == 0:
            cursor.close()
            return create_error_json(error_session_not_found)

        user = cursor.fetchone()

        self.__select(cursor, "FilterID", ["Filters"], ["FilterKey = %s AND FilterValue = %s"], [key, val])
        if cursor.rowcount == 0:
            cursor.close()
            return create_error_json(error_filter_not_found)

        filter_id = cursor.fetchone()

        self.__delete(cursor, "UserFilters", ["Username = %s AND FilterID = %s"], [user, filter_id])
        cursor.close()
        self.conn.commit()
        return create_json(["resp"], ["ok"])

    def post_message(self, session_id, msg):
        cursor = self.conn.cursor()
        filter_ids = []

        # Extracting the fields from the message
        msg_id = msg["id"]
        msg_user = msg["username"]
        msg_loc = msg["location"]
        msg_start = msg["start_date"]
        msg_end = msg["end_date"]
        msg_content = msg["content"]
        msg_filters = msg["filters"]

        self.__select(cursor, "*", ["Sessions"], ["SessionID = %s AND Username = %s"], [session_id, msg_user])
        if cursor.rowcount == 0:
            cursor.close()
            return create_error_json(error_session_not_found)

        self.__select(cursor, "*", ["Locations"], ["Name = %s"], [msg_loc])
        if cursor.rowcount == 0:
            cursor.close()
            return create_error_json(error_location_not_found)

        self.__select(cursor, "*", ["Messages"], ["MessageID = %s"], [msg_id])
        if cursor.rowcount != 0:
            cursor.close()
            return create_error_json(error_duplicate_msg)

        for filter in msg_filters:
            self.__select(cursor, "FilterID", ["Filters"], ["FilterKey = %s AND FilterValue = %s"], [filter["key"], filter["value"]])
            if cursor.rowcount == 0:
                cursor.close()
                return create_error_json(error_filter_not_found)

            # JSON supports the boolean type
            filter_ids.append((cursor.fetchone(), 1 if filter["is_whitelist"] else 0))

        try:
            self.__insert(cursor, "Messages",
                          ["MessageID", "Username", "Location", "StartDate", "EndDate", "Content"],
                          [msg_id, msg_user, msg_loc, msg_start, msg_end, msg_content])

            for el in filter_ids:
                self.__insert(cursor, "MessageFilters", ["MessageID", "FilterID", "Whitelist"],
                              [msg_id, el[0], el[1]])

        except MySQLdb.Error:
            cursor.close()
            return create_error_json(error_storing_msg)

        cursor.close()
        self.conn.commit()
        return create_json(["resp"], ["ok"])

    def delete_msg(self, session_id, msg_id):
        cursor = self.conn.cursor()

        self.__select(cursor, "Username", ["Sessions"], ["SessionID = %s"], [session_id])
        if cursor.rowcount == 0:
            cursor.close()
            return create_error_json(error_session_not_found)

        username = cursor.fetchone()

        self.__select(cursor, "*", ["Messages"], ["MessageID = %s AND Username = %s"], [msg_id, username])
        if cursor.rowcount == 0:
            cursor.close()
            return create_error_json(error_msg_not_found)

        self.__delete(cursor, "Messages", ["MessageID = %s"], [msg_id])
        cursor.close()
        self.conn.commit()
        return create_json(["resp"], ["ok"])

    def close(self):
        self.conn.close()
