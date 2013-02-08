package org.jboss.tusk.datastore.nosql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.tusk.datastore.DataStore;
import org.jboss.tusk.exception.DataStoreException;
import org.jboss.tusk.hadoop.HBaseException;
import org.jboss.tusk.hadoop.service.MessagePersister;

public class HBaseDataStore implements DataStore {
	
	private static final Log LOG = LogFactory.getLog(HBaseDataStore.class);

	private MessagePersister messagePersister = null;

	public HBaseDataStore() {
		messagePersister = new MessagePersister();
	}

	@Override
	public void put(String id, byte[] data) throws DataStoreException {
		try {
			messagePersister.writeMessage(id, data);
		} catch (HBaseException ex) {
			throw new DataStoreException("Caught HBaseException writing data with id " + id + ": " + ex.getMessage(), ex);
		}
	}

	@Override
	public byte[] get(String id) throws DataStoreException {
		try {
			return messagePersister.readMessage(id);
		} catch (HBaseException ex) {
			throw new DataStoreException("Caught HBaseException getting id " + id + ": " + ex.getMessage(), ex);
		}
	}

}
