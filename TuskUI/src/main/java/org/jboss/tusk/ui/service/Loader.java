package org.jboss.tusk.ui.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.tusk.exception.DataStoreException;
import org.jboss.tusk.ui.SearchHelper;

@Path("/loader")
public class Loader {
	
	private static final Log LOG = LogFactory.getLog(Loader.class);

	SearchHelper helper = new SearchHelper();

	@GET
	@Path("/load/{id}")
	public String load(@PathParam("id") String id) {
		try {
			return helper.loadFromInfinispan(id);
		} catch (DataStoreException ex) {
			LOG.error("Returning null because we caught a DataStoreException loading " + 
					id + " from Infinispan: " + ex.getMessage(), ex);
			//TODO change this so it returns a 500 error code
			return null;
		}
	}
	
}
