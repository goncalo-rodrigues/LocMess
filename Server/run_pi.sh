#!/bin/bash

if [ "$#" -ne 1 ]; then
  echo "Usage: ./run_pi.sh <raspberry pi public IP / DNS>"
  echo "Default: locmess.duckdns.org"
  echo ""
  ip="locmess.duckdns.org"
else
  ip=$1
fi

ssh -l "pi" $ip "mkdir locmess"
scp *.py "pi@$ip:./locmess"
tm=$(date +"%y-%m-%d_%H:%M:%S")
log="server$tm.log"
ssh -l "pi" $ip "cd locmess; chmod 777 *.py; sudo ./endpoint.py > >(tee -a $log) 2> >(tee -a $log >&2)"
