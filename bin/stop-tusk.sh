#!/usr/bin/env bash

# Stop services.

sudo service hadoop-hbase-regionserver stop
sudo service hadoop-hbase-master stop
sudo service hadoop-zookeeper-server stop
sudo service hadoop-0.20-tasktracker stop
sudo service hadoop-0.20-jobtracker stop
sudo service hadoop-0.20-secondarynamenode stop
sudo service hadoop-0.20-datanode stop
sudo service hadoop-0.20-namenode stop
