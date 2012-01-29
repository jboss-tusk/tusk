package org.jboss.tusk.monitoring;


public interface BigDataMonitorManagement {

	public long getTotalMessageCount();
	public long getTotalBytesProcessed();
	public void addMessage();
	public void addBytes(long byteSize);
	
}
