package org.jboss.tusk.esb;

import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.naming.NamingException;
import javax.ws.rs.core.MediaType;

import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.template.ColumnFamilyTemplate;
import me.prettyprint.cassandra.service.template.ColumnFamilyUpdater;
import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.factory.HFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.actions.ActionLifecycleException;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;
import org.jboss.tusk.common.DataStore;
import org.jboss.tusk.common.TuskCassandraConfiguration;
import org.jboss.tusk.common.TuskConfiguration;
import org.jboss.tusk.hadoop.HBaseException;
import org.jboss.tusk.hadoop.service.MessagePersister;
import org.jboss.tusk.monitoring.BigDataMonitorManagement;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;


/**
 * This class is responsible for persisting the extracted payload and indices into the appropriate
 * storage location. For the indices that is the index storage grid. For the payload that is some
 * form of disk-based storage, currently either Cassandra, HBase, or Infinispan.
 *
 * @author jhayes
 *
 */
public class PayloadAndIndexPersister extends JndiBaseActionHandler<BigDataMonitorManagement> {

	private static final TuskConfiguration configuration = new TuskConfiguration();

	private static final Log LOG = LogFactory.getLog(PayloadAndIndexPersister.class);

	//private static InfinispanService ispnService = null;
	
	//for Cassandra
	//TODO should these be static?
	private static TuskCassandraConfiguration tuskCassConf = new TuskCassandraConfiguration();
	private static Cluster dataCluster = null;
	private static Keyspace ksp = null;
	private static ColumnFamilyTemplate<String, String> cfTemplate = null;
	
	//for HBase
	private MessagePersister messagePersister = null;
	
	//for Infinispan
	//private static Cache<Object, Object> ispnDataStore = null;
	
	static {
		if (configuration.getDataStore().equals(DataStore.CASSANDRA)) {
			//TODO make a CassandraFacade class that encapsulates all the cassandra stuff
			dataCluster = HFactory.getOrCreateCluster("data-cluster", tuskCassConf.getCluster());
			LOG.debug("Hector dataCluster=" + dataCluster);
			
			// This is the keyspace to use for the data we are storing
			KeyspaceDefinition keyspaceDef = dataCluster.describeKeyspace(tuskCassConf.getKeyspace());
			LOG.debug("Hector keyspaceDef=" + keyspaceDef);
			
			ksp = HFactory.createKeyspace(tuskCassConf.getKeyspace(), dataCluster);
			LOG.debug("Hector keyspace=" + ksp);
			
			cfTemplate = new ThriftColumnFamilyTemplate<String, String>(
					ksp, tuskCassConf.getColumnFamily(), StringSerializer.get(), StringSerializer.get());
			LOG.debug("Hector cfTemplate=" + cfTemplate);
		} else if (configuration.getDataStore().equals(DataStore.HBASE)) {
			//init is in ctor
		} else if (configuration.getDataStore().equals(DataStore.INFINISPAN)) {
			//nothing to do
		}
		
		//System.setProperty("java.net.preferIPv4Stack", "true");
		//ispnService = new InfinispanService();
	}
	
	public PayloadAndIndexPersister(ConfigTree config) throws ConfigurationException {
		super(config);
		if (configuration.getDataStore().equals(DataStore.CASSANDRA)) {
			//init is in static block
		} else if (configuration.getDataStore().equals(DataStore.HBASE)) {
			messagePersister = new MessagePersister();
		} else if (configuration.getDataStore().equals(DataStore.INFINISPAN)) {
			//init is in static block
		}
	}
	
	@Override
	public void initialise() throws ActionLifecycleException {
		//do nothing
	}
	
