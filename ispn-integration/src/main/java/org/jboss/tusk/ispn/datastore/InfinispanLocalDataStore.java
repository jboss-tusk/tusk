package org.jboss.tusk.ispn.datastore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.tusk.datastore.DataStore;
import org.jboss.tusk.exception.DataStoreException;
import org.jboss.tusk.ispn.InfinispanService;
import org.jboss.tusk.ispn.InfinispanException;

public class InfinispanLocalDataStore implements DataStore {
	
	private static final Log LOG = LogFactory.getLog(InfinispanLocalDataStore.class);

	private static InfinispanService ispnService = null;
	
	public InfinispanLocalDataStore() {
		System.setProperty("java.net.preferIPv4Stack", "true");
		ispnService = new InfinispanService();
	}

	@Override
	public void put(String id, byte[] data) throws DataStoreException {
		try {
			ispnService.writeValue(id, new String(data));
		} catch (InfinispanException ex) {
			throw new DataStoreException("Exception happened while " +
			"writing payload to Infinispan grid.", ex);
		}
	}

	@Override
	public byte[] get(String id) throws DataStoreException {
		try {
			String dataStr = ispnService.loadValue(id);
			LOG.debug("dataStr=" + dataStr);
			
		    return dataStr.getBytes();
		} catch (InfinispanException ex) {
			throw new DataStoreException("Got InfinispanException getting object " +
					"with id " + id + ":" + ex.getMessage(), ex);
		}
	}

}
