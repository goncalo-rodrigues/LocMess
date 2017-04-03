#!/bin/bash

if [ "$#" -ne 1 ]; then
  echo "Usage: ./run_pi.sh <raspberry pi public IP / DNS>"
  exit 1
fi

ip=$1

ssh -l "pi" $ip "mkdir locmess"
scp *.py "pi@$ip:./locmess"
ssh -l "pi" $ip "cd locmess; chmod 777 *.py; rm server.log; sudo ./endpoint.py > >(tee -a server.log) 2> >(tee -a server.log >&2)"
