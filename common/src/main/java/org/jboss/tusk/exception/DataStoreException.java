package org.jboss.tusk.exception;

public class DataStoreException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2247254626465900704L;

	public DataStoreException(String message) {
		super(message);
	}

	public DataStoreException(String message, Throwable thr) {
		super(message, thr);
	}
}
