package org.drools.support.indexes;

import org.drools.support.BigDataIndex;

public class StringIndex extends BigDataIndex<String>{

	public StringIndex(String key, String value) {
		super(key, value);
	}

	@Override
	public String toString() {
		return key+"["+value+"]"; 
	}
}
