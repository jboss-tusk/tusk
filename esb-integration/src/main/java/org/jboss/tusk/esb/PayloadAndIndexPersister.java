package org.jboss.tusk.esb;

import java.util.Map;

import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.actions.ActionLifecycleException;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;
import org.jboss.tusk.exception.IntakeException;
import org.jboss.tusk.intake.IntakeHelper;
import org.jboss.tusk.monitoring.BigDataMonitorManagement;


/**
 * This class is responsible for persisting the extracted payload and indexes into the appropriate
 * storage location. For the indexes that is the index storage grid. For the payload that is some
 * form of disk-based storage, currently either Cassandra, HBase, or Infinispan.
 *
 * @author jhayes
 *
 */
public class PayloadAndIndexPersister extends JndiBaseActionHandler<BigDataMonitorManagement> {

	private static final Log LOG = LogFactory.getLog(PayloadAndIndexPersister.class);
	
	public PayloadAndIndexPersister(ConfigTree config) throws ConfigurationException {
		super(config);
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
		//retrieve the document and indexes that were stored earlier
		byte[] payloadBytes = (byte[]) message.getBody().get("payloadBytes");
		Map<String, Object> indexes = (Map<String, Object>)message.getBody().get("indexes");
		
		try {
			IntakeHelper.writePayloadAndIndexes(payloadBytes, indexes);
		} catch (IntakeException ex) {
			throw new ActionProcessingException("Caught IntakeException writing payload and indexes: " + ex.getMessage(), ex);
		}

		//record stats in mbean
		try {
			getBean().addBytes(payloadBytes.length);
			getBean().addMessage();
		} catch (NamingException e) {
			LOG.error("Exception finding EJB.",e);
		}
		
		return message;
	}
}
