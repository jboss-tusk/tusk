package org.jboss.tusk.hadoop;

import java.util.List;
import java.util.Map;

public class AbstractService {
	
	/**
	 * Returns true if the argument is null or is "empty", which is
	 * determined based on the type of the argument.
	 * @param o
	 * @return
	 */
	public boolean isEmpty(Object o) {
		if (o == null) {
			return true;
		} else if (o instanceof String && "".equals((String)o)) {
			return true;
		} else if (o instanceof List<?> && ((List<?>)o).size() < 1) {
			return true;
		} else if (o instanceof Map<?, ?> && ((Map<?, ?>)o).size() < 1) {
			return true;
		} else if (o instanceof byte[] && ((byte[])o).length < 1) {
			return true;
		}
		
		return false;
	}

}
