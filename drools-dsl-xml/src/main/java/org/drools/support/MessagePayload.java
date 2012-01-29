package org.drools.support;

import java.util.HashSet;
import java.util.Set;

public class MessagePayload<T> {

	protected Set<BigDataIndex<?>> indexes = new HashSet<BigDataIndex<?>>();
	protected T payload;
	
	public Set<BigDataIndex<?>> getIndexes() {
		return indexes;
	}
	
	public void setIndexes(Set<BigDataIndex<?>> indexes) {
		this.indexes = indexes;
	}
	
	public MessagePayload(T payload) {
		this.payload = payload;
	}
	
	public T getPayload() {
		return payload;
	}
	
	public void setPayload(T payload) {
		this.payload = payload;
	}
}
