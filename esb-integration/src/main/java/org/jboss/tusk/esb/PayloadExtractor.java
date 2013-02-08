package org.jboss.tusk.esb;

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
import org.jboss.tusk.intake.PayloadAndBytes;

/**
 * The PaylodExtractor is responsible for the extraction of Big Data input that comes into Tusk's 
 * via the DocumentEntry queue.
 * 
 * @author jhayes
 *
 */
public class PayloadExtractor extends AbstractActionLifecycle {

	private static final Log LOG = LogFactory.getLog(PayloadExtractor.class);
		
	public PayloadExtractor(ConfigTree config) throws ConfigurationException {
		super();
	}
	
	@Override
	public void initialise() throws ActionLifecycleException {
	}
	
	/**
	 * The process operation takes in an ESB message, extracts the payload, turns it into an XML DOM 
	 * document, and then adds the document to the message. For further processing by other actions.
	 * 
	 * @param message
	 */
	public Message process(Message message) throws ActionProcessingException {
		PayloadAndBytes payloadAndBytes = null;
		try {
			payloadAndBytes = IntakeHelper.extractPayload(message.getBody().get());
		} catch (IntakeException ex) {
			throw new ActionProcessingException("Caught IntakeException extracting payload and bytes: " + ex.getMessage(), ex);
		}
		
		if (payloadAndBytes != null) {
			if (payloadAndBytes.getPayload() != null) {
				message.getBody().add("payload", payloadAndBytes.getPayload());
			}
			if (payloadAndBytes.getBytes() != null) {
				message.getBody().add("payloadBytes", payloadAndBytes.getBytes());
			}
			return message;
		} else {
			//return null so the pipeline stops
			LOG.warn("Did not find payload in message.");
			return null;
		}
	}
	
}
