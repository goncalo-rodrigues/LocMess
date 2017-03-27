#!/bin/bash

if [ "$#" -ne 1 ]; then
  echo "The argument must be your student number"
  exit 1
fi

# Copy the file sigma
scp endpoint.py "ist1$1@sigma.ist.utl.pt:."
