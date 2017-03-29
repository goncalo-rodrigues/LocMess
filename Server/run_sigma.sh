#!/bin/bash

if [ "$#" -ne 2 ]; then
  echo "Usage: ./run_sigma.sh <path to AWS server certificate> <AWS machine public IP>"
  exit 1
fi

cert=$1
ip=$2

# Copy the file to AWS
scp -i $cert *.py "ec2-user@$ip:."
ssh -i $cert -l "ec2-user" $ip "chmod 777 *.py; sudo ./endpoint.py"
