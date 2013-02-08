package org.jboss.tusk.ui;


import java.util.Properties;

import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;


public class PopulateController extends AbstractTuskController {
	
	private static final Log LOG = LogFactory.getLog(PopulateController.class);
	
	private static String JMS_PROVIDER_URL = null;
	
	static {
		JMS_PROVIDER_URL = System.getProperty("jms.provider.jndi", "localhost:1099");
	}
	
	public PopulateController() {
	}
	
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest req,
			HttpServletResponse res) throws Exception {

		ModelAndView mav = new ModelAndView("populate");

		String addOne = (String)req.getParameter("addOne");
		String addMany = (String)req.getParameter("addMany");
		String addPayload = (String)req.getParameter("addPayload");
		String payloadJson = (String)req.getParameter("payloadJson");
		String payloadXml = (String)req.getParameter("payloadXml");
		String howMany = (String)req.getParameter("howMany");
		String payloadType = (String)req.getParameter("payloadType");
		
		if (StringUtils.isEmpty(payloadType)) {
			payloadType = PopulateHelper.PAYLOAD_TYPE_JSON;
		}
		
		String numAdded = null;
		if (StringUtils.equals(addOne, PopulateHelper.ADD_ONE_LABEL)) {
			numAdded = "1";
			LOG.info("Adding 1 random message.");
			doPopulateViaDriver(1, payloadType);
		} else if (StringUtils.equals(addMany, PopulateHelper.ADD_MANY_LABEL)) {
			numAdded = howMany;
			LOG.info("Adding " + numAdded + " random message(s).");
			doPopulateViaDriver(Integer.valueOf(howMany), payloadType);
		} else if (StringUtils.equals(addPayload, PopulateHelper.ADD_PAYLOAD_LABEL)) {
			numAdded = "1";
			LOG.info("Adding 1 message, provided by the user.");
			String payloadToUse = null;
			if (payloadType.equals(PopulateHelper.PAYLOAD_TYPE_JSON)) {
				payloadToUse = payloadJson;
			} else {
				payloadToUse = payloadXml;
			}
			doPopulate(1, payloadToUse, payloadType);
		}
		
		mav.addObject("numAdded", numAdded);
		mav.addObject("payloadType", payloadType);
		
		return mav;
	}

	private void doPopulate(int numToAdd, String payload, String payloadType) throws Exception {
		//JMS init
		InitialContext initCtx = getInitialContext();
		QueueConnectionFactory qcf = (QueueConnectionFactory) initCtx.lookup("ConnectionFactory");
		QueueConnection conn = qcf.createQueueConnection();
		QueueSession queueSession = conn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
		Queue requestQueueRef = (Queue) initCtx.lookup("queue/DocumentEntry");
		QueueSender sender = queueSession.createSender(requestQueueRef);
		
		//send the messages
		PopulateHelper helper = new PopulateHelper();
		for (int i = 0; i < numToAdd; i++) {
			String body = payload;
			if (StringUtils.isEmpty(body)) {
				body = payloadType.equalsIgnoreCase(helper.PAYLOAD_TYPE_XML) ? 
						helper.getRandomXmlPayload() :
							helper.getRandomJsonPayload();
			}
			ObjectMessage sendMsg = queueSession.createObjectMessage(body);
			sender.send(sendMsg);
			LOG.debug("Sent message.");
		}

		//clean up
		try {
			if (sender != null) {
				sender.close();
			}
			if (queueSession != null) {
				queueSession.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (Exception ex) {
			LOG.warn("Caught " + ex.getClass().getName() + " cleaning up JMS objects: " + 
					ex.getMessage() + ". This is not critical, so continuing work. It should be looked into though.", ex);
		}
		
		LOG.info("Finished adding " + numToAdd + " message(s)");
	}

	private void doPopulateViaDriver(int numToAdd, String payloadType) throws Exception {
		//JMS init
		InitialContext initCtx = getInitialContext();
		QueueConnectionFactory qcf = (QueueConnectionFactory) initCtx.lookup("ConnectionFactory");
		QueueConnection conn = qcf.createQueueConnection();
		QueueSession queueSession = conn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
		Queue requestQueueRef = (Queue) initCtx.lookup("queue/DocumentDriver");
		QueueSender sender = queueSession.createSender(requestQueueRef);
		
		//send the message
		String body = numToAdd + "," + payloadType;
		ObjectMessage sendMsg = queueSession.createObjectMessage(body);
		sender.send(sendMsg);
		LOG.debug("Sent driver message.");

		//clean up
		try {
			if (sender != null) {
				sender.close();
			}
			if (queueSession != null) {
				queueSession.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (Exception ex) {
			LOG.warn("Caught " + ex.getClass().getName() + " cleaning up JMS objects: " + 
					ex.getMessage() + ". This is not critical, so continuing work. It should be looked into though.", ex);
		}
		
		LOG.info("Finished sending driver to add " + numToAdd + " " + payloadType + " message(s)");
	}
	
	private InitialContext getInitialContext() throws Exception {
		Properties props = new Properties();
		props.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                        "org.jnp.interfaces.NamingContextFactory");
		props.setProperty(Context.PROVIDER_URL,
                        "jnp://" + JMS_PROVIDER_URL);
		props.setProperty(Context.URL_PKG_PREFIXES,
        				"org.jnp.interfaces");
		return new InitialContext(props);
	}

	private QueueSession getQueueSession() throws Exception {
		InitialContext iniCtx = getInitialContext();
		Object tmp = iniCtx.lookup("ConnectionFactory");
		QueueConnectionFactory qcf = (QueueConnectionFactory) tmp;
		QueueConnection conn = qcf.createQueueConnection();
		return conn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
	}

}
