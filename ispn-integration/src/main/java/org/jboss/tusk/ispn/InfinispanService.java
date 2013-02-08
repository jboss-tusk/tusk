package org.jboss.tusk.ispn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.Query;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.Search;
import org.infinispan.query.SearchManager;
import org.jboss.tusk.common.DataStoreType;
import org.jboss.tusk.common.TuskConfiguration;
import org.jboss.tusk.hadoop.HBaseFacade;
import org.jboss.tusk.ispn.InfinispanException;
import org.jboss.tusk.ispn.index.BigDataIndex;
import org.jboss.tusk.ispn.index.SearchCriterion;
import org.jboss.tusk.ispn.index.StringIndex;
//import org.infinispan.util.logging.Log;
//import org.infinispan.util.logging.LogFactory;


public class InfinispanService {

	private static final TuskConfiguration configuration = new TuskConfiguration();
	
	private static final Log LOG = LogFactory.getLog(InfinispanService.class);
	
	//valid values are 's3' or nothing
	private static final String JGROUPS_TRANSPORT = System.getProperty("tusk.jgroups.transport", null);

	//this is for the indexes
	private static Cache<Object, Object> indexGrid = null;
	
	//this is for data payloads (if Infinispan is the current data store)
	private static Cache<Object, Object> dataGrid = null;
	
	public InfinispanService() {
		//see if we need to create the grid objects
		if (indexGrid == null) {
			LOG.info("Creating grid objects for first time.");
			String indexGridConfigFile = null;
			String dataGridConfigFile = null;
			try {
				//figure out which config file to use for the index grid
				if (configuration.getDataStoreType().equals(DataStoreType.CASSANDRA)) {
					indexGridConfigFile = StringUtils.isEmpty(JGROUPS_TRANSPORT) ? 
							"bigdata-index-ispn-cassandra.xml" : 
								"bigdata-index-ispn-cassandra-s3.xml";
				} else if (configuration.getDataStoreType().equals(DataStoreType.HBASE)) {
					indexGridConfigFile = StringUtils.isEmpty(JGROUPS_TRANSPORT) ? 
							"bigdata-index-ispn-hbase.xml" : 
								"bigdata-index-ispn-hbase-s3.xml";
				} else if (configuration.getDataStoreType().equals(DataStoreType.INFINISPAN)) {
					indexGridConfigFile = StringUtils.isEmpty(JGROUPS_TRANSPORT) ? 
							"bigdata-index-ispn.xml" : 
								"bigdata-index-ispn-s3.xml";
					//need to create the data grid too since we are storing the data payloads
					//themselves in infinispan as well
					dataGridConfigFile = StringUtils.isEmpty(JGROUPS_TRANSPORT) ? 
							"bigdata-store-ispn.xml" : 
								"bigdata-store-ispn-s3.xml";
					LOG.info("Using " + dataGridConfigFile + " for the data grid.");
					dataGrid = new DefaultCacheManager(dataGridConfigFile).getCache();
				} else if (configuration.getDataStoreType().equals(DataStoreType.FILESYSTEM)) {
					indexGridConfigFile = StringUtils.isEmpty(JGROUPS_TRANSPORT) ? 
							"bigdata-index-ispn-filesystem.xml" : 
								"bigdata-index-ispn-filesystem-s3.xml";
				}
				
				//create the grid object
				LOG.info("Using " + indexGridConfigFile + " for the index grid.");
				indexGrid = new DefaultCacheManager(indexGridConfigFile).getCache();
			} catch (Exception ex) {
				LOG.error("Got exception creating cache manager: " + ex.getMessage());
				ex.printStackTrace();
			}
		} else {
			LOG.debug("Already created cache");
		}
	}
	
	public void writeValue(String key, String value) throws InfinispanException {
		try {
			StringValue strVal = new StringValue(value);
			LOG.debug("Writing " + key + "->" + strVal + " to " + dataGrid);
			dataGrid.put(key, strVal);
		} catch (Exception ex) {
			LOG.error("Got " + ex.getClass().getName() + " writing value: " + ex.getMessage());
			ex.printStackTrace();
		}
	}
	
