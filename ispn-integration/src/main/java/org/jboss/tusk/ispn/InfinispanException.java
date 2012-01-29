package org.jboss.tusk.ispn;

public class InfinispanException extends Exception {

	private static final long serialVersionUID = 5094136141852797511L;

	public InfinispanException(String message) {
		super(message);
	}
	
	public InfinispanException(String message, Throwable t) {
		super(message, t);
	}
	
}
