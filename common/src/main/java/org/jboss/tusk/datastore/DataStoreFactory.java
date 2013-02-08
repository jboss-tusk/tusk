package org.jboss.tusk.datastore;

import org.jboss.tusk.common.DataStoreType;
import org.jboss.tusk.common.TuskConfiguration;
import org.jboss.tusk.exception.DataStoreException;

public class DataStoreFactory {

	private static final TuskConfiguration configuration = new TuskConfiguration();
	
	private static final DataStoreFactory INSTANCE = new DataStoreFactory();
	
	private DataStoreFactory() {
	}
	
	public static DataStoreFactory getInstance() {
		return DataStoreFactory.INSTANCE;
	}
	
	public DataStore getDataStore() throws DataStoreException {
		String className = null;
		if (configuration.getDataStoreType().equals(DataStoreType.CASSANDRA)) {
			className = "org.jboss.tusk.datastore.nosql.CassandraDataStore";
		} else if (configuration.getDataStoreType().equals(DataStoreType.HBASE)) {
			className = "org.jboss.tusk.datastore.nosql.HBaseDataStore";
		} else if (configuration.getDataStoreType().equals(DataStoreType.FILESYSTEM)) {
			className = "org.jboss.tusk.datastore.filesystem.FileSystemDataStore";
		} else {
			className = "org.jboss.tusk.datastore.infinispan.InfinispanRemoteDataStore";
		}
		
		try {
			Class clazz = Class.forName(className);
			return (DataStore)clazz.newInstance();
		} catch (ClassNotFoundException ex) {
			throw new DataStoreException("Caught ClassNotFoundException getting data store: " + ex.getMessage(), ex);
		} catch (IllegalAccessException ex) {
			throw new DataStoreException("Caught IllegalAccessException getting data store: " + ex.getMessage(), ex);
		} catch (InstantiationException ex) {
			throw new DataStoreException("Caught InstantiationException getting data store: " + ex.getMessage(), ex);
		}
	}

}
