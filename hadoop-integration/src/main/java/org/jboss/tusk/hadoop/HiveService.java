package org.jboss.tusk.hadoop;

import java.util.ArrayList;
import java.util.List;

public class HiveService extends AbstractService {
	
	public List<String> queryIndex(String hiveQLQuery) {
		System.out.println("Executing query against message index: " + hiveQLQuery);
		if (isEmpty(hiveQLQuery)) {
			return new ArrayList<String>();
		}
		
		//TODO execute query
		System.out.println("Not implemented yet.");
		
		return new ArrayList<String>();
	}

}
