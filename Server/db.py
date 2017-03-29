import MySQLdb
import Crypto.Random.random


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

    def __select(self, cursor, col, tb, wh):
        query = "SELECT " + col + " FROM " + tb[0]

        rem_tbs = tb[1:]
        for t in rem_tbs:
            query += "," + t

        query += " WHERE "

        eq_pos = 0
        wh_len = len(wh)
        for i in range(wh_len):
            query += "%s "

            if(eq_pos == 0):
                query += "= "

            eq_pos = (eq_pos + 1) % 3

        cursor.execute(query, wh)

    def __create_random_str(self, length):
        class_random = Crypto.Random.random.StrongRandom()
        lst_rand = [chr(class_random.getrandbits(7)) for _ in range(length)]
        return "".join(lst_rand)

    """
        From this point on, the functions correspond to each available service
    """

    def signup(self, username, password):
        cursor = self.conn.cursor()
        try:
            self.__insert(cursor, "Users", ("Username", "Password"), [username, password])
            id = self.__create_random_str(128)
            self.__insert(cursor, "Sessions", ("SessionID", "Username"), [id, username])
            cursor.close()
            self.conn.commit()
            return id

        # This will be used to send the error message
        except MySQLdb.Error:
            cursor.close()
            return None

    # TODO: Return the filters of the given user
    def login(self, username, password):
        cursor = self.conn.cursor()

        try:
            self.__select(cursor, "*", ("Users"), ["Username", username, "AND", "Password", password])
        except MySQLdb:
            print("Oops!")

        # TODO: Continue from here!!!

        id = self.__create_random_str(128)
        self.__insert(cursor, "Sessions", ("SessionID", "Username"), [id, username])
        cursor.close()
        self.conn.commit()
        return id

    def close(self):
        self.conn.close()




"""
cursor = cnx.cursor()
query = "SELECT userID FROM passwords"
cursor.execute(query)

for (user_id) in cursor:
    print("ID: " + str(user_id))

"""