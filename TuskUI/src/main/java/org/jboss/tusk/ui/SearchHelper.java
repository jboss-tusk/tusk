package org.jboss.tusk.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

//import me.prettyprint.cassandra.serializers.StringSerializer;
//import me.prettyprint.cassandra.service.template.ColumnFamilyResult;
//import me.prettyprint.cassandra.service.template.ColumnFamilyTemplate;
//import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate;
//import me.prettyprint.hector.api.Cluster;
//import me.prettyprint.hector.api.Keyspace;
//import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
//import me.prettyprint.hector.api.exceptions.HectorException;
//import me.prettyprint.hector.api.factory.HFactory;

import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.util.Bytes;
import org.jboss.tusk.common.DataStoreType;
//import org.jboss.tusk.common.TuskCassandraConfiguration;
import org.jboss.tusk.common.TuskConfiguration;
import org.jboss.tusk.datastore.DataStore;
import org.jboss.tusk.datastore.DataStoreFactory;
//import org.jboss.tusk.datastore.filesystem.FileSystemDataStore;
import org.jboss.tusk.exception.DataStoreException;
//import org.jboss.tusk.hadoop.HBaseException;
//import org.jboss.tusk.hadoop.service.MessagePersister;
import org.jboss.tusk.ispn.InfinispanException;
import org.jboss.tusk.ispn.InfinispanService;
import org.jboss.tusk.ispn.index.SearchCriterion;
import org.apache.commons.logging.Log;

public class SearchHelper {
	
	private static final Log LOG = LogFactory.getLog(SearchHelper.class);

//	private String[] fields = {"disease", "state", "patientId"};
//	private String[] fields = {"id", "city", "addressLine1", "firstName", "lastName", "state", "zip"};
	private String[] fields = {"time", "origIp", "origPort", "destIp", "destPort", "transIp", "transPort", 
			"id", "city", "addressLine1", "firstName", "lastName", "state", "zip"};

	private static final TuskConfiguration configuration = new TuskConfiguration();
	
	private DataStore dataStore = null;
	
	
//	private static TuskCassandraConfiguration tuskCassConf = new TuskCassandraConfiguration();
	
	private static InfinispanService ispnService = null;
	
	//for cassandra
//	private static TuskCassandraConfiguration tuskCassConfConf = new TuskCassandraConfiguration();
//	private static Cluster dataCluster = null;
//	private static Keyspace ksp = null;
//	private static ColumnFamilyTemplate<String, String> cfTemplate = null;

