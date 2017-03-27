#!/usr/bin/env python

from urllib2 import urlopen
from flask import Flask
from json import load

app = Flask(__name__)

@app.route("/login")
def login():
    return "Hello World!"

if __name__ == "__main__":

    # Updates the DNS resolution to the current public ip
    my_ip = load(urlopen('http://jsonip.com'))['ip']
    urlopen("https://www.duckdns.org/update?domains=locmess&token=f0eccba8-8678-4968-b828-21808fdd1462&ip=" + my_ip)

    app.run(host=my_ip)
