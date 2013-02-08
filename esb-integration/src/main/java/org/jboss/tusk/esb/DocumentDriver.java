package org.jboss.tusk.esb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.actions.ActionLifecycleException;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.client.ServiceInvoker;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;
import org.jboss.soa.esb.message.format.MessageFactory;
import org.jboss.tusk.jms.utility.MessageStubUtility;

/**
 * This action simulates a number of messages arriving nearly simultaneously 
 * to be handled by the intaker.
 *  
 * @author jhayes
 *
 */
public class DocumentDriver extends AbstractActionLifecycle {

	private static final Log LOG = LogFactory.getLog(DocumentDriver.class);
	
	public DocumentDriver(ConfigTree config) throws ConfigurationException {
		super();
	}
	
	@Override
	public void initialise() throws ActionLifecycleException {
		//do nothing
	}
	
	public Message process(Message message) throws ActionProcessingException {
		String body = (String)message.getBody().get();
		String[] parts = body.split(",");
		
		int num = 1;
		
		try {
			num = Integer.parseInt(parts[0]);
		} catch (NumberFormatException ex) {
			LOG.warn("Couldn't format " + parts[0] + " into a number. Just doing 1 message.");
		}
		
		String payloadType = parts[1];
		
		LOG.info("Delivering " + num + " " + payloadType + " messages.");
		
		try {
			MessageStubUtility msu = new MessageStubUtility();
			ServiceInvoker si = new ServiceInvoker("jboss-bigdata", "DocumentEntry");
			
			for (int i = 0; i < num; i++) {
				Message reqMessage = MessageFactory.getInstance().getMessage();
				String payload = payloadType.equals("XML") ? msu.stubXmlMessage() : msu.stubJsonMessage();
				reqMessage.getBody().add(payload);
				si.deliverAsync(reqMessage);
			}
		} catch (Exception ex) {
			LOG.error("Caught " + ex.getClass().getName() + ": " + ex.getMessage(), ex);
		}

		LOG.info("Finished delivering " + num + " messages.");
		
		return message;
	}
	
}
