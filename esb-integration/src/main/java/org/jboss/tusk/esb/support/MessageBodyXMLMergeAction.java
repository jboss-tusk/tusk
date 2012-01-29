package org.jboss.tusk.esb.support;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.soa.esb.ConfigurationException;
import org.jboss.soa.esb.actions.AbstractActionPipelineProcessor;
import org.jboss.soa.esb.actions.ActionProcessingException;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Message;
import org.jboss.tusk.esb.support.util.MergeXmlHelper;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


public class MessageBodyXMLMergeAction extends AbstractActionPipelineProcessor {
	protected ConfigTree _config;
	private static final Log LOG = LogFactory
			.getLog(MessageBodyXMLMergeAction.class);
	protected List<String> mergeLocations = new ArrayList<String>();
	protected String resultLocation;
	
	public MessageBodyXMLMergeAction(ConfigTree config)
			throws ConfigurationException {
		_config = config;

		this.resultLocation = config.getAttribute("resultLocation");
		
		ConfigTree locations[] = _config.getChildren("merge-location");

		for (ConfigTree conf : locations) {
			mergeLocations.add(conf.getRequiredAttribute("body-location"));
		}
	}

	public Message process(Message message) throws ActionProcessingException {
		try {
			Document documents[] = new Document[mergeLocations.size()];

			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder parser = factory.newDocumentBuilder();
			String mergeXml = "";
			int counter = 0;

			for (String location : mergeLocations) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("processing location " + location);
				}
				try {
					if (StringUtils.isNotBlank(location)) {
						mergeXml = (String) message.getBody().get(location);
					} else {
						mergeXml = (String) message.getBody().get();
					}
					if (LOG.isDebugEnabled()) {
						LOG.debug("Got xml: " + mergeXml + " from location "
								+ location);
					}

					ByteArrayInputStream tmp = new ByteArrayInputStream(
							mergeXml.getBytes());
					documents[counter++] = parser.parse(tmp);
				} catch (SAXException e) {
					LOG.error("caught SAXException.", e);
				} catch (IOException e) {
					LOG.error("caught IOException", e);
				}
			}

			String mergedXml = MergeXmlHelper.mergeXml((Document[]) documents);
			
			
			if(StringUtils.isNotBlank(resultLocation)) {
				message.getBody().add(resultLocation, mergedXml);
			}
			else {
				message.getBody().add(mergedXml);
			}
			

		} catch (ParserConfigurationException e) {
			LOG.error("Caught ParserConfigurationException", e);
		} catch (TransformerException e) {
			LOG.error("Caught TransformerException", e);
		}

		return message;
	}
}
