package org.jboss.tusk.ispn.index;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.hibernate.search.annotations.Field;

public abstract class BigDataIndex<T> implements Serializable {

	private static final long serialVersionUID = -8670065622982345518L;

	public BigDataIndex(String key, T value, String docId) {
		this.key = key;
		this.value = value;
		this.docId = docId;
	}
	
	public BigDataIndex(String key, T value) {
		this.key = key;
		this.value = value;
		this.docId = null;
	}
	
	@Field protected final String key;
	@Field protected final T value;
	protected final String docId;
	
	public String getKey() {
		return key;
	}
	public T getValue() {
		return value;
	}
	public String getDocId() {
		return docId;
	}
	
	@Override
	public String toString() {
		return key +"["+ReflectionToStringBuilder.toString(value)+"]" + 
			(this.docId != null ? "-->" + this.docId : "");
	}
}