	//for hbase
//	private MessagePersister messagePersister = null;
	
	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		ispnService = new InfinispanService();
		
//		if (configuration.getDataStoreType().equals(DataStoreType.CASSANDRA)) {
//			dataCluster = HFactory.getOrCreateCluster("data-cluster", tuskCassConf.getCluster());
//			LOG.debug("Hector dataCluster=" + dataCluster);
//			
//			// This is the keyspace to use for the data we are storing
//			KeyspaceDefinition keyspaceDef = dataCluster.describeKeyspace(tuskCassConf.getKeyspace());
//			LOG.debug("Hector keyspaceDef=" + keyspaceDef);
//			
//			ksp = HFactory.createKeyspace(tuskCassConf.getKeyspace(), dataCluster);
//			LOG.debug("Hector keyspace=" + ksp);
//			
//			cfTemplate = new ThriftColumnFamilyTemplate<String, String>(
//					ksp, tuskCassConf.getColumnFamily(), StringSerializer.get(), StringSerializer.get());
//			LOG.debug("Hector cfTemplate=" + cfTemplate);
//		} else if (configuration.getDataStoreType().equals(DataStoreType.HBASE)) {
//			//init is in ctor
//		} else if (configuration.getDataStoreType().equals(DataStoreType.INFINISPAN)) {
//			//init already done above
//		} else if (configuration.getDataStoreType().equals(DataStoreType.FILESYSTEM)) {
//			//nothing to do
//		}
	}
	
	public SearchHelper() {
		try {
			dataStore = DataStoreFactory.getInstance().getDataStore();
		} catch (DataStoreException ex) {
			LOG.error("Caught DataStoreException getting DataStore object: " + ex.getMessage(), ex);
		}
		
//		if (configuration.getDataStoreType().equals(DataStoreType.CASSANDRA)) {
//			//TODO move init from static block in here???
//		} else if (configuration.getDataStoreType().equals(DataStoreType.HBASE)) {
//			//for HBase
//			messagePersister = new MessagePersister();
//		} else if (configuration.getDataStoreType().equals(DataStoreType.INFINISPAN)) {
//			//init is in static block
//		} else if (configuration.getDataStoreType().equals(DataStoreType.FILESYSTEM)) {
//			//init is in static block
//		}
	}
	
	public List<String> doSearch(List<SearchCriterion> criteria, String operator) {
		return ispnService.searchIndex(criteria, operator.equalsIgnoreCase("and"));
	}
	
	public Map<String, String> loadData(List<String> ids) throws DataStoreException {
		Map<String, String> messages = new HashMap<String, String>();
		if (ids.size() > 0) {
			for (String msgId : ids) {
				String message = "";
				LOG.debug("Loading message " + msgId);
				
				//Use InfinispanService directly if that's our data store. The reason is that
				//the Infinispan DataStore impl class can't use Infinispan classes because those
				//won't work in a EAP 5x based platform (ie SOA-P 5.3). So it makes a rest call
				//to a service deployed in the UI war that is running in a compatible container
				//(ie tomcat6 or EAP6).
				if (configuration.getDataStoreType().equals(DataStoreType.INFINISPAN)) {
					message = loadFromInfinispan(msgId);
				} else {
					message = Bytes.toString(dataStore.get(msgId));
				}
				
//				try {
//					if (configuration.getDataStoreType().equals(DataStoreType.CASSANDRA)) {
//					    ColumnFamilyResult<String, String> result = cfTemplate.queryColumns(msgId);
//					    message = result.getString("body");
//					} else if (configuration.getDataStoreType().equals(DataStoreType.HBASE)) {
//						try {
//							message = Bytes.toString(messagePersister.readMessage(msgId));
//						} catch (HBaseException ex) {
//							throw new DataStoreException(ex.getMessage(), ex);
//						}
//					} else if (configuration.getDataStoreType().equals(DataStoreType.INFINISPAN)) {
//						try {
//							message = ispnService.loadValue(msgId);
//	//						byte[] messageBytes = (byte[]) ispnDataStore.get(msgId);
//							LOG.debug("  message=" + message);
//	//						message = new String(messageBytes);
//						} catch (InfinispanException ex) {
//							throw new DataStoreException(ex.getMessage(), ex);
//						}
//					} else if (configuration.getDataStoreType().equals(DataStoreType.FILESYSTEM)) {
//						try {
//							message = Bytes.toString(FileSystemDataStore.readData(msgId));
//						} catch (Exception ex) {
//							throw new DataStoreException("Got " + ex.getClass().getName() + " reading " +
//									"data from file system storage: " + ex.getMessage());
//						}
//					}
//				} catch (HectorException ex) {
//				    System.err.println("Got HectorException reading message " + msgId + ": " + ex.getMessage());
//				}
				
				messages.put(msgId, message);
			}
		}
		
		return messages;
	}
	
	public String loadFromInfinispan(String id) throws DataStoreException {
		try {
			String dataStr = ispnService.loadValue(id);
			LOG.debug("Result of loading " + id + " from Infinispan: " + dataStr);
			return dataStr;
		} catch (InfinispanException ex) {
			throw new DataStoreException("Caught InfinispanException loading object with id " + id + ": " + ex.getMessage(), ex);
		}
	}

	public String getFieldOptions(String fieldName, HttpServletRequest request) {
		StringBuffer buf = new StringBuffer();
		buf.append("<select name=\"" + fieldName + "\">");
		buf.append("<option value=\"\"></option>");
		for (int i = 0; i < fields.length; i++) {
			buf.append("<option value=\"" + fields[i] + "\" " + getSelected(fieldName, fields[i], request) + ">" + fields[i] + "</option>");
		}
		buf.append("</select>");
		
		return buf.toString();
	}
	
	private String getSelected(String fieldName, String targetVal, HttpServletRequest request) {
		String usedVal = (String) request.getAttribute(fieldName);
		if (targetVal.equals(usedVal)) {
			return "selected=\"selected\"";
		} else {
			return "";
		}
	}

}
