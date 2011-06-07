#!/bin/bash

mkdir $1
cd $1
hg init 
touch file
hg add file
hg branch master
echo "1 master" >> file
hg commit -m"master commit"
sleep 3
echo "2 master" >> file
hg commit -m"master commit"
sleep 3
hg branch site1
echo "3 site1" >> file
hg commit -m"site1 commit"
sleep 3
echo "4 site1" >> file
hg commit -m"site1 commit"
sleep 3
hg update master
echo "3 master" >> file
hg commit -m"master commit"
sleep 3
echo "4 master" >> file
hg commit -m"master commit"
hg branch site2
echo "5 site2" >> file
hg commit -m"site2 commit"
sleep 3
echo "6 site2" >> file
hg commit -m"site2 commit"
sleep 3
hg update master
hg merge -f site2
hg commit -m"master merge site2"
echo "7 master" >> file
hg commit -m"master commit"
sleep 3
echo "8 master" >> file
hg commit -m"master commit"
sleep 3
hg merge -f site1
hg resolve -m file
hg commit -m"master merge site1"
