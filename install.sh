#!/bin/bash

echo "Installing pip"
sudo apt-get install python-pip python-dev build-essential
sudo pip install --upgrade pip
sudo pip install --upgrade virtualenv

echo "Installing flask"
sudo pip install Flask

echo "Installing PyCrypto"
sudo pip install pycrypto

echo "Installing MySQL python module"
sudo apt-get install python-mysqldb