	/**
	 * The process operation takes in an ESB message, does some processing, and returns the message
	 * up the action pipeline.  The method expects that the information sent in is in XML format and
	 * throws an exception if the information is unable to be parsed.  For properly formatted input,
	 * the data is converted to DOM format and rules are run against this to extract the payload and 
	 * indexes.  The information is then written to the data store.
	 * 
	 * @param message
	 */
	public Message process(Message message) throws ActionProcessingException {
		//retrieve the document and indices that were stored earlier
		byte[] messageBodyBytes = (byte[]) message.getBody().get("payloadBytes");
		Map<String, Object> indices = (Map<String, Object>)message.getBody().get("indices");
		
		//persist the message and indexes
		String messageKey = UUID.randomUUID().toString();

		//save the message
		LOG.info("About to write message body for key " + messageKey);
		writePayload(messageKey, messageBodyBytes);
	    LOG.info("Done writing message body for key " + messageKey);

	    //now write indexes
		LOG.info("About to write message indices for key " + messageKey);
		writeIndexes(indices, messageKey);

		//record stats in mbean
		try {
			getBean().addBytes(messageBodyBytes.length);
			getBean().addMessage();
		} catch (NamingException e) {
			LOG.error("Exception finding EJB.",e);
		}
		
		return message;
	}

	/**
	 * The writePayload operation writes the payloads extracted from the input message to the configured
	 * data store.  Currently, the payloads are stored by calling a REST service.
	 * 
	 * @param messageKey
	 * @param messageBodyBytes
	 * @throws ActionProcessingException
	 */
	private void writePayload(String messageKey, byte[] messageBodyBytes) throws ActionProcessingException {
		if (configuration.getDataStore().equals(DataStore.CASSANDRA)) {
			ColumnFamilyUpdater<String, String> updater = cfTemplate.createUpdater(messageKey);
			updater.setByteArray("body", messageBodyBytes);
			updater.setLong("timestamp", System.currentTimeMillis());
			
			try {
				cfTemplate.update(updater);
			} catch (HectorException ex) {
				throw new ActionProcessingException("Got HectorException writing " +
						"message to data storage: " + ex.getMessage());
			}
		} else if (configuration.getDataStore().equals(DataStore.HBASE)) {
			try {
				//TODO need to write timestamp too?
				messagePersister.writeMessage(messageKey, messageBodyBytes);
			} catch (HBaseException ex) {
				throw new ActionProcessingException("Got HBaseException writing " +
						"message to data storage: " + ex.getMessage());
			}
		} else if (configuration.getDataStore().equals(DataStore.INFINISPAN)) {
//			try {
//				ispnService.writeValue(messageKey, messageBodyBytes);
//			} catch (InfinispanException ex) {
//				throw new ActionProcessingException("Exception happened while " +
//				"writing payload to Infinispan grid.", ex);
//			}
			
			//use the tomcat rest service
			Form f = new Form();
			f.add("value", new String(messageBodyBytes));
			Client c = Client.create();
			WebResource r = c.resource("http://localhost:8888/TuskUI/rest/indexer/store/");
			String indexResponse = r.path(messageKey).type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).post(String.class, f);
		    LOG.info("Done storing value for key " + messageKey + "; response was " + indexResponse);
		}
	}
	
	/**
	 * The writeIndexes operation writes the indexes extracted from the input message to the configured
	 * data store.  Currently, the indexes are stored by calling a REST service.
	 * 
	 * @param indexFields
	 * @param messageKey
	 * @throws ActionProcessingException
	 */
	private void writeIndexes(Map<String, Object> indexFields, String messageKey) throws ActionProcessingException {
//		try {
//			ispnService.writeIndex(messageKey, indexFields);
//		} catch (InfinispanException ex) {
//			throw new ActionProcessingException("Exception happened while " +
//			"writing index to Infinispan cache.", ex);
//		}

		//use the tomcat rest service
		Form f = new Form();
		f.add("indexes", serializeMap(indexFields));
		Client c = Client.create();
		WebResource r = c.resource("http://localhost:8888/TuskUI/rest/indexer/add/" + messageKey);
		String indexResponse = r.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).accept(MediaType.TEXT_PLAIN).post(String.class, f);
	    LOG.info("Done writing message indexes for key " + messageKey + "; response was " + indexResponse);
	}

	//There's surely a better way to do this, but for now...
	private String serializeMap(Map<String, Object> map) {
		StringBuffer buf = new StringBuffer();
		for (Entry<String, Object> entry : map.entrySet()) {
			buf.append(entry.getKey() + ":" + entry.getValue() + "|");
		}
		buf.delete(buf.length()-1, buf.length());
		
		return buf.toString();
	}
}
