#!/bin/bash

echo "Installing pip2"
sudo apt-get install python-pip2 python-dev build-essential
sudo pip2 install --upgrade pip2
sudo pip2 install --upgrade virtualenv

echo "Installing flask"
sudo pip2 install Flask

echo "Installing PyCrypto"
sudo pip2 install pycrypto

echo "Installing MySQL python module"
sudo apt-get install python-mysqldb
