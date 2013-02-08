package org.jboss.tusk.intake;

public class PayloadAndBytes {
	
	private Object payload = null;

	private byte[] bytes = null;
	
	public PayloadAndBytes(Object payload, byte[] bytes) {
		this.payload = payload;
		this.bytes = bytes;
	}

	public Object getPayload() {
		return payload;
	}

	public void setPayload(Object payload) {
		this.payload = payload;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}

}
