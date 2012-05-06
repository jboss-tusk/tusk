package org.jboss.tusk.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.template.ColumnFamilyResult;
import me.prettyprint.cassandra.service.template.ColumnFamilyTemplate;
import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.factory.HFactory;

import org.apache.hadoop.hbase.util.Bytes;
import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.jboss.tusk.common.TuskCassandraConfiguration;
import org.jboss.tusk.common.TuskConfiguration;
import org.jboss.tusk.common.DataStore;
import org.jboss.tusk.hadoop.service.MessagePersister;
import org.jboss.tusk.ispn.InfinispanException;
import org.jboss.tusk.ispn.InfinispanService;
import org.jboss.tusk.ispn.index.SearchCriterion;
import org.springframework.web.servlet.ModelAndView;


public class SearchController extends AbstractTuskController {

	private static final TuskConfiguration configuration = new TuskConfiguration();
	private static TuskCassandraConfiguration tuskCassConf = new TuskCassandraConfiguration();
	
	private static InfinispanService ispnService = null;
	
	//for cassandra
	private static TuskCassandraConfiguration tuskCassConfConf = new TuskCassandraConfiguration();
	private static Cluster dataCluster = null;
	private static Keyspace ksp = null;
	private static ColumnFamilyTemplate<String, String> cfTemplate = null;

	//for hbase
	private MessagePersister messagePersister = null;
	
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
	}
	
	public SearchController() {
		if (configuration.getDataStore().equals(DataStore.CASSANDRA)) {
			//TODO move init from static block in here???
		} else if (configuration.getDataStore().equals(DataStore.HBASE)) {
			//for HBase
			messagePersister = new MessagePersister();
		} else if (configuration.getDataStore().equals(DataStore.INFINISPAN)) {
			//init is in static block
		}
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest req,
			HttpServletResponse res) throws Exception {

		String operator = (String)req.getParameter("operator");

		String field1 = (String)req.getParameter("field1");
		String term1 = (String)req.getParameter("term1");
		String field2 = (String)req.getParameter("field2");
		String term2 = (String)req.getParameter("term2");
		String field3 = (String)req.getParameter("field3");
		String term3 = (String)req.getParameter("term3");
		
		//search against InfinispanService
		List<String> results = new ArrayList<String>();
		
		if ((!isEmpty(field1) && !isEmpty(term1)) ||
				(!isEmpty(field2) && !isEmpty(term2)) ||
				(!isEmpty(field3) && !isEmpty(term3))) {
			System.out.println("Doing query");
			List<SearchCriterion> searchCriteria = new ArrayList<SearchCriterion>();
			
			if (!isEmpty(field1) && !isEmpty(term1)) {
				searchCriteria.add(new SearchCriterion(field1, term1));
				System.out.println("Added " + field1 + "=" + term1);
			}
			if (!isEmpty(field2) && !isEmpty(term2)) {
				searchCriteria.add(new SearchCriterion(field2, term2));
				System.out.println("Added " + field2 + "=" + term2);
			}
			if (!isEmpty(field3) && !isEmpty(term3)) {
				searchCriteria.add(new SearchCriterion(field3, term3));
				System.out.println("Added " + field3 + "=" + term3);
			}
			
			System.out.println("About to run query");
			results = ispnService.searchIndex(searchCriteria, operator.equalsIgnoreCase("and"));
			System.out.println("Got " + results.size() + " results.");
		} else {
			System.out.println("Nothing to search on.");
		}
		
		ModelAndView mav = new ModelAndView("search");

		mav.addObject("results", results);
		
		//load data for the matching messages
		if (results.size() > 0) {
			Map<String, String> messages = new HashMap<String, String>();
			for (String msgId : results) {
				String message = "";
				System.out.println("Loading message " + msgId);
				try {
					if (configuration.getDataStore().equals(DataStore.CASSANDRA)) {
					    ColumnFamilyResult<String, String> result = cfTemplate.queryColumns(msgId);
					    message = result.getString("body");
					} else if (configuration.getDataStore().equals(DataStore.HBASE)) {
						message = Bytes.toString(messagePersister.readMessage(msgId));
					} else if (configuration.getDataStore().equals(DataStore.INFINISPAN)) {
						message = ispnService.loadValue(msgId);
//						byte[] messageBytes = (byte[]) ispnDataStore.get(msgId);
						System.out.println("  message=" + message);
//						message = new String(messageBytes);
					}
				} catch (HectorException ex) {
				    System.err.println("Got HectorException reading message " + msgId + ": " + ex.getMessage());
				}
				messages.put(msgId, message);
			}
			mav.addObject("messages", messages);
		}

		//remove field names where there were no terms given
		if (!isEmpty(field1) && isEmpty(term1)) {
			field1 = null;
		}
		if (!isEmpty(field2) && isEmpty(term2)) {
			field2 = null;
		}
		if (!isEmpty(field3) && isEmpty(term3)) {
			field3 = null;
		}

		mav.addObject("field1", field1);
		mav.addObject("term1", term1);
		mav.addObject("field2", field2);
		mav.addObject("term2", term2);
		mav.addObject("field3", field3);
		mav.addObject("term3", term3);
		mav.addObject("operator", operator);
		
		return mav;
	}

}
