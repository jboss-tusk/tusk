package org.drools.support.xml.evaluators;

import java.io.StringWriter;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import org.drools.support.BigDataIndex;
import org.drools.support.indexes.StringIndex;
import org.w3c.dom.Node;

/**
 * Extracts strings based on XPath.
 * @author Brad Davis
 */
public class XPathStringIndexEvaluator extends XPathAbstractIndexEvaluator<String> {

	public XPathStringIndexEvaluator(Map<String, String> namespaces,
			String xpathExpression, String keyName)
			throws XPathExpressionException {
		super(namespaces, xpathExpression, keyName);
	}

	@Override
	protected String convertNode(Node node) throws TransformerException {
		StringWriter sw = new StringWriter();
		Transformer t = TransformerFactory.newInstance().newTransformer();
		t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		sw.append(node.getTextContent());
		//t.transform(new DOMSource(node), new StreamResult(sw));
		return sw.toString();
	}

	@Override
	protected BigDataIndex<String> createIndex(String key, String value) {
		return new StringIndex(key, value);
	}

}
