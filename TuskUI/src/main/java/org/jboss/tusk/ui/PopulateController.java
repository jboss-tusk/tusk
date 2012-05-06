package org.jboss.tusk.ui;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.hadoop.hbase.util.Bytes;
import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.jboss.tusk.common.TuskCassandraConfiguration;
import org.jboss.tusk.common.TuskConfiguration;
import org.jboss.tusk.common.DataStore;
import org.jboss.tusk.hadoop.service.MessagePersister;
import org.jboss.tusk.ispn.InfinispanException;
import org.jboss.tusk.ispn.InfinispanService;
import org.springframework.web.servlet.ModelAndView;

import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.template.ColumnFamilyTemplate;
import me.prettyprint.cassandra.service.template.ColumnFamilyUpdater;
import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.factory.HFactory;


public class PopulateController extends AbstractTuskController {

	private static final TuskConfiguration configuration = new TuskConfiguration();

	
	private static InfinispanService ispnService = null;

	//for Cassandra
	private static TuskCassandraConfiguration tuskCassConf = new TuskCassandraConfiguration();
	private static Cluster dataCluster = null;
	private static Keyspace ksp = null;
	private static ColumnFamilyTemplate<String, String> cfTemplate = null;

	//for hbase
	private MessagePersister messagePersister = null;
	
	//for Infinispan
	private static Cache<Object, Object> ispnDataStore = null;

	private static Map<String, Object> dummyMetadata0 = new HashMap<String, Object>();
	private static Map<String, Object> dummyMetadata1 = new HashMap<String, Object>();
	private static Map<String, Object> dummyMetadata2 = new HashMap<String, Object>();
	private static Map<String, Object> dummyMetadata3 = new HashMap<String, Object>();
	private static Map<String, Object> dummyMetadata4 = new HashMap<String, Object>();
	private static Map<Integer, Map<String, Object>> metadatas = new HashMap<Integer, Map<String, Object>>();
	
	private static int keyCounter = 0;
	
	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		ispnService = new InfinispanService();

		if (configuration.getDataStore().equals(DataStore.CASSANDRA)) {
			dataCluster = HFactory.getOrCreateCluster("data-cluster", tuskCassConf.getCluster());
			System.out.println("Hector dataCluster=" + dataCluster);
			
			// This is the keyspace to use for the data we are storing
			KeyspaceDefinition keyspaceDef = dataCluster.describeKeyspace(tuskCassConf.getKeyspace());
			System.out.println("Hector keyspaceDef=" + keyspaceDef);
			
			ksp = HFactory.createKeyspace(tuskCassConf.getKeyspace(), dataCluster);
			System.out.println("Hector keyspace=" + ksp);
			
			cfTemplate = new ThriftColumnFamilyTemplate<String, String>(
					ksp, tuskCassConf.getColumnFamily(), StringSerializer.get(), StringSerializer.get());
			System.out.println("Hector cfTemplate=" + cfTemplate);
		} else if (configuration.getDataStore().equals(DataStore.HBASE)) {
			//init is in ctor
		} else if (configuration.getDataStore().equals(DataStore.INFINISPAN)) {
			//init already done above
		}
		
		//metadata0
		dummyMetadata0.put("patientId", "111111");
		dummyMetadata0.put("state", "dc");
		dummyMetadata0.put("disease", "rubella");
		
		//metadata1
		dummyMetadata1.put("patientId", "123456");
		dummyMetadata1.put("state", "dc");
		dummyMetadata1.put("disease", "flu, measles, pertussis");
		
		//metadata2
		dummyMetadata2.put("patientId", "999999");
		dummyMetadata2.put("state", "va");
		dummyMetadata2.put("disease", "mumps, zombie, cholera");
		
		//metadata3
		dummyMetadata3.put("patientId", "555555");
		dummyMetadata3.put("state", "dc");
		dummyMetadata3.put("disease", "gout, plague, cancer");
		
		//metadata4
		dummyMetadata4.put("patientId", "987654");
		dummyMetadata4.put("state", "nc");
//		dummyMetadata4.put("disease", "mumps, zombie, cholera");
		
		metadatas.put(0, dummyMetadata0);
		metadatas.put(1, dummyMetadata1);
		metadatas.put(2, dummyMetadata2);
		metadatas.put(3, dummyMetadata3);
		metadatas.put(4, dummyMetadata4);
		
		//temporary hack to clear the cache because cache.clear() fails with the cassandra cache store
		try {
			Set<String> keys = new HashSet<String>();
			keys.addAll(dummyMetadata0.keySet());
			keys.addAll(dummyMetadata1.keySet());
			keys.addAll(dummyMetadata2.keySet());
			keys.addAll(dummyMetadata3.keySet());
			keys.addAll(dummyMetadata4.keySet());
			for (int i = 0; i < 100; i++) {
				ispnService.removeIndex("msg" + i, keys);
			} 
//			ispnService.clearCache();
		} catch (InfinispanException ex) {
			System.err.println("Got InfinispanException initializing SearchController: " + ex.getMessage());
		}
	}
	
	public PopulateController() {
		if (configuration.getDataStore().equals(DataStore.CASSANDRA)) {
			//TODO move stuff from static block in here?
		} else if (configuration.getDataStore().equals(DataStore.HBASE)) {
			messagePersister = new MessagePersister();
		} else if (configuration.getDataStore().equals(DataStore.INFINISPAN)) {
			//init is in static block
		}
	}
	
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest req,
			HttpServletResponse res) throws Exception {

		ModelAndView mav = new ModelAndView("populate");
		
		String message = (String)req.getParameter("message");
		
		if (!isEmpty(message)) {
			//choose a dummy metadata randomly, add it to the message body, then index it
			Map<String, Object> metadataFields = metadatas.get(new Random().nextInt(5));
			for (Entry<String, Object> field : metadataFields.entrySet()) {
				message += "\n<" + field.getKey() + ">" + field.getValue() + "</" + field.getKey() + ">";
			}
			
			System.out.println("Message is now:\n" + message);
			
			//get one of the predetermined keys
			String key = getNextKey();
	
			try {
				System.out.println("About to update");
				if (configuration.getDataStore().equals(DataStore.CASSANDRA)) {
					//write message to Cassandra
					ColumnFamilyUpdater<String, String> updater = cfTemplate.createUpdater(key);
					updater.setString("body", message);
					updater.setLong("timestamp", System.currentTimeMillis());
					cfTemplate.update(updater);
				} else if (configuration.getDataStore().equals(DataStore.HBASE)) {
					messagePersister.writeMessage(key, Bytes.toBytes(message));
				} else if (configuration.getDataStore().equals(DataStore.INFINISPAN)) {
					ispnDataStore.put(key, message);
				}
			    System.out.println("Done updating");
			} catch (HectorException ex) {
			    System.err.println("Got HectorException: " + ex.getMessage());
			}
			
			//write dummy indexes to Infinispan
			ispnService.writeIndex(key, metadataFields);

			mav.addObject("key", key);
			mav.addObject("message", message);
		}
		
		return mav;
	}

	private String getNextKey() {
		String key = "msg" + keyCounter;
		keyCounter++;
		return key;
	}

}
