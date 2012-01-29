#!/bin/bash

java -cp /etc/hbase/conf:target/hadoop-integration-1.0-SNAPSHOT.jar:/usr/lib/hbase/hbase-0.90.3-cdh3u1.jar:/etc/alternatives/hadoop-lib/hadoop-core-0.20.2-cdh3u1.jar:/usr/lib/hbase/lib/commons-logging-1.1.1.jar:/usr/lib/hbase/lib/zookeeper.jar:/usr/lib/hbase/lib/log4j-1.2.16.jar org.jboss.tusk.hadoop.Shell "$@"

