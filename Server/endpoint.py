#!/usr/bin/env python

import threading
import sys
from error_messages import *
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


@app.route("/request_locations", methods=['POST'])
def request_locations():
    req = request.get_json()

    print("IN: " + str(req) + "\n")
    sys.stdout.flush()

    if "session_id" in req and "startswith" in req and "range" in req:
        return db.request_locations(req["session_id"], req["startswith"], req["range"])

    return create_error_json(error_keys_not_in_json)


def start_server():
    app.run(host="0.0.0.0", port=80, threaded=True)


# Updates the DNS resolution to the current public ip
my_ip = load(urlopen('http://jsonip.com'))['ip']
urlopen("https://www.duckdns.org/update?domains=locmess&token=f0eccba8-8678-4968-b828-21808fdd1462&ip=" + my_ip)

# Runs the server on a new thread
t = threading.Thread(target=start_server)
t.daemon = True
t.start()

print("Press any key to stop the server.\n")
sys.stdout.flush()
raw_input()
db.close()