package org.jboss.tusk.esb.support;

import java.io.ByteArrayOutputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.actions.AbstractActionPipelineProcessor;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.listeners.ListenerTagNames;
import org.jboss.soa.esb.message.Message;

/**
 * Provides a more efficient logging mechanism than the OOTB JBoss SOA-P Logger.
 * To log the body of the message, the log level in the Log4j configuration must be equal to or more detailed than the action's configuration. 
 * 
 * Usage:
 * &lt;action class="org.jboss.tusk.esb.support.ExtendedLogActionHandler" name="log-before"&gt;&lt;/action&gt;
 * 
 * Default parameters; these can be overwritten in the configTree for more control:
 * logLocation - message.get()
 * logLevel - DEBUG
 * logCategory - [ServiceCategory].[ServiceName].[Action]
 * logMessage - None.
 * 
 * Optional parameters:
 * &lt;property name="logLocation" value="requestLocation" /&gt; - The location on the message to log.
 * &lt;property name="logLevel" value="DEBUG" /&gt; - The level at which the logger should execute.
 * &lt;property name="logCategory" value="org.jboss.tusk.example.location" /&gt; - The category the logger should log to in Log4j's configuration.
 * &lt;property name="logMessage" value="Hello World" /&gt; - An additional message to log.
 * 
 * @author Brad Davis
 *
 */
public class ExtendedLogActionHandler extends AbstractActionPipelineProcessor {

	protected final Logger logger;
    protected final Priority level;
    protected final String message;
    
    protected final String parentServiceCategory;
    protected final String parentServiceName;
    protected final String actionName;
    protected final String logLocation;
    
    /**
     * Creates the LogActionHandler.
     * 
     * @param config - supported, optional parameters: logLocation, logLevel, logCategory, logMessage
     * @throws ConfigurationException
     */
    public ExtendedLogActionHandler(ConfigTree config) throws ConfigurationException {
		parentServiceCategory = config.getParent().getAttribute(ListenerTagNames.SERVICE_CATEGORY_NAME_TAG);
		parentServiceName = config.getParent().getAttribute(ListenerTagNames.SERVICE_NAME_TAG);
		actionName = StringUtils.defaultIfEmpty(config.getAttribute(ListenerTagNames.ACTION_ELEMENT_TAG),"unnamed");
		
		logLocation = config.getAttribute("logLocation");
		level = Level.toLevel(config.getAttribute("logLevel"), Level.DEBUG);
		String logCategory = StringUtils.defaultIfEmpty(config.getAttribute("logCategory"),
				StringUtils.deleteWhitespace(parentServiceCategory+"."+parentServiceName+"."+actionName));
		
		logger = Logger.getLogger(logCategory);
		message = config.getAttribute("logMessage");
	}

	public Message process( final Message msg )
	{
		if(!logger.isEnabledFor(level))
		{
			//do nothing and return
			return msg;
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(parentServiceCategory+":"+parentServiceName+"["+actionName+"] :: ");
		if(StringUtils.isNotBlank(message))
		{
			sb.append(message);
		}
		
		Object messageBody = null;
		if(StringUtils.isNotBlank(logLocation)) 
		{
			messageBody = msg.getBody().get(logLocation);
		} 
		else 
		{
			messageBody = msg.getBody().get();
		}
		sb.append("\n=============\n");

		if(messageBody != null && messageBody instanceof String)
		{
			sb.append(messageBody);
		} 
		else if(messageBody != null && (messageBody instanceof byte[]))
		{
			sb.append(new String((byte[])messageBody));
		}
		else
		{
			sb.append(ReflectionToStringBuilder.toString(messageBody));
		}
		sb.append("\n=============");		
		logger.log(level, sb.toString());

		return msg;
	}
	

}
