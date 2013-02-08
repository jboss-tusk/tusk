package org.jboss.tusk.ispn;

import java.io.Serializable;

import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.ProvidedId;

/**
 * Container for strings to be stored in the data grid.
 * @author justin
 *
 */
@Indexed @ProvidedId
public class StringValue implements Serializable {

	private String value;
	
	public StringValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}
}
