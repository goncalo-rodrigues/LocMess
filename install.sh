#!/bin/bash

echo "Installing pip"
sudo apt-get install python-pip python-dev build-essential
sudo pip install --upgrade pip
sudo pip install --upgrade virtualenv

echo "Installing flask"
sudo pip install Flask
