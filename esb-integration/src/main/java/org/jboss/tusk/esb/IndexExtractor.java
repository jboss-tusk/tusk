package org.jboss.tusk.esb;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.drools.KnowledgeBase;
import org.drools.agent.KnowledgeAgent;
import org.drools.runtime.StatelessKnowledgeSession;
import org.drools.support.BigDataIndex;
import org.drools.support.xml.XmlMessagePayload;
import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.actions.ActionLifecycleException;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;
import org.w3c.dom.Document;

/**
 * The IndexExtractor class is responsible for extracting indexes from the input document
 * and storing them in the message for later persistence.
 * 
 * @author jhayes
 *
 */
public class IndexExtractor extends AbstractActionLifecycle {

	private static final Log LOG = LogFactory.getLog(IndexExtractor.class);
	private KnowledgeAgent kagent;

	public IndexExtractor(ConfigTree config) throws ConfigurationException {
		super();
	}
	
	@Override
	public void initialise() throws ActionLifecycleException {
		//do nothing
	}
	
	public Message process(Message message) throws ActionProcessingException {
		Document document = (Document) message.getBody().get("payloadDocument");
		
		XmlMessagePayload payload = new XmlMessagePayload(document);
		
		//extract the indexes
		StatelessKnowledgeSession ss = getKnowledgeBase().newStatelessKnowledgeSession();
		ss.execute(payload);
		
		
		if (payload.getIndexes() != null) {
			Map<String, Object> indices = new HashMap<String, Object>();
			for (BigDataIndex<?> bdi : payload.getIndexes()) {
				LOG.info("Extracted index: " + bdi);
				indices.put(bdi.getKey(), bdi.getValue());
			}

			//store the indexes in the message for further processing
			message.getBody().add("indices", indices);
		}
		
		//even if there were no indices we want to return the message so it gets persisted later
		return message;
	}

	/**
	 * This method performs a check to see if a knowledge agent already exists.  If not, the
	 * knowledge agent is created and it's knowledge base is returned to the caller.  The
	 * knowledge agent automatically loads the changeset configured in indexing-changeset.xml.
	 * 
	 * @return
	 */
	private KnowledgeBase getKnowledgeBase() {
		if (kagent==null) {
			kagent = KnowledgeAgentSingleton.getInstance().getKagent();
		}
		
//		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
//		kbuilder.add( ResourceFactory.newClassPathResource("xpath-rules.drl"), ResourceType.DRL);
//		if ( kbuilder.hasErrors() ) {
//			throw new ActionLifecycleException("Error building rules: "+kbuilder.getErrors().toString());
//		}
		
		return kagent.getKnowledgeBase();
	}
	
}
