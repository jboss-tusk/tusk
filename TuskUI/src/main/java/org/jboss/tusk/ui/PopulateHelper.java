package org.jboss.tusk.ui;

import java.io.IOException;

import org.jboss.tusk.jms.utility.MessageStubUtility;

public class PopulateHelper {

	public static final String ADD_ONE_LABEL = "Add One Random Message";
	public static final String ADD_MANY_LABEL = "Add Many Random Messages";
	public static final String ADD_PAYLOAD_LABEL = "Add This Message";

	public static final String PAYLOAD_TYPE_XML = "XML";
	public static final String PAYLOAD_TYPE_JSON = "JSON";
	
	private final MessageStubUtility msu;
	
	public PopulateHelper() throws IOException {
		msu = new MessageStubUtility();
	}
	
	public String getRandomXmlPayload() {
		return msu.stubXmlMessage();
	}
	
	public String getRandomJsonPayload() {
		return msu.stubJsonMessage();
	}

}
