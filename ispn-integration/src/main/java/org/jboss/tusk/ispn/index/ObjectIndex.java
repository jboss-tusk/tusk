package org.jboss.tusk.ispn.index;

import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.ProvidedId;

@Indexed @ProvidedId
public class ObjectIndex extends BigDataIndex<Object>{

	private static final long serialVersionUID = 254191608570966230L;

	public ObjectIndex(String key, Object value, String docId) {
		super(key, value, docId);
	}

	public ObjectIndex(String key, Object value) {
		super(key, value);
	}

	@Override
	public String toString() {
		return key+"["+value+"]" + 
			(this.docId != null ? "-->" + this.docId : "");
	}
}
