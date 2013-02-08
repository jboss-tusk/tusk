package org.jboss.tusk.hadoop;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HiveService extends AbstractService {

	private static final Log LOG = LogFactory.getLog(HiveService.class);
	
	public List<String> queryIndex(String hiveQLQuery) {
		LOG.debug("Executing query against message index: " + hiveQLQuery);
		if (isEmpty(hiveQLQuery)) {
			return new ArrayList<String>();
		}
		
		//TODO execute query
		LOG.debug("Not implemented yet.");
		
		return new ArrayList<String>();
	}

}
