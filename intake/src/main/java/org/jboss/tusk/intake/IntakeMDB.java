package org.jboss.tusk.intake;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

@MessageDriven(name = "IntakeMDB", activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/intakeQueue"),
		@ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
public class IntakeMDB {
	public void onMessage(Message message) {
		TextMessage tm = (TextMessage) message;
		try {
			System.out.println("Received message " + tm.getText());
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

}
