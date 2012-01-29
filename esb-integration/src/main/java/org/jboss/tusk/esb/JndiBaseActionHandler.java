package org.jboss.tusk.esb;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.actions.ActionLifecycleException;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;

public abstract class JndiBaseActionHandler<T> extends AbstractActionLifecycle {

	private static final Log LOG = LogFactory.getLog(JndiBaseActionHandler.class);
	protected T ejbRef;
	protected String jndiName;
	
	public JndiBaseActionHandler(ConfigTree config) throws ConfigurationException {
		jndiName = config.getRequiredAttribute("jndiName");
	}
	
	@Override
	public void initialise() throws ActionLifecycleException {
		super.initialise();
		//reference ejb.
		try {
			getBean();
		} catch(Exception e)
		{
			throw new ActionLifecycleException("Exception retrieving EJB from: "+jndiName, e);
		}
	}

	public abstract Message process(Message message) throws ActionProcessingException;
	
	protected T getBean() throws NamingException
	{		
		if(ejbRef==null)
		{
			LOG.info("Retreiving and caching EJB lookup.");
			InitialContext context = new InitialContext();
			ejbRef = (T)context.lookup(jndiName);
		}
		return ejbRef;
	}
	
}
