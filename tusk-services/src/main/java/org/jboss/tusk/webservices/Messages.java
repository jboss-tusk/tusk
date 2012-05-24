package org.jboss.tusk.webservices;

import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.ws.rs.core.MediaType;

import org.jboss.tusk.webservices.vo.ZipOperation;
import org.jboss.tusk.webservices.vo.ZipOperations;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

@WebService()
public class Messages {

	//TODO this calls a rest service running on tomcat. Once we remove tomcat, it can just use the
	//code that is in the Indexer.getZips method
	@WebMethod()
	public @WebResult(name="zipOperations", partName="zipOperations") ZipOperations operationsByZip(String zip) {
	    System.out.println("Zip: " + zip);
	    
	    String restUrl = "http://localhost:8888/TuskUI/rest/indexer/zip" + (zip != null ? "/" + zip : "");
	    
		Client c = Client.create();
		WebResource r = c.resource(restUrl);
		String indexResponse = r.get(String.class);
		
	    ZipOperations zos = new ZipOperations();
	    
		String[] operations = indexResponse.split("\\|");
		System.out.println("Response from rest: " + indexResponse);
		for (int i = 0; i < operations.length; i++) {
//			System.out.println("operation " + i + "=" + operations[i]);
			if (operations[i].indexOf(",") > -1) {
				String parts[] = operations[i].split(",");
				ZipOperation zo = new ZipOperation(parts[0], parts[1]);
				zos.addZipOperation(zo);
			}
		}
		
		System.out.println("Returning " + zos.getZipOperation().size() + " zip operations.");
	    
	    return zos;
	}


	//TODO this calls a rest service running on tomcat. Once we remove tomcat, it can just use the
	//code that is in the Indexer.getZips method
	@WebMethod()
	public @WebResult(name="zipOperationsShort", partName="zipOperationsShort") String operationsByZipShort(String zip) {
	    System.out.println("Zip: " + zip);
	    
	    String restUrl = "http://localhost:8888/TuskUI/rest/indexer/zip" + (zip != null ? "/" + zip : "");
	    
		Client c = Client.create();
		WebResource r = c.resource(restUrl);
		String indexResponse = r.get(String.class);
		
//	    ZipOperations zos = new ZipOperations();
//	    
//		String[] operations = indexResponse.split("\\|");
		System.out.println("Response from rest: " + indexResponse);
//		for (int i = 0; i < operations.length; i++) {
////			System.out.println("operation " + i + "=" + operations[i]);
//			if (operations[i].indexOf(",") > -1) {
//				String parts[] = operations[i].split(",");
//				ZipOperation zo = new ZipOperation(parts[0], parts[1]);
//				zos.addZipOperation(zo);
//			}
//		}
//		
//		System.out.println("Returning " + zos.getZipOperation().size() + " zip operations.");
	    
	    return indexResponse;
	}
}
