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
	
	//TODO put the default value in a startup argument or config file (eg /etc/tusk/config.properties)
	
	//Other supported data stores can be found in org.jboss.tusk.common.DataStore
	private DataStore dataStore = DataStore.INFINISPAN;
	
	public DataStore getDataStore() {
		return dataStore;
	}

	public void setDataStore(DataStore dataStore) {
		this.dataStore = dataStore;
	}
	
	
}
