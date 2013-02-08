package org.jboss.tusk.common;

/**
 * The TuskConfiguration class is responsible for designating which data store the Tusk 
 * application will be using.  The data stores compatible with Tusk at the moment are defined in 
 * org.jboss.tusk.common.DataStore.
 * 
 * @author
 *
 */
public class TuskConfiguration {
	
	//Other supported data stores can be found in org.jboss.tusk.common.DataStore.
	//Try to use a system property first, default to Infinispan if not set.
	private DataStoreType dataStoreType = DataStoreType.valueOf(
			System.getProperty("tusk.datastore", DataStoreType.INFINISPAN.toString()));
	
	public DataStoreType getDataStoreType() {
		return dataStoreType;
	}

	public void setDataStoreType(DataStoreType dataStoreType) {
		this.dataStoreType = dataStoreType;
	}
	
	
}
