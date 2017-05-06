#!/usr/bin/env python

import threading
import sys
from db import *
from urllib2 import urlopen
from flask import *
from json import *
import os


app = Flask(__name__)
db = Database()
out = sys.stdout
cont = True
SCRIPT_DIR = os.path.dirname(os.path.realpath(__file__)) + "/"
out.flush()


@app.route("/login", methods=['POST'])
def login():
    req = request.get_json()

    print("IN: " + str(req) + "\n")
    out.flush()

    if "username" in req and "password" in req:
        return db.login(req["username"], req["password"])

    return create_error_json(error_keys_not_in_json)


@app.route("/signup", methods=['POST'])
def signup():
    req = request.get_json()

    print("IN: " + str(req) + "\n")
    out.flush()

    if "username" in req and "password" in req:
        return db.signup(req["username"], req["password"])

    return create_error_json(error_keys_not_in_json)


@app.route("/logout", methods=['POST'])
def logout():
    req = request.get_json()

    print("IN: " + str(req) + "\n")
    out.flush()

    if "session_id" in req:
        return db.logout(req["session_id"])

    return create_error_json(error_keys_not_in_json)


@app.route("/request_locations", methods=['POST'])
def request_locations():
    req = request.get_json()

    print("IN: " + str(req) + "\n")
    out.flush()

    if "session_id" in req and "startswith" in req:
        return db.request_locations(req["session_id"], req["startswith"])

    return create_error_json(error_keys_not_in_json)


@app.route("/get_messages", methods=['POST'])
def get_messages():
    req = request.get_json()

    print("IN: " + str(req) + "\n")
    out.flush()

    if "session_id" in req and "locations" in req and are_locations(req["locations"]):
        return db.filtered_delivery(req["session_id"], req["locations"])

    return create_error_json(error_keys_not_in_json)


@app.route("/send_locations", methods=['POST'])
def send_locations():
    req = request.get_json()

    print("IN: " + str(req) + "\n")
    out.flush()

    if "session_id" in req and "locations" in req and are_locations(req["locations"]):
        return db.count_messages(req["session_id"], req["locations"])

    return create_error_json(error_keys_not_in_json)


@app.route("/create_location", methods=['POST'])
def create_location():
    req = request.get_json()

    print("IN: " + str(req) + "\n")
    out.flush()

    if "session_id" in req and "name" in req:
        if "gps" in req and "lat" in req["gps"] and "long" in req["gps"] and "radius" in req["gps"]:
            return db.create_gps_location(req["session_id"], req["name"], req["gps"])
        elif "ssids" in req:
            return db.create_ssids_location(req["session_id"], req["name"], req["ssids"])

    return create_error_json(error_keys_not_in_json)


@app.route("/remove_location", methods=['POST'])
def remove_location():
    req = request.get_json()

    print("IN: " + str(req) + "\n")
    out.flush()

    if "session_id" in req and "name" in req:
        return db.remove_location(req["session_id"], req["name"])

    return create_error_json(error_keys_not_in_json)


@app.route("/get_location_info", methods=['POST'])
def get_location_info():
    req = request.get_json()

    print("IN: " + str(req) + "\n")
    out.flush()

    if "session_id" in req and "location" in req:
        res = db.get_location_info(req["session_id"], req["location"])
        print("OUT: " + str(res) + "\n")
        out.flush()
        return res

    return create_error_json(error_keys_not_in_json)


@app.route("/set_my_filter", methods=['POST'])
def set_my_filter():
    req = request.get_json()

    print("IN: " + str(req) + "\n")
    out.flush()

    if "session_id" in req and "filter" in req and "key" in req["filter"] and "value" in req["filter"]:
        return db.set_my_filter(req["session_id"], req["filter"])

    return create_error_json(error_keys_not_in_json)


@app.route("/get_keys", methods=['POST'])
def get_keys():
    req = request.get_json()

    print("IN: " + str(req) + "\n")
    out.flush()

    if "session_id" in req:
        return db.get_keys(req["session_id"])

    return create_error_json(error_keys_not_in_json)


@app.route("/get_values_key", methods=['POST'])
def get_values_keys():
    req = request.get_json()

    print("IN: " + str(req) + "\n")
    out.flush()

    if "session_id" in req and "key" in req:
        return db.get_values_key(req["session_id"], req["key"])

    return create_error_json(error_keys_not_in_json)


@app.route("/remove_filter", methods=['POST'])
def remove_filter():
    req = request.get_json()

    print("IN: " + str(req) + "\n")
    out.flush()

    if "session_id" in req and "filter" in req and "key" in req["filter"] and "value" in req["filter"]:
        return db.remove_filter(req["session_id"], req["filter"])

    return create_error_json(error_keys_not_in_json)


@app.route("/post_message", methods=['POST'])
def post_message():
    req = request.get_json()

    print("IN: " + str(req) + "\n")
    out.flush()

    if "session_id" in req and "msg" in req and is_message(req["msg"]):
        return db.post_message(req["session_id"], req["msg"])

    return create_error_json(error_keys_not_in_json)


@app.route("/delete_message", methods=['POST'])
def delete_message():
    req = request.get_json()

    print("IN: " + str(req) + "\n")
    out.flush()

    if "session_id" in req and "msg_id" in req:
        return db.delete_msg(req["session_id"], req["msg_id"])

    return create_error_json(error_keys_not_in_json)


@app.route("/esgarabisch", methods=['GET'])
def shutdown():
    global cont
    cont = False
    return "Bye!"


def start_server():
    # HTTPS
    context = (SCRIPT_DIR + 'cert.pem', SCRIPT_DIR + 'key.pem')
    app.run(host="0.0.0.0", port=443, ssl_context=context, threaded=True)

    # HTTP
    # app.run(host="0.0.0.0", port=80, threaded=True)


# Updates the DNS resolution to the current public ip
my_ip = load(urlopen('http://jsonip.com'))['ip']
urlopen("https://www.duckdns.org/update?domains=locmess&token=f0eccba8-8678-4968-b828-21808fdd1462&ip=" + my_ip)

# Runs the server on a new thread
t = threading.Thread(target=start_server)
t.daemon = True
t.start()

# print("Press <enter> to stop the server.\n")
# out.flush()
# raw_input()

while cont:
    time.sleep(1)

db.close()
