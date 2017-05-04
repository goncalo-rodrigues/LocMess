#!/bin/sh

SUBJ="/CN=CMU/OU=DEI/O=locmess.duckdns.org/L=Lisbon/C=PT"
# -subj $SUBJ
# Country Name (2 letter code) [AU]:PT
# State or Province Name (full name) [Some-State]:Lisbon
# Locality Name (eg, city) []:Lisbon
# Organization Name (eg, company) [Internet Widgits Pty Ltd]:G02
# Organizational Unit Name (eg, section) []:
# Common Name (e.g. server FQDN or YOUR name) []:locmess.duckdns.org
# Email Address []:
openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -days 365 -nodes
