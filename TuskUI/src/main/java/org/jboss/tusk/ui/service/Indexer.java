package org.jboss.tusk.ui.service;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.tusk.ispn.InfinispanException;
import org.jboss.tusk.ispn.InfinispanService;



@Path("/indexer")
public class Indexer {

	private static InfinispanService ispnService = null;

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		ispnService = new InfinispanService();
	}

	@POST
	@Path("/store/{key}")
	@Produces(MediaType.TEXT_PLAIN)
	public String storePost(@PathParam("key") String key, @FormParam("value") String value) {
		System.out.println("Storing " + key + "->" + value);
		try {
			ispnService.writeValue(key, value);
		} catch (InfinispanException ex) {
			ex.printStackTrace();
			System.out.println("Caught InfinispanExcepion storing data: " + ex.getMessage());
			return "Caught InfinispanExcepion storing data: " + ex.getMessage();
		}
		return value.length() + " bytes added to key " + key;
	}

	@GET
	@Path("/add/{messageKey}/{indexes}")
	public String addGet(@PathParam("messageKey") String messageKey, @PathParam("indexes") String indexes) {
		return doAdd(messageKey, indexes);
	}

	@POST
	@Path("/add/{messageKey}")
	@Produces(MediaType.TEXT_PLAIN)
	public String addPost(@PathParam("messageKey") String messageKey, @FormParam("indexes") String indexes) {
		return doAdd(messageKey, indexes);
	}
	
	private String doAdd(String messageKey, String indexes) {
		System.out.println("Adding index for messageKey " + messageKey + "->" + indexes);
		
		//deserialize the indexes
		Map<String, Object> indexMap = deserializeMap(indexes);
		
		//add the indexes to infinispan
		if (indexMap == null || indexMap.size() == 0) {
			System.out.println("Nothing to index for messageKey " + messageKey);
		} else {
			try {
				ispnService.writeIndex(messageKey, indexMap);
			} catch (InfinispanException ex) {
				ex.printStackTrace();
				System.out.println("Caught InfinispanExcepion writing index: " + ex.getMessage());
				return "Caught InfinispanExcepion writing index: " + ex.getMessage();
			}
		}
		
		return "added indexes for " + messageKey;
	}
	
	private Map<String, Object> deserializeMap(String mapStr) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			String[] items = mapStr.split("\\|");
			for (int i = 0; i < items.length; i++) {
				String[] entry = items[i].split(":");
				map.put(entry[0], entry[1]);
			}
	
			System.out.println("Deserialized map:");
			for (Object key : map.keySet()) {
				System.out.println("  " + key + "=" + map.get(key));
			}
		} catch (Exception ex) {
			System.out.println("Exception adding index: " + ex.getMessage());
		}
		
		return map;
	}
	
	@GET
	@Path("/test")
	@Produces(MediaType.TEXT_HTML)
	public String getTestData() {
		System.out.println("Getting test data");
		
		return "Test Data";
	}
	
}
