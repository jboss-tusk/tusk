package org.drools.support.json.evaluators;

import org.drools.support.BigDataIndex;
import org.drools.support.indexes.StringIndex;

/**
 * Extracts strings from Json string.
 * @author Justin Hayes
 */
public class JsonStringIndexEvaluator extends JsonAbstractIndexEvaluator<String> {

	public JsonStringIndexEvaluator(String field) {
		super(field);
	}

	public JsonStringIndexEvaluator(String[] fields) {
		super(fields);
	}

	@Override
	protected String convertValue(String val) {
		return val;
	}

	@Override
	protected BigDataIndex<String> createIndex(String key, String value) {
		return new StringIndex(key, value);
	}

}
