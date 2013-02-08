package org.jboss.tusk.datastore;

import org.jboss.tusk.exception.DataStoreException;

public interface DataStore {
	
	public void put(String id, byte[] data) throws DataStoreException;
	
	public byte[] get(String id) throws DataStoreException;

}
