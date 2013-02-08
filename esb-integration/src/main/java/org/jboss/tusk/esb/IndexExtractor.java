package org.jboss.tusk.esb;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.actions.ActionLifecycleException;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;
import org.jboss.tusk.exception.IntakeException;
import org.jboss.tusk.intake.IntakeHelper;

/**
 * The IndexExtractor class is responsible for extracting indexes from the input document
 * and storing them in the message for later persistence.
 * 
 * @author jhayes
 *
 */
public class IndexExtractor extends AbstractActionLifecycle {

	private static final Log LOG = LogFactory.getLog(IndexExtractor.class);

	public IndexExtractor(ConfigTree config) throws ConfigurationException {
		super();
	}
	
	@Override
	public void initialise() throws ActionLifecycleException {
		//do nothing
	}
	
	public Message process(Message message) throws ActionProcessingException {
		Object payload = message.getBody().get("payload");
		
		if (payload == null) {
			LOG.warn("Could not find payload in message.");
			return null;
		}
		
		try {
			Map<String, Object> indexes = IntakeHelper.extractIndexes(payload);
			if (indexes != null && MapUtils.isNotEmpty(indexes)) {
				message.getBody().add("indexes", indexes);
			}
		} catch (IntakeException ex) {
			throw new ActionProcessingException("Caught IntakeException extracting indexes: " + ex.getMessage(), ex);
		}
		
		//even if there were no indexes we want to return the message so it gets persisted later
		return message;
	}
	
}
