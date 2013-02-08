package org.drools.support.json.evaluators;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.drools.support.BigDataIndex;

/**
 * 
 * Extract index information from Json strings.
 * @author Justin Hayes
 *
 */
public abstract class JsonAbstractIndexEvaluator<R> {

	private static final Log LOG = LogFactory.getLog(JsonAbstractIndexEvaluator.class);
	
	private final String[] fields;
	
	public JsonAbstractIndexEvaluator(String field) {
		String[] justOneField = {field};
		this.fields = justOneField;
	}
	
	public JsonAbstractIndexEvaluator(String[] fields) {
		this.fields = fields;
	}
	
	public Iterator<BigDataIndex<R>> evaluate(final Map<String, Object> jsonMap) 
	{
		if (jsonMap == null){
			throw new NullPointerException();
		}
		
		//extract the value of each field
		List<BigDataIndex<R>> results = new ArrayList<BigDataIndex<R>>();
		
		for (String field : this.fields) {
			//TODO this only supports flat JSON objects, where all fields are at the top level.
			//It does not support nested fields, but that can be added by parsing the field
			//and doing repeated .get calls to go all the way down to what you want
			if (jsonMap.containsKey(field)) {
				try {
					BigDataIndex<R> bdi = createIndex(field, convertValue(jsonMap.get(field).toString()));
					results.add(bdi);
				} catch (ParseException ex) {
					LOG.error("Caught ParseException getting the value for field " + 
							field + ": " + ex.getMessage() + ". Continuing anyway.", ex);
				} catch (NullPointerException ex) {
					LOG.error("Caught NullPointerException getting the value for field " + 
							field + ": " + ex.getMessage() + ". Continuing anyway.", ex);
				}
			}
		}
		
		return results.iterator();
	}
	
	protected abstract R convertValue(String value) throws ParseException;
	
	protected abstract BigDataIndex<R> createIndex(String key, R value);
}
