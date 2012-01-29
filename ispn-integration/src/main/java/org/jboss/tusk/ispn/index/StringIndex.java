package org.jboss.tusk.ispn.index;

import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.ProvidedId;

@Indexed @ProvidedId
public class StringIndex extends BigDataIndex<String>{
	
	private static final long serialVersionUID = 254191608570966230L;

	public StringIndex(String key, String value, String docId) {
		super(key, value, docId);
	}

	public StringIndex(String key, String value) {
		super(key, value);
	}

	@Override
	public String toString() {
		return key+"["+value+"]" + 
			(this.docId != null ? "-->" + this.docId : "");
	}
}
