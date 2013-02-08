package org.jboss.tusk.esb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.actions.ActionLifecycleException;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;

/**
 * This action either records starting time or outputs ending time (if the
 * start has already been recorded.
 *  
 * @author jhayes
 *
 */
public class TimeRecorder extends AbstractActionLifecycle {

	private static final Log LOG = LogFactory.getLog(TimeRecorder.class);
	
	private String timerType = null;

	public TimeRecorder(ConfigTree config) throws ConfigurationException {
		super();
		timerType = config.getRequiredAttribute("timerType");
	}
	
	@Override
	public void initialise() throws ActionLifecycleException {
		//do nothing
	}
	
	public Message process(Message message) throws ActionProcessingException {
		Long start = (Long)message.getBody().get("start");
		if (start == null) {
			//this is the first time it's been called
			message.getBody().add("start", new Long(System.currentTimeMillis()));
			message.getBody().add("timerType", timerType);
		} else {
			//this is the second time it's been called
			message.getBody().remove("start");
			String type = (String)message.getBody().get("timerType");
			message.getBody().remove("timerType");
			long total = System.currentTimeMillis() - start;
			LOG.info("***" + type + ": Message " + message.getHeader().getCall().getMessageID() + " took " + total + " ms.");
		}
		
		return message;
	}
	
}
