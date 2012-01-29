package org.jboss.tusk.esb;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import javax.naming.NamingException;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
import org.drools.KnowledgeBase;
import org.drools.agent.KnowledgeAgent;
import org.drools.runtime.StatelessKnowledgeSession;
import org.drools.support.BigDataIndex;
import org.drools.support.xml.XmlMessagePayload;
import org.jboss.tusk.common.TuskCassandraConfiguration;
import org.jboss.tusk.common.TuskConfiguration;
import org.jboss.tusk.common.DataStore;
import org.jboss.tusk.common.TuskHBaseConfiguration;
import org.jboss.tusk.hadoop.HBaseException;
import org.jboss.tusk.hadoop.HBaseFacade;
import org.jboss.tusk.hadoop.service.MessagePersister;
import org.jboss.tusk.monitoring.BigDataMonitorManagement;
import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.actions.ActionLifecycleException;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;

//import org.jboss.tusk.ispn.InfinispanException;
//import org.jboss.tusk.ispn.InfinispanService;

public class BigDataExtractor extends JndiBaseActionHandler<BigDataMonitorManagement> {

	private static final TuskConfiguration configuration = new TuskConfiguration();

	private static final Log LOG = LogFactory.getLog(BigDataExtractor.class);
	private static final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	private KnowledgeAgent kagent;
	private DocumentBuilder parser;

//	private static InfinispanService ispnService = null;
	
	//for Cassandra
	//TODO should these be static?
	private static TuskCassandraConfiguration tuskCassConf = new TuskCassandraConfiguration();
	private static Cluster dataCluster = null;
	private static Keyspace ksp = null;
	private static ColumnFamilyTemplate<String, String> cfTemplate = null;
	
	//for HBase
	private MessagePersister messagePersister = null;
	
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
		}
		
//		System.setProperty("java.net.preferIPv4Stack", "true");
//		ispnService = new InfinispanService();
	}
	
	public BigDataExtractor(ConfigTree config) throws ConfigurationException {
		super(config);
		if (configuration.getDataStore().equals(DataStore.CASSANDRA)) {
			//init is in static block
		} else if (configuration.getDataStore().equals(DataStore.HBASE)) {
			messagePersister = new MessagePersister();
		}
	}
	
	@Override
	public void initialise() throws ActionLifecycleException {
		//create document parser.
		factory.setNamespaceAware(true);
		try {
			parser = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new ActionLifecycleException("Exception building parser.", e);
		}
	}
	
	public Message process(Message message) throws ActionProcessingException {
		
		Document document = null;
		byte[] messageBodyBytes = null;
		
		Object messageBody = message.getBody().get();
		if(messageBody instanceof byte[])
		{
			messageBodyBytes = (byte[])messageBody;
			
			//parse document.
			ByteArrayInputStream tmp = new ByteArrayInputStream(messageBodyBytes);
			try {
				document = parser.parse(tmp);
			} catch (Exception e) {
				throw new ActionProcessingException("Exception parsing document.", e);
			} 
		}
		else if(messageBody instanceof String) {
			messageBodyBytes = ((String)messageBody).getBytes();
			
			//parse document.
			ByteArrayInputStream tmp = new ByteArrayInputStream(messageBodyBytes);
			try {
				document = parser.parse(tmp);
			} catch (Exception e) {
				throw new ActionProcessingException("Exception parsing document.", e);
			} 
		}
		else if(messageBody instanceof Document) 
		{
			//just cast.
			document = (Document)messageBody;
			
			//get a byte[] from the document - TODO need to test this
			try {
				messageBodyBytes = nodeToString(document.getDocumentElement()).getBytes();
			} catch (TransformerException e) {
				throw new ActionProcessingException("Exception parsing document.", e);
			}
		}
		
		if(document!=null)
		{
			LOG.debug("Document not null.");
			XmlMessagePayload payload = new XmlMessagePayload(document);
			
			//extract the index.
			StatelessKnowledgeSession ss = getKnowledgeBase().newStatelessKnowledgeSession();
			ss.execute(payload);
			
			Map<String, Object> indexFields = new HashMap<String, Object>();
			if(payload.getIndexes() != null)
			{
				for(BigDataIndex<?> bdi : payload.getIndexes())
				{
					LOG.info("Extracted index: "+bdi);
					indexFields.put(bdi.getKey(), bdi.getValue());
				}
			}

			//persist the message and indexes
			String messageKey = UUID.randomUUID().toString();
	
			//save the message
			LOG.info("About to write message body for key " + messageKey);
			writePayload(messageKey, messageBodyBytes);
		    LOG.info("Done writing message body for key " + messageKey);

		    //now write indexes
			LOG.info("About to write message indexes for key " + messageKey);
			writeIndexes(indexFields, messageKey);

			try {
				getBean().addBytes(messageBodyBytes.length);
				getBean().addMessage();
			} catch (NamingException e) {
				LOG.error("Exception finding EJB.",e);
			}
			
		}
		else
		{
			LOG.warn("Did not find document.");
		}
		
		return message;
	}

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
		}
	}
	
	private void writeIndexes(Map<String, Object> indexFields, String messageKey) throws ActionProcessingException {
//		try {
//			ispnService.writeIndex(messageKey, indexFields);
//		} catch (InfinispanException ex) {
//			throw new ActionProcessingException("Exception happened while " +
//			"writing index to Infinispan cache.", ex);
//		}
		
		Form f = new Form();
		f.add("indexes", serializeMap(indexFields));
		Client c = Client.create();
		WebResource r = c.resource("http://localhost:8888/TuskUI/rest/indexer/add/" + messageKey);
		String indexResponse = r.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).accept(MediaType.TEXT_PLAIN).post(String.class, f);
	    LOG.info("Done writing message indexes for key " + messageKey + "; response was " + indexResponse);
	}

	private KnowledgeBase getKnowledgeBase() {
		if(kagent==null)
		{
			kagent = KnowledgeAgentSingleton.getInstance().getKagent();
		}
		
		/*
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		kbuilder.add( ResourceFactory.newClassPathResource("xpath-rules.drl"), ResourceType.DRL);
		if ( kbuilder.hasErrors() ) {
			throw new ActionLifecycleException("Error building rules: "+kbuilder.getErrors().toString());
		}
		*/
		
		
		return kagent.getKnowledgeBase();
	}
	
	private static String nodeToString(Node node) throws TransformerException {
		StringWriter sw = new StringWriter();
		Transformer t = TransformerFactory.newInstance().newTransformer();
		t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		t.transform(new DOMSource(node), new StreamResult(sw));
	
		return sw.toString();
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
