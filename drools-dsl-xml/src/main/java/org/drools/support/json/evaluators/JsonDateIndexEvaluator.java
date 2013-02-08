package org.drools.support.json.evaluators;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.drools.support.BigDataIndex;
import org.drools.support.indexes.DateIndex;

/**
 * Extracts dates from Json string.
 * @author Justin Hayes
 */
public class JsonDateIndexEvaluator extends JsonAbstractIndexEvaluator<Date> {

	private final DateFormat df;
	
	public JsonDateIndexEvaluator(String field, String dateFormat) {
		super(field);
		df = new SimpleDateFormat(dateFormat);
	}

	public JsonDateIndexEvaluator(String[] fields, String dateFormat) {
		super(fields);
		df = new SimpleDateFormat(dateFormat);
	}
	
	@Override
	protected Date convertValue(String val) throws ParseException {
		return df.parse(val);
	}

	@Override
	protected BigDataIndex<Date> createIndex(String key, Date value) {
		return new DateIndex(key, value);
	}

}
