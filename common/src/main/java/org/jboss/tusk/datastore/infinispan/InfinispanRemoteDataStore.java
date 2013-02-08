package org.jboss.tusk.datastore.infinispan;

import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.tusk.datastore.DataStore;
import org.jboss.tusk.exception.DataStoreException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;

public class InfinispanRemoteDataStore implements DataStore {
	
	private static final Log LOG = LogFactory.getLog(InfinispanRemoteDataStore.class);

	public InfinispanRemoteDataStore() {
	}

	@Override
	public void put(String id, byte[] data) throws DataStoreException {
		//use the tomcat rest service
		Form f = new Form();
		f.add("value", new String(data));
		Client c = Client.create();
		WebResource r = c.resource("http://localhost:8888/TuskUI/rest/indexer/store/");
		String indexResponse = r.path(id).type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).post(String.class, f);
	    LOG.info("Done storing value for key " + id + "; response was " + indexResponse);
	}

	@Override
	public byte[] get(String id) throws DataStoreException {
		//use the tomcat rest service
		//TODO test this
		Client c = Client.create();
		WebResource r = c.resource("http://localhost:8888/TuskUI/rest/loader/load/" + id);
		String dataStr = r.path(id).type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).get(String.class);
	    LOG.info("Done getting object with id " + id + "; response was " + dataStr);
	    
	    return dataStr.getBytes();
	}

}
