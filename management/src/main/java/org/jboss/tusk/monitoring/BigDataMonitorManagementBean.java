package org.jboss.tusk.monitoring;

import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;

import org.jboss.ejb3.annotation.Management;
import org.jboss.ejb3.annotation.Service;
import org.jboss.util.naming.NonSerializableFactory;


@Service()
@Management(BigDataMonitorManagement.class)
public class BigDataMonitorManagementBean implements BigDataMonitorManagement {

	
	public BigDataMonitorManagementBean() {

	}
	
	public void start() {
		InitialContext rootCtx;
		try {
			rootCtx = new InitialContext();
			
	        try {
	            Name fullName = rootCtx.getNameParser("").parse("BigDataMonitor");
	            NonSerializableFactory.rebind(fullName, this, true);
	        } finally {
	            rootCtx.close(); 
	        }
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}
	
	
	private long totalMessageCount = 0;
	private long totalBytes = 0;
	
	
	public long getTotalMessageCount() {
		return totalMessageCount;
	}

	public long getTotalBytesProcessed() {
		return totalBytes;
	}
	
	public synchronized void addMessage() {
		totalMessageCount++;
	}
	
	public synchronized void addBytes(long byteSize) {
		totalBytes = totalBytes + byteSize;
	}

}
