package org.jboss.tusk.ui.service;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.tusk.ispn.InfinispanException;
import org.jboss.tusk.ispn.InfinispanService;
import org.jboss.tusk.ispn.index.SearchCriterion;
import org.jboss.tusk.ui.SearchHelper;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;


@Path("/indexer")
public class Indexer {

	private static final Log LOG = LogFactory.getLog(Indexer.class);

	private SearchHelper helper = new SearchHelper();
	
	private static InfinispanService ispnService = null;
	
	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		ispnService = new InfinispanService();
	}

	@POST
	@Path("/store/{key}")
	@Produces(MediaType.TEXT_PLAIN)
	public String storePost(@PathParam("key") String key, @FormParam("value") String value) {
		LOG.debug("Storing " + key + "->" + value);
		try {
			ispnService.writeValue(key, value);
		} catch (InfinispanException ex) {
			ex.printStackTrace();
			LOG.error("Caught InfinispanExcepion storing data: " + ex.getMessage());
			return "Caught InfinispanExcepion storing data: " + ex.getMessage();
		}
		return value.length() + " bytes added to key " + key;
	}

	@GET
	@Path("/add/{messageKey}")
	public String addGet(@PathParam("messageKey") String messageKey, String indexes) {
		return doAdd(messageKey, indexes);
	}

	@POST
	@Path("/add/{messageKey}")
	@Produces(MediaType.TEXT_PLAIN)
	public String addPost(@PathParam("messageKey") String messageKey, @FormParam("indexes") String indexes) {
		return doAdd(messageKey, indexes);
	}
	
	private String doAdd(String messageKey, String indexes) {
		LOG.debug("Adding index for messageKey " + messageKey + "->" + indexes);

		long start = System.currentTimeMillis();
		
		//deserialize the indexes
		Map<String, Object> indexMap = deserializeMap(indexes);
		
		//add the indexes to infinispan
		if (indexMap == null || indexMap.size() == 0) {
			LOG.debug("Nothing to index for messageKey " + messageKey);
		} else {
			try {
				ispnService.writeIndex(messageKey, indexMap);
			} catch (InfinispanException ex) {
				ex.printStackTrace();
				LOG.error("Caught InfinispanExcepion writing index: " + ex.getMessage());
				return "Caught InfinispanExcepion writing index: " + ex.getMessage();
			}
		}

		LOG.info("***WriteIndex: writing index for " + messageKey + " took " + (System.currentTimeMillis() - start) + " ms.");
		
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
	
			LOG.debug("Deserialized map:");
			for (Object key : map.keySet()) {
				LOG.debug("  " + key + "=" + map.get(key));
			}
		} catch (Exception ex) {
			LOG.error("Exception adding index: " + ex.getMessage());
		}
		
		return map;
	}

	@GET
	@Path("/search/{criteria}")
	@Produces(MediaType.TEXT_HTML)
	public String getSearch(@PathParam("criteria") String criteria) {
		LOG.debug("Searching on " + criteria);
		
		if (StringUtils.isEmpty(criteria)) {
			return "No criteria provided.";
		}

		//first search to get all entries
		List<SearchCriterion> searchCriteria = new ArrayList<SearchCriterion>();
		String[] fields = criteria.split(";");
		for (String field : fields) {
			String[] parts = field.split("=");
			searchCriteria.add(new SearchCriterion(parts[0], parts[1]));
		}
		List<String> results = helper.doSearch(searchCriteria, "and");
		
		//now load the corresponding messages
		StringBuffer buf = new StringBuffer();
		if (results.size() > 0) {
			try {
				Map<String, String> messages = helper.loadData(results);
				for (Entry<String, String> entry : messages.entrySet()) {
					LOG.debug("Handling message " + entry.getKey());
					if (buf.length() > 0) {
						buf.append("|");
					}
					buf.append(entry.getKey() + "=" + entry.getValue());
				}
			} catch (Exception ex) {
			    System.err.println("Got " + ex.getClass().getName() + " reading messages: " + ex.getMessage());
			}
		}
		
		LOG.debug("Returning " + buf.toString());
		
		return buf.toString();
	}

	@GET
	@Path("/zip")
	@Produces(MediaType.TEXT_HTML)
	public String getZips() {
		return getZips(null);
	}
	
	@GET
	@Path("/zip/{zip}")
	@Produces(MediaType.TEXT_HTML)
	public String getZips(@PathParam("zip") String zip) {
		return getSearch("zip=" + (StringUtils.isEmpty(zip) ? "*" : zip));
	}
	
	private String extractZip(String messageXml) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(messageXml)));
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			//TODO make namespace aware: http://jboss.com/person is NS for zip
			NamespaceContext ctx = new NamespaceContext() {
                public String getNamespaceURI(String prefix) {
                    String uri;
                    if (prefix.equals("per")) {
                        uri = "http://jboss.com/person";
                    } else if (prefix.equals("opp")) {
                    	uri = "http://jboss.com/person/operations";
                    } else {
                        uri = null;
                    }
                    return uri;
                }
               
                // Dummy implementation - not used!
                public Iterator getPrefixes(String val) {
                    return null;
                }
               
                // Dummy implemenation - not used!
                public String getPrefix(String uri) {
                    return null;
                }
            };
            xpath.setNamespaceContext(ctx);

			XPathExpression expr = xpath.compile("//per:zip");
			String zip = (String)expr.evaluate(doc, XPathConstants.STRING);
			LOG.debug("Got zip '" + zip + "'");
			
//			return zip;
			
			//TODO until namespace aware xpath works, do a stupid string operation
			return messageXml.substring(messageXml.indexOf("<per:zip>") + 9, messageXml.indexOf("</per:zip>"));
		} catch (Exception ex) {
			System.err.println("Got " + ex.getClass().getName() + " extracting zip: " + ex.getMessage());
			return null;
		}
	}

	@GET
	@Path("/test")
	@Produces(MediaType.TEXT_HTML)
	public String getTestData() {
		LOG.debug("Getting test data");
		
		return "Test Data";
	}
	
}
