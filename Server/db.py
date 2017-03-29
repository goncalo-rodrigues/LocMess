import MySQLdb
import Crypto.Random


class Database:
    def __init__(self, in_user='locmess_account', in_passwd='FDvlalaland129&&', in_host='localhost', in_db='cmu_locmess'):
        self.conn = MySQLdb.connect(user=in_user, passwd=in_passwd, host=in_host, db=in_db)

    def register(self, username, password):
        query = "INSERT INTO Users(Username, Password) VALUES (%s, %s)"

        # TODO: Create a specific method with this
        cursor = self.conn.cursor()
        cursor.execute(query, [username, password])
        query = "INSERT INTO Sessions(SessionID, Username, EndDate) VALUES (%s, %s)"
        random = Crypto.Random.get_random_bytes










"""
    def login(self, username, password):

    def close(self):
        self.conn.close()





cursor = cnx.cursor()
query = "SELECT userID FROM passwords"
cursor.execute(query)

for (user_id) in cursor:
    print("ID: " + str(user_id))

"""