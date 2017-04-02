#!/usr/bin/env python

import threading
import sys
from db import *
from urllib2 import urlopen
from flask import *
from json import *


app = Flask(__name__)
db = Database()


@app.route("/login", methods=['POST'])
def login():
    req = request.get_json()

    print("IN: " + str(req) + "\n")
    sys.stdout.flush()

    if "username" in req and "password" in req:
        return db.login(req["username"], req["password"])

    return create_error_json(error_keys_not_in_json)


@app.route("/signup", methods=['POST'])
def signup():
    req = request.get_json()

    print("IN: " + str(req) + "\n")
    sys.stdout.flush()

    if "username" in req and "password" in req:
        return db.signup(req["username"], req["password"])

    return create_error_json(error_keys_not_in_json)


@app.route("/logout", methods=['POST'])
def logout():
    req = request.get_json()

    print("IN: " + str(req) + "\n")
    sys.stdout.flush()

    if "session_id" in req:
        return db.logout(req["session_id"])

    return create_error_json(error_keys_not_in_json)


@app.route("/request_locations", methods=['POST'])
def request_locations():
    req = request.get_json()

    print("IN: " + str(req) + "\n")
    sys.stdout.flush()

    if "session_id" in req and "startswith" in req:
        return db.request_locations(req["session_id"], req["startswith"])

    return create_error_json(error_keys_not_in_json)


# TODO
@app.route("/send_locations", methods=['POST'])
def send_locations():
    req = request.get_json()

    print("IN: " + str(req) + "\n")
    sys.stdout.flush()

    return create_error_json(error_method_not_implemented)


@app.route("/create_location", methods=['POST'])
def create_location():
    req = request.get_json()

    print("IN: " + str(req) + "\n")
    sys.stdout.flush()

    if "session_id" in req and "name" in req:
        if "gps" in req:
            return db.create_gps_location(req["session_id"], req["name"], req["gps"])
        elif "ssids" in req:
            return db.create_ssids_location(req["session_id"], req["name"], req["ssids"])

    return create_error_json(error_keys_not_in_json)


# TODO
@app.route("/remove_location", methods=['POST'])
def remove_location():
    req = request.get_json()

    print("IN: " + str(req) + "\n")
    sys.stdout.flush()

    return create_error_json(error_method_not_implemented)


# TODO
@app.route("/set_my_filter", methods=['POST'])
def set_my_filter():
    req = request.get_json()

    print("IN: " + str(req) + "\n")
    sys.stdout.flush()

    return create_error_json(error_method_not_implemented)


# TODO: Diogo changed the method interface (check on GitHub)
@app.route("/get_filters", methods=['POST'])
def get_filters():
    req = request.get_json()

    print("IN: " + str(req) + "\n")
    sys.stdout.flush()

    return create_error_json(error_method_not_implemented)


# TODO
@app.route("/remove_filter", methods=['POST'])
def remove_filter():
    req = request.get_json()

    print("IN: " + str(req) + "\n")
    sys.stdout.flush()

    return create_error_json(error_method_not_implemented)


# TODO
@app.route("/post_message", methods=['POST'])
def post_message():
    req = request.get_json()

    print("IN: " + str(req) + "\n")
    sys.stdout.flush()

    return create_error_json(error_method_not_implemented)


# TODO
@app.route("/delete_message", methods=['POST'])
def delete_message():
    req = request.get_json()

    print("IN: " + str(req) + "\n")
    sys.stdout.flush()

    return create_error_json(error_method_not_implemented)


def start_server():
    app.run(host="0.0.0.0", port=80, threaded=True)


# Updates the DNS resolution to the current public ip
my_ip = load(urlopen('http://jsonip.com'))['ip']
urlopen("https://www.duckdns.org/update?domains=locmess&token=f0eccba8-8678-4968-b828-21808fdd1462&ip=" + my_ip)

# Runs the server on a new thread
t = threading.Thread(target=start_server)
t.daemon = True
t.start()

print("Press <enter> to stop the server.\n")
sys.stdout.flush()
raw_input()
db.close()

# FIXME: Debug stuff
# search_for = "barc"
# signup_res = loads(db.login("a", "a"))
# print "Session ID: " + str(signup_res)
# print "Created GPS: " + str(db.create_gps_location(signup_res["session_id"], "Barco", {"lat": 12, "long": 13, "radius": 14}))
# print "Created WifiIDs: " + str(db.create_ssids_location(signup_res["session_id"], "Barca", ["eduroam", "h3", "bananas"]))
# print "Requested locations result for " + search_for + ": " + str(db.request_locations(signup_res["session_id"], search_for))
#
# # TODO: Put here the new created methods
#
# print "Logout result: " + str(db.logout(signup_res["session_id"]))
