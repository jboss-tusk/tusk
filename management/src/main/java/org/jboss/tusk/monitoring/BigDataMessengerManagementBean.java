package org.jboss.tusk.monitoring;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;

import javax.annotation.Resource;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.jboss.ejb3.annotation.Depends;
import org.jboss.ejb3.annotation.Management;
import org.jboss.ejb3.annotation.Service;
import org.jboss.tusk.jms.utility.MessageStubUtility;


@Service()
@Management(BigDataMessengerManagement.class)
@Depends({"com.jboss.bigdata:name=DocumentEntry,service=Queue"})
public class BigDataMessengerManagementBean implements BigDataMessengerManagement {

	@Resource(mappedName="java:/JmsXA") ConnectionFactory factory;
	@Resource(mappedName="queue/DocumentEntry") Destination documentQueue;
	 
	
	private final MessageStubUtility msu;
	
	public BigDataMessengerManagementBean() throws IOException {
		msu = new MessageStubUtility();
	}
	
	public void stubMessages(int count) {
		Collection<String> messages = msu.stubMessages(count);
		for(String msg : messages) {
			sendDocumentObject(msg);
		}
		
	}

	public void stubMessages() {
		stubMessages(10);
	}
	
	private void sendDocumentObject(Serializable o) {
		javax.jms.Connection connection = null;
		Session session = null;
		MessageProducer producer = null;
		
		try {
			connection = factory.createConnection();
			session = connection.createSession(true, Session.SESSION_TRANSACTED);
			
			ObjectMessage om = session.createObjectMessage();
			om.setObject(o);
			
			producer = session.createProducer(documentQueue);
			producer.send(om);
		
		} catch (JMSException e) {
			e.printStackTrace();
		} finally {
			if(producer!=null){try {producer.close();} catch(Exception e){};}
			if(session!=null){try {session.close();} catch(Exception e){};}
			if(connection!=null){try {connection.close();} catch(Exception e){};}
		}
	}
}