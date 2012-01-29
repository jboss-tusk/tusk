#!/usr/bin/env bash

# Start services.

sudo service hadoop-0.20-namenode status
sudo service hadoop-0.20-datanode status
sudo service hadoop-0.20-secondarynamenode status
sudo service hadoop-0.20-jobtracker status
sudo service hadoop-0.20-tasktracker status
sudo service hadoop-hbase-master status
sudo service hadoop-hbase-regionserver status
sudo service hadoop-zookeeper-server status
