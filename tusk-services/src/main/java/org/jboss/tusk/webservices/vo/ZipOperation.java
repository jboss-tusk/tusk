package org.jboss.tusk.webservices.vo;

public class ZipOperation {
	
	private String zip;
	private String payload;
	
	public ZipOperation() {
		this(null, null);
	}
	
	public ZipOperation(String zip, String payload) {
		this.zip = zip;
		this.payload = payload;
	}
	
	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

}
