package org.jboss.tusk.esb.support;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.actions.ActionLifecycleException;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;

public abstract class EjbBaseActionHandler<T> extends AbstractActionLifecycle {

	private static final Log LOG = LogFactory.getLog(EjbBaseActionHandler.class);
	protected T ejbRef;
	protected String jndiName;
	
	public EjbBaseActionHandler(ConfigTree config) throws ConfigurationException {
		jndiName = config.getRequiredAttribute("jndiName");
	}
	
	@Override
	public void initialise() throws ActionLifecycleException {
		super.initialise();
		//reference ejb.
		try {
			getEjb();
		} catch(Exception e)
		{
			throw new ActionLifecycleException("Exception retrieving EJB from: "+jndiName, e);
		}
	}

	public abstract Message process(Message message);
	
	protected T getEjb() throws NamingException
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
