package org.jboss.tusk.common;

public class TuskConfiguration {
	
	//to change the data store (among Cassandra, HBase, or whatever else is supported)
	//change this default value
	//TODO put the default value in a startup argument or config file (eg /etc/tusk/config.properties)
//	private DataStore dataStore = DataStore.CASSANDRA;
	//private DataStore dataStore = DataStore.HBASE;
	private DataStore dataStore = DataStore.INFINISPAN;
	
	public DataStore getDataStore() {
		return dataStore;
	}

	public void setDataStore(DataStore dataStore) {
		this.dataStore = dataStore;
	}
	
	
}
