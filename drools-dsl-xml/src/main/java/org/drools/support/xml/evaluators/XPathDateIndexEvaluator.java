package org.drools.support.xml.evaluators;

import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPathExpressionException;

import org.drools.support.BigDataIndex;
import org.drools.support.indexes.DateIndex;
import org.w3c.dom.Node;

/**
 * Extracts dates using XPath.
 * @author Brad Davis
 */
public class XPathDateIndexEvaluator extends XPathAbstractIndexEvaluator<Date> {

	private final DateFormat df;
	
	public XPathDateIndexEvaluator(Map<String, String> namespaces,
			String xpathExpression, String keyName, String dateFormat)
			throws XPathExpressionException {
		
		super(namespaces, xpathExpression, keyName);
		df = new SimpleDateFormat(dateFormat);
	}

	@Override
	protected Date convertNode(Node node) throws TransformerException {
		StringWriter sw = new StringWriter();
		Transformer t = TransformerFactory.newInstance().newTransformer();
		t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		//t.transform(new DOMSource(node), new StreamResult(sw));
		sw.append(node.getTextContent());
	
		Date date;
		try {
			date = df.parse(sw.toString());
		} catch (ParseException e) {
			throw new TransformerException("Unable to parse date.", e);
		}
		return date;
	}

	@Override
	protected BigDataIndex<Date> createIndex(String key, Date value) {
		return new DateIndex(key, value);
	}

}
