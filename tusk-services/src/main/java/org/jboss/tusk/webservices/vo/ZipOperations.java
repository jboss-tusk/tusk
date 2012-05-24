package org.jboss.tusk.webservices.vo;

import java.util.ArrayList;
import java.util.List;

public class ZipOperations {
	
	private List<ZipOperation> zipOperation = new ArrayList<ZipOperation>();
	
	public ZipOperations() {
		this(null);
	}
	
	public ZipOperations(List<ZipOperation> messages) {
		this.zipOperation = messages;
	}

	public List<ZipOperation> getZipOperation() {
		return zipOperation;
	}

	public void setZipOperation(List<ZipOperation> messages) {
		this.zipOperation = messages;
	}
	
	public void addZipOperation(ZipOperation message) {
		if (this.zipOperation == null) {
			zipOperation = new ArrayList<ZipOperation>();
		}
		this.zipOperation.add(message);
	}

}
