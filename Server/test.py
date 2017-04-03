#!/usr/bin/env python

from db import *
from json import *

db = Database()
search_for = "barc"
signup_res = loads(db.login("a", "a"))
signup_res2 = loads(db.login("b", "b"))
print "Session ID: " + str(signup_res)
print "Second Session ID: " + str(signup_res)
print "\n======================================\n"

# Locations tests
print "Created GPS: " + str(db.create_gps_location(signup_res["session_id"], "Barco", {"lat": 12, "long": 13, "radius": 14}))
print "Created WifiIDs: " + str(db.create_ssids_location(signup_res["session_id"], "Barca", ["eduroam", "h3", "bananas"]))
print "Requested locations result for " + search_for + ": " + str(db.request_locations(signup_res["session_id"], search_for))
print "Deletion result: " + str(db.remove_location(signup_res["session_id"], "Barca"))
print "\n======================================\n"

# Filters tests
print "Filter creation result: " + str(db.set_my_filter(signup_res["session_id"], {"key": "TestKey", "value": "TestValue"}))
print "Second filter creation result: " + str(db.set_my_filter(signup_res2["session_id"], {"key": "TestKey", "value": "TestValue"}))
print "Getting filters: " + str(db.get_filters(signup_res["session_id"]))
print "Filter removal result: " + str(db.remove_filter(signup_res["session_id"], {"key": "TestKey", "value": "TestValue"}))
print "\n======================================\n"

# Message tests
print "Message filter creation result: " + str(db.set_my_filter(signup_res2["session_id"], {"key": "MessageKey", "value": "MessageValue"}))
res_dict = create_msg_dict("1", "a", "Barco", 123, 132, "This is the content of a possible message",
                           [{"key": "MessageKey", "value": "MessageValue", "is_whitelist":True}])

print "Message: " + str(res_dict)
print "Message creation result: " + str(db.post_message(signup_res["session_id"], res_dict))
print "Message deletion result: " + str(db.delete_msg(signup_res["session_id"], "1"))

print "\n======================================\n"

# Ends the test
print "Logout result: " + str(db.logout(signup_res["session_id"]))
print "Logout result: " + str(db.logout(signup_res2["session_id"]))