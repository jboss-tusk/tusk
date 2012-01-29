package org.jboss.tusk.esb.support;

import javax.naming.ConfigurationException;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.soa.esb.Service;
import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.actions.ActionLifecycleException;
import org.jboss.soa.esb.client.ServiceInvoker;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.listeners.ListenerTagNames;
import org.jboss.soa.esb.listeners.message.MessageDeliverException;
import org.jboss.soa.esb.message.Message;

public class ExceptionActionHandler extends AbstractActionLifecycle {

	private static final Log LOG = LogFactory.getLog(ExceptionActionHandler.class);

	protected Service exceptionService;
	
	protected String exceptionServiceCategory;
	protected String exceptionServiceName;
	protected String parentServiceCategory;
	protected String parentServiceName;
	protected boolean allowRetry;
	
	
	public ExceptionActionHandler(ConfigTree config) throws ConfigurationException {
		exceptionServiceCategory = config.getAttribute("exceptionServiceCategory");
		exceptionServiceName = config.getAttribute("exceptionServiceName");
		allowRetry = BooleanUtils.toBoolean(config.getAttribute("allowRetry"));
		
		if((StringUtils.isNotBlank(exceptionServiceCategory)&&StringUtils.isBlank(exceptionServiceName))
				|| (StringUtils.isBlank(exceptionServiceCategory)&&StringUtils.isNotBlank(exceptionServiceName))) {
			throw new ConfigurationException("Service Category and Service Name are required when one is provided.  Current State: "+exceptionServiceCategory+":"+exceptionServiceName);
		}
		
		parentServiceCategory = config.getParent().getAttribute(ListenerTagNames.SERVICE_CATEGORY_NAME_TAG);
		parentServiceName = config.getParent().getAttribute(ListenerTagNames.SERVICE_NAME_TAG);
	}
	

	@Override
	public void initialise() throws ActionLifecycleException {
		if(StringUtils.isNotBlank(exceptionServiceCategory)&&StringUtils.isNotBlank(exceptionServiceName))
		{
			exceptionService = new Service(exceptionServiceCategory, exceptionServiceName);
		}
	}
	
	public Message process(Message message) {
		return message;
	}

	public void exceptionHandler(Message message, Throwable t) {
		if(exceptionService!=null)
		{
			try {
				ServiceInvoker invoker = new ServiceInvoker(exceptionService);
				
				//Create the shortened stack trace.
				String exceptionStack = ExceptionUtils.getMessage(t);
				exceptionStack +="\n\nRoot Cause: "+ExceptionUtils.getRootCause(t);
				
				//Append the settings to the message.
				message.getBody().add("exceptionTrace", exceptionStack);
				message.getBody().add("sourceServiceName", parentServiceName);
				message.getBody().add("sourceServiceCategory", parentServiceCategory);
				message.getBody().add("allowRetry", Boolean.toString(allowRetry));

				invoker.deliverAsync(message);
				
			} catch (MessageDeliverException e) {
				LOG.error("Exception delivering to service "+exceptionService.getCategory()+":"+exceptionService.getName(),e);
			}
		}
		else
		{
			LOG.error("Exception in service: "+parentServiceCategory+":"+parentServiceName,t);
		}
		
	}
}
