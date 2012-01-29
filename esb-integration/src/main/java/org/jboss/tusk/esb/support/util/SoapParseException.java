package org.jboss.tusk.esb.support.util;

public class SoapParseException extends Exception {

	public SoapParseException(String message, Throwable t) {
		super(message, t);
	}
	
	public SoapParseException(String message) {
		super(message);
	}
}
