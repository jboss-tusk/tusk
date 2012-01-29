package org.drools.support.xml.evaluators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.drools.support.BigDataIndex;
import org.drools.support.xml.MapContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/**
 * 
 * Uses XPath to extract index information.
 * @author Brad Davis
 *
 */
public abstract class XPathAbstractIndexEvaluator<R> {
	private static final XPathFactory factory = XPathFactory.newInstance();
	
	private final XPath xpath;
	private final XPathExpression expression;
	private final String indexKey;
	
	public XPathAbstractIndexEvaluator(MapContext context, String xpathExpression, String keyName) throws XPathExpressionException {
		this.xpath = factory.newXPath();
		xpath.setNamespaceContext(context);
		xpath.compile(xpathExpression);
		expression = xpath.compile(xpathExpression);
		indexKey = keyName;
	}
	
	public XPathAbstractIndexEvaluator(Map<String, String> namespaces, String xpathExpression, String keyName) throws XPathExpressionException {
		this.xpath = factory.newXPath();
		MapContext context = new MapContext(namespaces);
		xpath.setNamespaceContext(context);
		expression = xpath.compile(xpathExpression);
		indexKey = keyName;
	}
	
	public Iterator<BigDataIndex<R>> evaluate(final Document document) throws XPathExpressionException
	{
		if(document==null){
			throw new NullPointerException();
		}
		
		final NodeList nodes = (NodeList)expression.evaluate(document, XPathConstants.NODESET);	
		List<BigDataIndex<R>> results = new ArrayList<BigDataIndex<R>>(); 
		if(nodes!=null)
		{
			for (int i = 0; i < nodes.getLength(); i++) {
				try {
					BigDataIndex<R> bdi = createIndex(this.indexKey, convertNode(nodes.item(i)));
					results.add(bdi);
				} catch (TransformerException e) {
					e.printStackTrace();
				}
			}
		}
		
		return results.iterator();
	}
	
	protected abstract R convertNode(Node node) throws TransformerException;
	
	protected abstract BigDataIndex<R> createIndex(String key, R value);
}
