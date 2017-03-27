import MySQLdb

cnx = MySQLdb.connect(user='dpm_account', passwd='FDvlalaland129&&',
                              host='localhost',
                              db='sec_dpm')

cursor = cnx.cursor()
query = "SELECT userID FROM passwords"
cursor.execute(query)

for (user_id) in cursor:
    print("ID: " + str(user_id))

cnx.close()
