package org.jboss.tusk.intake;

import org.drools.agent.KnowledgeAgent;
import org.drools.agent.KnowledgeAgentConfiguration;
import org.drools.agent.KnowledgeAgentFactory;
import org.drools.io.ResourceChangeScannerConfiguration;
import org.drools.io.ResourceFactory;

public class KnowledgeAgentSingleton {

		private KnowledgeAgent kagent;

		// Private constructor prevents instantiation from other classes
		private KnowledgeAgentSingleton() {
			KnowledgeAgentConfiguration kaconf =KnowledgeAgentFactory.newKnowledgeAgentConfiguration();
	        // Do not scan directories, just files.
	        kaconf.setProperty( "drools.agent.scanDirectories", "false" );
	        kagent = KnowledgeAgentFactory.newKnowledgeAgent("MyAgent",kaconf);
	        ResourceChangeScannerConfiguration sconf = ResourceFactory.getResourceChangeScannerService().newResourceChangeScannerConfiguration();
	        sconf.setProperty( "drools.resource.scanner.interval", "30" );
	        ResourceFactory.getResourceChangeScannerService().configure( sconf );
	        kagent.applyChangeSet(ResourceFactory.newClassPathResource("indexing-changeset.xml"));
	        /*
	        UrlResource urlResource = (UrlResource)ResourceFactory.newUrlResource("http://localhost:8080/jboss-brms/org.drools.guvnor.Guvnor/package/IndexingRules/LATEST/ChangeSet.xml");
	        urlResource.setBasicAuthentication("enabled");
	        urlResource.setUsername("admin");
	        urlResource.setPassword("admin");
	        kagent.applyChangeSet(urlResource);
	        */
	        ResourceFactory.getResourceChangeNotifierService().start();
	        ResourceFactory.getResourceChangeScannerService().start();
		}
		
		public KnowledgeAgent getKagent() {
			return kagent;
		}

		private static class DroolsKnowledgeSingletonHolder {
			private static final KnowledgeAgentSingleton INSTANCE = new KnowledgeAgentSingleton();
		}

		public static KnowledgeAgentSingleton getInstance() {
			return DroolsKnowledgeSingletonHolder.INSTANCE;
		}

	}
