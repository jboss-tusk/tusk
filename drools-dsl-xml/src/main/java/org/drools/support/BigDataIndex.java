package org.drools.support;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

public abstract class BigDataIndex<T> {

	public BigDataIndex(String key, T value) {
		this.key = key;
		this.value = value;
	}
	
	protected final String key;
	protected final T value;
	
	public String getKey() {
		return key;
	}
	public T getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return key +"["+ReflectionToStringBuilder.toString(value)+"]";
	}

}
