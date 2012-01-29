package org.jboss.tusk.common;

public class TuskCassandraConfiguration {
	
	private String cluster = "localhost:9160";
	
	private String keyspace = "TuskData";

	private String columnFamily = "Messages";

	public String getCluster() {
		return cluster;
	}
	public void setCluster(String cluster) {
		this.cluster = cluster;
	}
	
	public String getKeyspace() {
		return keyspace;
	}
	public void setKeyspace(String keyspace) {
		this.keyspace = keyspace;
	}
	
	public String getColumnFamily() {
		return columnFamily;
	}
	public void setColumnFamily(String columnFamily) {
		this.columnFamily = columnFamily;
	}

}
