package org.jboss.tusk.hadoop;

public class HadoopException extends Exception {

	private static final long serialVersionUID = -1314731405790339426L;

	public HadoopException(String message) {
		super(message);
	}
	
	public HadoopException(String message, Throwable t) {
		super(message, t);
	}
	
}
