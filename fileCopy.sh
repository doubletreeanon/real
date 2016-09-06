#!/bin/bash

a=212
file="AbdulKalam.txt"
while [ $a -lt 423 ]
do
cp $file "$a.txt"
a=`expr $a + 1` 
done
