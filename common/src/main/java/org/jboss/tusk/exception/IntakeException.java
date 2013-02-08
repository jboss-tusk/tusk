package org.jboss.tusk.exception;

public class IntakeException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2247172938592310284L;

	public IntakeException(String message) {
		super(message);
	}

	public IntakeException(String message, Throwable thr) {
		super(message, thr);
	}

}