	public String loadValue(String key) throws InfinispanException {
		try {
			LOG.debug("Loading " + key + " from " + dataGrid);
			
			StringValue strVal = (StringValue)dataGrid.get(key);
			return strVal.toString();
		} catch (Exception ex) {
			LOG.error("Got " + ex.getClass().getName() + " writing value: " + ex.getMessage());
			ex.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Writes the index fields for a document into the Infinispan cache.
	 * Field values must be strings.
	 * 
	 * TODO need a way to put all indexes for a given documentId in a 
	 * single cached object so we never have to do multiple queries and
	 * return the intersection of results (e.g. doMultiStepQuery)
	 * 
	 * TODO need a way to consistently and extensibly do marshalling and
	 * unmarshalling to/from strings.
	 * 
	 * @param documentId
	 * @param fields
	 * @throws InfinispanException
	 */
	public void writeIndex(String documentId, Map<String, Object> fields) throws InfinispanException {
		//convert fields into StringIndex objects and insert into cache
		for (Entry<String, Object> entry : fields.entrySet()) {
			String indexUniqueId = documentId + "_" + entry.getKey();
			StringIndex strIndex = new StringIndex(entry.getKey(), entry.getValue().toString().toLowerCase(), documentId);
			LOG.debug("About to write " + indexUniqueId + "->" + strIndex + " to " + indexGrid);
			
			indexGrid.put(indexUniqueId, strIndex);
			
			if (LOG.isDebugEnabled()) {
				Object val = indexGrid.get(indexUniqueId);
				LOG.debug("Just added and loaded for " + indexUniqueId + "=" + val + " (a " + val.getClass() + ")");
			}
		}
	}
	
	/**
	 * Removes the index fields for a document from the Infinispan cache.
	 * 
	 * @param documentId
	 * @param keys
	 * @throws InfinispanException
	 */
	public void removeIndex(String documentId, Set<String> keys) throws InfinispanException {
		for (String key : keys) {
			String indexUniqueId = documentId + "_" + key;
			indexGrid.remove(indexUniqueId);
			
			LOG.debug("Removed index " + indexUniqueId);
		}
	}
	
	/**
	 * Searches the index, given a set of criteria fields. Any resulting objects will
	 * match ALL criteria.
	 * @param criteria a list of index entries that match ALL criteria fields
	 * @return
	 */
	public List<String> searchIndex(List<SearchCriterion> criteria, boolean isAndQuery) {
		List<String> results = new ArrayList<String>();

		SearchManager searchManager = Search.getSearchManager(indexGrid);
		
		if (criteria.size() < 2) {
			results = doCombinedQuery(searchManager, criteria, true);
		} else if (!isAndQuery) {
			results = doCombinedQuery(searchManager, criteria, isAndQuery);
		} else {
			results = doMultiStepQuery(searchManager, criteria);
		}

		return results;
	}

	/**
	 * Executes a query where results have to match all
	 * of the search criteria.
	 * @param searchManager
	 * @param criteria
	 * @return
	 */
	private List<String> doMultiStepQuery(SearchManager searchManager, List<SearchCriterion> criteria) {
		List<String> finalResults = null;
		
		//the (inefficient) strategy (for now) is to execute a search for each of the
		//criteria fields and only return the results that come back for each of the
		//sub-searches
		for (SearchCriterion entry : criteria) {
			List<SearchCriterion> criterion = new ArrayList<SearchCriterion>(1);
			criterion.add(entry);
			
			//'results' is the results that match this single field
			List<String> singleFieldResults = doCombinedQuery(searchManager, criterion, true);
			
			if (singleFieldResults == null || singleFieldResults.size() == 0) {
				//got no results so return the empty list now
				return new ArrayList<String>();
			} else if (finalResults == null) {
				//first iteration and we got some results so keep 
				//those as the running final result list
				finalResults = singleFieldResults;
			} else {
				//keep only the results from the running final list
				//that are also results in the current single field search
				List<String> newFinalResults = new ArrayList<String>();
				for (String val : finalResults) {
					if (singleFieldResults.contains(val)) {
						newFinalResults.add(val);
					}
				}
				finalResults = newFinalResults;
			}
		}
		
		return finalResults;
	}

	/**
	 * Executes a query where results only have to match at least one
	 * of the search criteria.
	 * @param criteria
	 * @param searchManager
	 * @return
	 */
	private List<String> doCombinedQuery(SearchManager searchManager, 
			List<SearchCriterion> criteria, boolean isAndQuery) {
		List<String> results = new ArrayList<String>();
		
		QueryBuilder queryBuilder = searchManager.buildQueryBuilderForClass(StringIndex.class).get();
		
		List<BooleanJunction<BooleanJunction>> junctions = new ArrayList<BooleanJunction<BooleanJunction>>();
		
		//make sub-queries for each criteria field
		for (SearchCriterion entry : criteria) {
			List<Query> queries = new ArrayList<Query>();
			Query queryKey = queryBuilder.keyword()
	    		.onField("key")
	    		.matching(entry.getField())
	    		.createQuery();
			Query queryValue = queryBuilder.keyword().wildcard()
	    		.onField("value")
	    		.matching(entry.getValue().toLowerCase() + "*")
	    		.createQuery();
//			Query queryValue = queryBuilder.phrase()
//	    		.onField("value")
//	    		.sentence(entry.getValue())
//	    		.createQuery();
			queries.add(queryKey);
			queries.add(queryValue);

			//now and the fields together
			BooleanJunction<BooleanJunction> blnJunction = queryBuilder.bool();
			for (Query query : queries) {
				blnJunction.must(query);
			}
			
			junctions.add(blnJunction);
		}

		//now and all of the BooleanJunctions together
		BooleanJunction<BooleanJunction> allBooleanJunction = queryBuilder.bool();
		for (BooleanJunction<BooleanJunction> junction : junctions) {
			if (isAndQuery) {
				allBooleanJunction.must(junction.createQuery());
			} else {
				allBooleanJunction.should(junction.createQuery());
			}
		}
		Query allQuery = allBooleanJunction.createQuery();
		
		//now execute the query and gather the results
		CacheQuery query = searchManager.getQuery(allQuery, StringIndex.class);
		List<?> queryResults = query.list();

		LOG.debug("Query \"" + allQuery + "\" returned " + queryResults.size() + " results.");
		
		if (queryResults != null) {
			for (Object result : queryResults) {
				if (result != null) {
					String docId = ((BigDataIndex<?>)result).getDocId();
					LOG.debug("  " + docId);
					
					//don't keep dupes
					if (!results.contains(docId)) {
						results.add(docId);
					}
				} else {
					LOG.warn("Got a result from the search, but it was null.");
				}
			}
		}
		
		return results;
	}
	
	public void clearCache() throws InfinispanException {
		try {
			synchronized(indexGrid) {
				indexGrid.clear();
			}
		} catch (Exception ex) {
			throw new InfinispanException("Caught exception clearing cache: " + ex.getMessage(), ex);
		}
	}
	
	public void stopCache() {
		indexGrid.stop();
	}
	
	public void startCache() {
		indexGrid.start();
	}

}
