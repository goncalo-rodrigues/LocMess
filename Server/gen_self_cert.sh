#!/bin/sh

SUBJ="/CN=CMU/OU=DEI/O=locmess.duckdns.org/L=Lisbon/C=PT"
openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -days 365 -subj $SUBJ -nodes
