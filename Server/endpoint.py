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


# TODO: Send the current user filters (maybe she is connecting from other device)
@app.route("/login", methods=['POST'])
def login():
    req = request.get_json()

    # TODO: Access the DB

    data = {}
    data['session_id'] = '123456789'
    return dumps(data)


@app.route("/signup", methods=['POST'])
def signup():
    res = None
    req = request.get_json()

    if "username" in req and "password" in req:
        res = db.signup(req["username"], req["password"])

    if res is None:
        return create_error_json(error_username_exists)

    data = {}
    data['session_id'] = res
    return dumps(res)


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
