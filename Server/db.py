import MySQLdb
import Crypto.Random.random


class Database:
    def __init__(self, in_user='locmess_account', in_passwd='FDvlalaland129&&', in_host='localhost', in_db='cmu_locmess'):
        self.conn = MySQLdb.connect(user=in_user, passwd=in_passwd, host=in_host, db=in_db)

    def signup(self, username, password):
        try:
            # TODO: Create a specific method with this
            query = "INSERT INTO Users(Username, Password) VALUES (%s, %s)"
            cursor = self.conn.cursor()
            cursor.execute(query, [username, password])
            query = "INSERT INTO Sessions(SessionID, Username) VALUES (%i, %s)"
            class_random = Crypto.Random.random.StrongRandom()
            random = class_random.getrandbits(128)
            cursor.execute(query, [random, username])
            cursor.close()
            self.conn.commit()
            return random
        except MySQLdb.Error:
            return None

    
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