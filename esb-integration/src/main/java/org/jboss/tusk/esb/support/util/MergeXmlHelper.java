package org.jboss.tusk.esb.support.util;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;


public class MergeXmlHelper {

	/**
	 * Static utility class for merging XML fragements.
	 */
	private MergeXmlHelper() {
		//seal
	}
	
	/**
	 * Merges many Serialized XML Strings into one following the MergeDocument schema.
	 * @param xmlSources
	 * @return
	 */
	public static String mergeXml(String... xmlSources) {
		String merge = "<merge:root xmlns:merge=\"http://org.jboss.tusk.esb.support.soa/merged\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">";
		
		for(String s : xmlSources)
		{
			merge=merge+"<source>";
			merge=merge+s;
			merge=merge+"</source>";
		}
		merge+="</merge:root>";
		return merge;
	}
	
	/**
	 * Merges many DOM Objects into one following the MergeDocument schema.
	 * @param xmlDocuments
	 * @return
	 * @throws TransformerException
	 */
	public static String mergeXml(Document... xmlDocuments) throws TransformerException {
		
		List<String> mergedSerialized = new ArrayList<String>(xmlDocuments.length);
		for(Document document : xmlDocuments)
		{	
			StringWriter writer = new StringWriter();
			TransformerFactory xformFactory = TransformerFactory.newInstance();
		    Transformer idTransform = xformFactory.newTransformer();
		    idTransform.setOutputProperty("omit-xml-declaration","yes");
		    Source input = new DOMSource(document);
		    Result output = new StreamResult(writer);
		    idTransform.transform(input, output);
		    
		    mergedSerialized.add(writer.toString());
		}
		//Array of serialized XML documents.
		String[] serializedArray = new String[mergedSerialized.size()];
		mergedSerialized.toArray(serializedArray);
		
		return mergeXml(serializedArray);
	}
}
