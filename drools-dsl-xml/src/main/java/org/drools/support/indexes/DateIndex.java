package org.drools.support.indexes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.drools.support.BigDataIndex;

public class DateIndex extends BigDataIndex<Date>{

	private static final DateFormat df = new SimpleDateFormat();
	
	public DateIndex(String key, Date value) {
		super(key, value);
	}

	@Override
	public String toString() {
		if(value!=null)
		{
			return key+"["+df.format(value)+"]"; 
		}
		else return key+"[null]";
	}
}
