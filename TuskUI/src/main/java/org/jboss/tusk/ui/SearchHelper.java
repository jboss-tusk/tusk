package org.jboss.tusk.ui;

import javax.servlet.http.HttpServletRequest;

public class SearchHelper {

//	private String[] fields = {"disease", "state", "patientId"};
	private String[] fields = {"id", "city", "addressLine1", "firstName", "lastName", "state", "zip"};
	
	public SearchHelper() {
		
	}
	
	public String getFieldOptions(String fieldName, HttpServletRequest request) {
		StringBuffer buf = new StringBuffer();
		buf.append("<select name=\"" + fieldName + "\">");
		buf.append("<option value=\"\"></option>");
		for (int i = 0; i < fields.length; i++) {
			buf.append("<option value=\"" + fields[i] + "\" " + getSelected(fieldName, fields[i], request) + ">" + fields[i] + "</option>");
		}
		buf.append("</select>");
		
		return buf.toString();
	}
	
	private String getSelected(String fieldName, String targetVal, HttpServletRequest request) {
		String usedVal = (String) request.getAttribute(fieldName);
		if (targetVal.equals(usedVal)) {
			return "selected=\"selected\"";
		} else {
			return "";
		}
	}

}
