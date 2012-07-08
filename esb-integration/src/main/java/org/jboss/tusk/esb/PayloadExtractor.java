package org.jboss.tusk.esb;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.actions.ActionLifecycleException;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;
import org.jboss.tusk.common.TuskConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * The PaylodExtractor is responsible for the extraction of Big Data input that comes into Tusk's 
 * via the DocumentEntry queue.
 * 
 * @author jhayes
 *
 */
public class PayloadExtractor extends AbstractActionLifecycle {

	private static final TuskConfiguration configuration = new TuskConfiguration();

	private static final Log LOG = LogFactory.getLog(PayloadExtractor.class);
	private static final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	private DocumentBuilder parser;
		
	public PayloadExtractor(ConfigTree config) throws ConfigurationException {
		super();
	}
	
	@Override
	public void initialise() throws ActionLifecycleException {
		//create document parser.
		factory.setNamespaceAware(true);
		try {
			parser = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new ActionLifecycleException("Exception building parser.", e);
		}
	}
	
	/**
	 * The process operation takes in an ESB message, extracts the payload, turns it into an XML DOM 
	 * document, and then adds the document to the message. For further processing by other actions.
	 * 
	 * @param message
	 */
	public Message process(Message message) throws ActionProcessingException {
		
		Document document = null;
		byte[] messageBodyBytes = null;
		
		Object messageBody = message.getBody().get();
		if(messageBody instanceof byte[]) {
			messageBodyBytes = (byte[])messageBody;
			
			//parse document.
			ByteArrayInputStream tmp = new ByteArrayInputStream(messageBodyBytes);
			try {
				document = parser.parse(tmp);
			} catch (Exception e) {
				throw new ActionProcessingException("Exception parsing document.", e);
			} 
		} else if(messageBody instanceof String) {
			messageBodyBytes = ((String)messageBody).getBytes();
			
			//parse document.
			ByteArrayInputStream tmp = new ByteArrayInputStream(messageBodyBytes);
			try {
				document = parser.parse(tmp);
			} catch (Exception e) {
				throw new ActionProcessingException("Exception parsing document.", e);
			} 
		} else if(messageBody instanceof Document) {
			//just cast.
			document = (Document)messageBody;
			
			//get a byte[] from the document - TODO need to test this
			try {
				messageBodyBytes = nodeToString(document.getDocumentElement()).getBytes();
			} catch (TransformerException e) {
				throw new ActionProcessingException("Exception parsing document.", e);
			}
		}
		
		if (document != null) {
			//put document in message; then return it
			LOG.debug("Document not null.");
			message.getBody().add("payloadDocument", document);
			message.getBody().add("payloadBytes", messageBodyBytes);
			return message;
		} else {
			//return null so the pipeline stops
			LOG.warn("Did not find payload in message.");
			return null;
		}
	}
	
	private static String nodeToString(Node node) throws TransformerException {
		StringWriter sw = new StringWriter();
		Transformer t = TransformerFactory.newInstance().newTransformer();
		t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		t.transform(new DOMSource(node), new StreamResult(sw));
	
		return sw.toString();
	}
	
}
