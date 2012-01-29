package org.drools.support.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

public class MapContext implements NamespaceContext {

	private final Map<String, String> context = new HashMap<String, String>();
	
	public MapContext() {

	}
	
	public MapContext(Map<String, String> in) {
		if(in!=null&&in.size()>0)
		{
			context.putAll(in);
		}
	}
	
	public String getNamespaceURI(String prefix) {
		return context.get(prefix);
	}

	public String getPrefix(String namespaceURI) {
		Iterator<String> prefixIterator = getPrefixes(namespaceURI);
		
		if(prefixIterator.hasNext())
		{
			return getPrefixes(namespaceURI).next();
		}
		return null;
	}

	public Iterator<String> getPrefixes(String namespaceURI) {
		List<String> prefixes = new ArrayList<String>();
		
		for(String key : context.keySet())
		{
			//slow but works.
			if(namespaceURI.equals(context.get(key)))
			{
				prefixes.add(key);
			}
		}
		
		return prefixes.iterator();
	}
	
}
