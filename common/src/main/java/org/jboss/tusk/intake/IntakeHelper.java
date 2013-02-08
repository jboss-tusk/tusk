package org.jboss.tusk.intake;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.drools.KnowledgeBase;
import org.drools.agent.KnowledgeAgent;
import org.drools.runtime.StatelessKnowledgeSession;
import org.drools.support.BigDataIndex;
import org.drools.support.MessagePayload;
import org.drools.support.json.JsonMessagePayload;
import org.drools.support.xml.XmlMessagePayload;
import org.jboss.tusk.datastore.DataStore;
import org.jboss.tusk.datastore.DataStoreFactory;
import org.jboss.tusk.exception.DataStoreException;
import org.jboss.tusk.exception.IntakeException;
import org.w3c.dom.Document;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;

/**
 * This class contains methods related to the intake of data in the tusk system. This functionality is separated
 * into this class so that a variety of intake mechanisms can be used (ie ESB service, web service, web app, etc).
 * @author jhayes
 *
 */
public class IntakeHelper {

	private static final Log LOG = LogFactory.getLog(IntakeHelper.class);
	
	private static final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	
	public static PayloadAndBytes extractPayload(Object container) throws IntakeException {
		byte[] bytes = null;
		Object payload = null;
		
		if(container instanceof byte[]) {
			bytes = (byte[])container;
			//TODO not sure if this will work; or if we'll ever have a byte[] in the message body.
			payload = getPayload(new String(bytes), bytes);
		} else if(container instanceof String) {
			bytes = ((String)container).getBytes();
			payload = getPayload((String)container, bytes);
		}
		
		return new PayloadAndBytes(payload, bytes);
	}
	
	public static Map<String, Object> extractIndexes(Object payload) throws IntakeException {
		if (payload == null) {
			throw new IntakeException("Could not find payload in message.");
		}
		
		//get the actual message payload
		MessagePayload messagePayload = null;
		if (payload instanceof Document) {
			//it's an XML document
			Document document = (Document)payload;
			messagePayload = new XmlMessagePayload(document);
		} else if (payload instanceof String) {
			//it's a JSON string
			messagePayload = new JsonMessagePayload((String)payload);
		}
		
		//extract the indexes
		StatelessKnowledgeSession ss = getKnowledgeBase().newStatelessKnowledgeSession();
		ss.execute(messagePayload);

		Map<String, Object> indexes = new HashMap<String, Object>();
		if (messagePayload.getIndexes() != null && messagePayload.getIndexes().size() > 0) {
			Iterator<BigDataIndex<?>> iter = messagePayload.getIndexes().iterator();
			while (iter.hasNext()) {
				BigDataIndex<?> bdi = (BigDataIndex<?>)iter.next();
				LOG.info("Extracted index: " + bdi);
				indexes.put(bdi.getKey(), bdi.getValue());
			}
			
//			for (BigDataIndex<?> bdi : messagePayload.getIndexes()) {
//				LOG.info("Extracted index: " + bdi);
//				indexes.put(bdi.getKey(), bdi.getValue());
//			}
		}
		
		return indexes;
	}
	
	public static void writePayloadAndIndexes(byte[] bytes, Map<String, Object> indexes) throws IntakeException {
		//come up with a unique id
		String messageKey = UUID.randomUUID().toString();

		//save the message
		DataStore dataStore = null;
		try {
			dataStore = DataStoreFactory.getInstance().getDataStore();
		} catch (DataStoreException ex) {
			throw new IntakeException("Caught exception creating DataStore object: " + ex.getMessage(), ex);
		}

		LOG.info("About to write message body for key " + messageKey);
		try {
			dataStore.put(messageKey, bytes);
		} catch (DataStoreException ex) {
			throw new IntakeException("Caught exception putting data in data store: " + ex.getMessage(), ex);
		}
//		writePayload(messageKey, messageBodyBytes);
	    LOG.info("Done writing message body for key " + messageKey);

	    //now write indexes
	    if (indexes != null && indexes.size() > 0) {
			LOG.info("About to write message indexes for key " + messageKey);
			writeIndexes(indexes, messageKey);
		    LOG.info("Done writing message indexess for key " + messageKey);
	    }
		
	}
	
	/**
	 * Returns the payload contained within the message. It figures out whether the message
	 * contains an XML payload or a JSON payload. If it starts with "{" it's assumed to be
	 * JSON. Otherwise it's treated as XML. 
	 * 
	 * TODO make this decision finer grained to assume if it starts with "<" it's XML, and
	 * that way it can handle other formats.
	 * 
	 * @param str
	 * @param bytes
	 * @return
	 * @throws IntakeException
	 */
	private static Object getPayload(String str, byte[] bytes) throws IntakeException {
		if (StringUtils.isEmpty(str)) {
			return null;
		}
		
		Object payload = null;
		
		if (str.startsWith("{")) {
			//it's JSON, so just return the string
			payload = str;
		} else {
			//assume it's XML, so parse the document and return it
			ByteArrayInputStream tmp = new ByteArrayInputStream(bytes);
			try {
				//create document parser.
				factory.setNamespaceAware(true);
				DocumentBuilder parser = factory.newDocumentBuilder();
				payload = parser.parse(tmp);
			} catch (ParserConfigurationException e) {
				throw new IntakeException("Exception building parser.", e);
			} catch (Exception e) {
				throw new IntakeException("Exception parsing document.", e);
			} 
		}
		
		return payload;
	}
	
	/**
	 * The writeIndexes operation writes the indexes extracted from the input message to the configured
	 * data store.  Currently, the indexes are stored by calling a REST service. This is done because
	 * I haven't gotten it working yet where the ispn data grid can be collocated with SOA-P 5.x.
	 * 
	 * @param indexFields
	 * @param messageKey
	 * @throws IntakeException
	 */
	private static void writeIndexes(Map<String, Object> indexFields, String messageKey) throws IntakeException {
//		try {
//			ispnService.writeIndex(messageKey, indexFields);
//		} catch (InfinispanException ex) {
//			throw new IntakeException("Exception happened while " +
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
	private static String serializeMap(Map<String, Object> map) {
		StringBuffer buf = new StringBuffer();
		for (Entry<String, Object> entry : map.entrySet()) {
			buf.append(entry.getKey() + ":" + entry.getValue() + "|");
		}
		buf.delete(buf.length()-1, buf.length());
		
		return buf.toString();
	}

	/**
	 * This method performs a check to see if a knowledge agent already exists.  If not, the
	 * knowledge agent is created and it's knowledge base is returned to the caller.  The
	 * knowledge agent automatically loads the changeset configured in indexing-changeset.xml.
	 * 
	 * @return
	 */
	private static KnowledgeBase getKnowledgeBase() {
		KnowledgeAgent kagent = KnowledgeAgentSingleton.getInstance().getKagent();
		
//		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
//		kbuilder.add( ResourceFactory.newClassPathResource("xpath-rules.drl"), ResourceType.DRL);
//		if ( kbuilder.hasErrors() ) {
//			throw new ActionLifecycleException("Error building rules: "+kbuilder.getErrors().toString());
//		}
		
		return kagent.getKnowledgeBase();
	}

}
