#!/bin/bash

a=1600
while [ $a -lt 1811 ]
do
echo $a
java Server $a &
a=`expr $a + 1` 
done
