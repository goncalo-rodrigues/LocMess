import MySQLdb
import time
import Crypto.Random.random
import base64
import array

from json_creator import *
from Crypto.Signature import PKCS1_v1_5
from Crypto.Hash import SHA256


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
        lst_rand = []

        i = 0
        while i < length:
            try:
                lst_rand.append(chr(class_random.getrandbits(7)).encode("utf8"))
                i += 1
            except UnicodeDecodeError:
                print "Invalid char for session_id!"

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
        return create_json(["session_id", "timestamp"], [id, int(time.time())])

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
        return create_json(["session_id", "filters", "messages", "timestamp"],
                           [id, self.__get_login_filters(id), self.__get_created_messages(id), int(time.time())])

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

        cursor.close()
        return result

    def __get_created_messages(self, session_id):
        cursor = self.conn.cursor()

        self.__select(cursor, "Username", ["Sessions"], ["SessionID = %s"], [session_id])
        if cursor.rowcount == 0:
            cursor.close()
            return create_error_json(error_session_not_found)

        user = cursor.fetchone()

        self.__select(cursor, "M.MessageID, M.Username, M.Location, M.StartDate, M.EndDate, M.Content",
                      ["Messages AS M"], ["M.Username = %s"], [user])
        result = []
        q_res = cursor.fetchall()
        for row in q_res:
            result.append(create_msg_dict(row[0], row[1], row[2], row[3], row[4], row[5], []))
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

    def get_location_info(self, session_id, location):
        cursor = self.conn.cursor()

        gps = {}
        ssids = []

        self.__select(cursor, "*", ["Sessions"], ["SessionID = %s"], [session_id])
        if cursor.rowcount == 0:
            cursor.close()
            return create_error_json(error_session_not_found)

        self.__select(cursor, "*", ["Locations"], ["Name = %s"], [location])
        if cursor.rowcount == 0:
            cursor.close()
            return create_error_json(error_location_not_found)

        self.__select(cursor, "Latitude, Longitude, Radius", ["GPS"], ["Location = %s"], [location])
        if cursor.rowcount != 0:
            row = cursor.fetchone()
            gps["lat"] = row[0]
            gps["long"] = row[1]
            gps["radius"] = row[2]

        self.__select(cursor, "DISTINCT WifiID", ["WifiIDs"], ["Location = %s"], [location])
        if cursor.rowcount != 0:
            q_res = cursor.fetchall()
            for row in q_res:
                ssids.append(row[0])

        cursor.close()
        self.conn.commit()
        return create_json(["gps", "ssids"], [gps, ssids])

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

        cursor.close()
        return create_json(["keys"], [result])

    def get_values_key(self, session_id, key):
        cursor = self.conn.cursor()

        self.__select(cursor, "Username", ["Sessions"], ["SessionID = %s"], [session_id])
        if cursor.rowcount == 0:
            cursor.close()
            return create_error_json(error_session_not_found)

        self.__select(cursor, "FilterValue", ["Filters"], ["FilterKey = %s"], [key])

        result = []
        q_res = cursor.fetchall()
        for row in q_res:
            result.append(row[0])

        cursor.close()
        return create_json(["values"], [result])

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
                self.set_my_filter(session_id, filter)
                self.remove_filter(session_id, filter)
                self.__select(cursor, "FilterID", ["Filters"], ["FilterKey = %s AND FilterValue = %s"],
                              [filter["key"], filter["value"]])

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

    def __b64encoded_sig(self, msg, priv_key):
        signer = PKCS1_v1_5.new(priv_key)
        digest = SHA256.new()
        str_msg = msg_to_str(msg)
        msg = bytearray()
        msg.extend(map(ord, str_msg))

        digest.update(msg)
        sign = signer.sign(digest)

        return base64.standard_b64encode(sign)

    def sign_message(self, session_id, msg, priv_key):
        cursor = self.conn.cursor()

        self.__select(cursor, "Username", ["Sessions"], ["SessionID = %s"], [session_id])
        if cursor.rowcount == 0:
            cursor.close()
            return create_error_json(error_session_not_found)

        user = cursor.fetchone()[0]
        cursor.close()

        if msg["username"] != user:
            return create_error_json(error_not_matching_users)

        return create_json(["signed_msg"], [self.__b64encoded_sig(msg, priv_key)])

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

    """-------------------------------------------------------------------------
        Send location functions
    -------------------------------------------------------------------------"""

    def __get_msgid_gps(self, wh_query, lst_query):
        cursor = self.conn.cursor()

        self.__select(cursor, "M.MessageID, M.Username, M.Location, M.StartDate, M.EndDate, M.Content",
                      ["GPS AS G", "Messages AS M"], [wh_query], lst_query)

        if cursor.rowcount == 0:
            cursor.close()
            return None

        result = []
        q_res = cursor.fetchall()
        for row in q_res:
            self.__select(cursor, "F.FilterKey, F.FilterValue, MF.Whitelist",
                          ["Filters AS F", "MessageFilters AS MF"],
                          ["F.FilterID = MF.FilterID AND MF.MessageID = %s"], [row[0]])

            q_filters = cursor.fetchall()
            filters = []
            for row2 in q_filters:
                filter = {}
                filter["key"] = row2[0]
                filter["value"] = row2[1]
                filter["is_whitelist"] = False if row2[2] == "\x00" else True
                filters.append(filter)

            result.append(create_msg_dict(row[0], row[1], row[2], row[3], row[4], row[5], filters))

        cursor.close()
        return result

    def __get_msgid_ssid(self, wh_query, lst_query):
        cursor = self.conn.cursor()

        self.__select(cursor, "M.MessageID, M.Username, M.Location, M.StartDate, M.EndDate, M.Content",
                      ["WifiIDs AS W", "Messages AS M"], [wh_query], lst_query)

        if cursor.rowcount == 0:
            cursor.close()
            return None

        result = []
        q_res = cursor.fetchall()
        for row in q_res:
            self.__select(cursor, "F.FilterKey, F.FilterValue, MF.Whitelist",
                          ["Filters AS F", "MessageFilters AS MF"],
                          ["F.FilterID = MF.FilterID AND MF.MessageID = %s"], [row[0]])

            q_filters = cursor.fetchall()
            filters = []
            for row2 in q_filters:
                filter = {}
                filter["key"] = row2[0]
                filter["value"] = row2[1]
                filter["is_whitelist"] = False if row2[2] == "\x00" else True
                filters.append(filter)

            result.append(create_msg_dict(row[0], row[1], row[2], row[3], row[4], row[5], filters))

        cursor.close()
        return result

    def __search_messages(self, session_id, loc_lst):
        cursor = self.conn.cursor()

        # Sequence: lat, long, lat, timestamp, timestamp
        haversine_formula = "OR ((6371000 * acos(cos(radians(%s)) * cos(radians(G.Latitude)) \
                * cos(radians(G.Longitude) - radians(%s)) + sin(radians(%s)) * sin(radians(G.Latitude)))) <= G.Radius \
                AND %s >= M.StartDate AND %s < M.EndDate) "

        ssid_cmp = "OR (W.WifiID = %s AND %s >= M.StartDate AND %s < M.EndDate) "

        q_black = "(SELECT COUNT(DISTINCT MF.FilterID) FROM MessageFilters AS MF, UserFilters AS UF \
                  WHERE MF.MessageID = M.MessageID AND MF.Whitelist = 0 AND UF.Username = %s \
                  AND UF.FilterID = MF.FilterID) = 0"


        q_white = "(SELECT COUNT(DISTINCT MF.FilterID) FROM MessageFilters AS MF, UserFilters AS UF \
                  WHERE MF.MessageID = M.MessageID AND MF.Whitelist = 1 AND UF.Username = %s \
                  AND UF.FilterID = MF.FilterID)\
                  =\
                  (SELECT COUNT(DISTINCT MF.FilterID) FROM MessageFilters AS MF \
                  WHERE MF.MessageID = M.MessageID AND MF.Whitelist = 1)"

        q_not_delivered = "M.MessageID NOT IN (SELECT DM.MessageID FROM DeliveredMessages AS DM WHERE DM.Username = %s)"

        q_having = ") HAVING (" + q_white + ") AND (" + q_black + ") AND (" + q_not_delivered + ")"

        q_gps = None
        q_ssids = None

        gps = []
        ssids = []

        self.__select(cursor, "Username", ["Sessions"], ["SessionID = %s"], [session_id])
        if cursor.rowcount == 0:
            cursor.close()
            return create_error_json(error_session_not_found)

        user = cursor.fetchone()

        for loc in loc_lst:
            tm = loc["timestamp"]

            if "lat" in loc and "long" in loc:
                if q_gps is None:
                    q_gps = "M.Username != %s AND G.Location = M.Location AND ( 0 "
                    gps.extend(user)
                q_gps += haversine_formula

                gps.extend([loc["lat"], loc["long"], loc["lat"], tm, tm])

            if len(loc["ssids"]) > 0:
                if q_ssids is None:
                    q_ssids = "M.Username != %s AND W.Location = M.Location AND ( 0 "
                    ssids.extend(user)

                for ssid in loc["ssids"]:
                    q_ssids += ssid_cmp
                    ssids.extend([ssid, tm, tm])

        result = []

        if q_gps is not None:
            q_gps += q_having
            gps.extend([user, user, user])
            q_res = self.__get_msgid_gps(q_gps, gps)
            if q_res is not None:
                result.extend(q_res)

        if q_ssids is not None:
            q_ssids += q_having
            ssids.extend([user, user, user])
            q_res = self.__get_msgid_ssid(q_ssids, ssids)
            if q_res is not None:
                result.extend(q_res)

        cursor.close()
        return create_json(["messages"], [list(result)])

    def filtered_delivery(self, session_id, loc_lst):
        msgs = self.__search_messages(session_id, loc_lst)
        out_json = loads(msgs)

        if "messages" in out_json:
            cursor = self.conn.cursor()

            # There will be one user, because __search_messages was successful
            self.__select(cursor, "Username", ["Sessions"], ["SessionID = %s"], [session_id])
            user = cursor.fetchone()
            for el in out_json["messages"]:
                try:
                    self.__insert(cursor, "DeliveredMessages", ["Username", "MessageID"], [user, el["id"]])
                except MySQLdb.Error:
                    # There was someone that sent this message to this user
                    continue
            cursor.close()
            self.conn.commit()

        return msgs

    def count_messages(self, session_id, loc_lst):
        msgs = self.__search_messages(session_id, loc_lst)
        out_json = loads(msgs)

        if "messages" in out_json:
            msgs = create_json(["n_messages"], [len(out_json["messages"])])

        return msgs

    def close(self):
        self.conn.close()
