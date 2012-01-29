#!/usr/bin/env bash

# Start services.

sudo service hadoop-0.20-namenode start
sudo service hadoop-0.20-datanode start
sudo service hadoop-0.20-secondarynamenode start
sudo service hadoop-0.20-jobtracker start
sudo service hadoop-0.20-tasktracker start
sudo service hadoop-hbase-master start
sudo service hadoop-hbase-regionserver start
sudo service hadoop-zookeeper-server start
