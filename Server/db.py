import MySQLdb
import Crypto.Random.random
from error_messages import *


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

    # TODO: Return the filters of the given user
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
        return create_json(["session_id"], [id])

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

    def close(self):
        self.conn.close()
