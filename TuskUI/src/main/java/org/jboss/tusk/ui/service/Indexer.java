package org.jboss.tusk.ui.service;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.template.ColumnFamilyResult;
import me.prettyprint.cassandra.service.template.ColumnFamilyTemplate;
import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.factory.HFactory;

import org.apache.hadoop.hbase.util.Bytes;
import org.jboss.tusk.common.DataStore;
import org.jboss.tusk.common.TuskCassandraConfiguration;
import org.jboss.tusk.common.TuskConfiguration;
import org.jboss.tusk.hadoop.HBaseException;
import org.jboss.tusk.hadoop.service.MessagePersister;
import org.jboss.tusk.ispn.InfinispanException;
import org.jboss.tusk.ispn.InfinispanService;
import org.jboss.tusk.ispn.index.SearchCriterion;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;



@Path("/indexer")
public class Indexer {

	private static final TuskConfiguration configuration = new TuskConfiguration();
	private static TuskCassandraConfiguration tuskCassConf = new TuskCassandraConfiguration();

	private static InfinispanService ispnService = null;
	
	//for cassandra
	private static TuskCassandraConfiguration tuskCassConfConf = new TuskCassandraConfiguration();
	private static Cluster dataCluster = null;
	private static Keyspace ksp = null;
	private static ColumnFamilyTemplate<String, String> cfTemplate = null;
	
	//for hbase
	private MessagePersister messagePersister = null;
	
	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		ispnService = new InfinispanService();
		
		if (configuration.getDataStore().equals(DataStore.CASSANDRA)) {
			dataCluster = HFactory.getOrCreateCluster("data-cluster", tuskCassConf.getCluster());
			System.out.println("Hector dataCluster=" + dataCluster);
			
			// This is the keyspace to use for the data we are storing
			KeyspaceDefinition keyspaceDef = dataCluster.describeKeyspace(tuskCassConf.getKeyspace());
			System.out.println("Hector keyspaceDef=" + keyspaceDef);
			
			ksp = HFactory.createKeyspace(tuskCassConf.getKeyspace(), dataCluster);
			System.out.println("Hector keyspace=" + ksp);
			
			cfTemplate = new ThriftColumnFamilyTemplate<String, String>(
					ksp, tuskCassConf.getColumnFamily(), StringSerializer.get(), StringSerializer.get());
			System.out.println("Hector cfTemplate=" + cfTemplate);
		} else if (configuration.getDataStore().equals(DataStore.HBASE)) {
			//init is in ctor
		} else if (configuration.getDataStore().equals(DataStore.INFINISPAN)) {
			//init already done above
		}
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
	@Path("/add/{messageKey}")
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
	@Path("/zip")
	@Produces(MediaType.TEXT_HTML)
	public String getZips() {
		return getZips(null);
	}
	
	@GET
	@Path("/zip/{zip}")
	@Produces(MediaType.TEXT_HTML)
	public String getZips(@PathParam("zip") String zip) {
		System.out.println("Getting zip codes" + (zip != null ? " for " + zip : ""));

		//first search to get all entries
		List<SearchCriterion> searchCriteria = new ArrayList<SearchCriterion>();
		searchCriteria.add(new SearchCriterion("zip", zip == null ? "*" : zip));
		List<String> results = ispnService.searchIndex(searchCriteria, true);
		
		//now load the corresponding messages
		StringBuffer buf = new StringBuffer();
		if (results.size() > 0) {
			for (String msgId : results) {
				String message = "";
				System.out.println("Loading message " + msgId);
				try {
					if (configuration.getDataStore().equals(DataStore.CASSANDRA)) {
					    ColumnFamilyResult<String, String> result = cfTemplate.queryColumns(msgId);
					    message = result.getString("body");
					} else if (configuration.getDataStore().equals(DataStore.HBASE)) {
						message = Bytes.toString(messagePersister.readMessage(msgId));
					} else if (configuration.getDataStore().equals(DataStore.INFINISPAN)) {
						message = ispnService.loadValue(msgId);
					}
					
					if (buf.length() > 0) {
						buf.append("|");
					}
					buf.append(extractZip(message) + "," + message);
				} catch (Exception ex) {
				    System.err.println("Got " + ex.getClass().getName() + " reading message " + msgId + ": " + ex.getMessage());
				}
			}
		}
		
		System.out.println("Returning " + buf.toString());
		
		return buf.toString();
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
			System.out.println("Got zip '" + zip + "'");
			
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
		System.out.println("Getting test data");
		
		return "Test Data";
	}
	
}
