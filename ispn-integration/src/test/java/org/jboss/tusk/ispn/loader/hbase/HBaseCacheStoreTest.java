package org.jboss.tusk.ispn.loader.hbase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.Search;
import org.infinispan.query.SearchManager;
import org.jboss.tusk.ispn.index.StringIndex;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.apache.lucene.search.Query;

import org.hibernate.search.query.dsl.QueryBuilder;



public class HBaseCacheStoreTest {

	private static final String[] messageKeys = {"message1", "message2", "message3", "message4", "message5"};
	
	private static final StringIndex[] stringIndexes = {
		new StringIndex("color", "red", messageKeys[0]),
		new StringIndex("color", "blue", messageKeys[1]),
		new StringIndex("color", "green", messageKeys[2]),
		new StringIndex("color", "black", messageKeys[3]),
		new StringIndex("color", "purple", messageKeys[4]),
	};
	
	private Cache<Object, Object> cache = null;
	
	static {
	}
	
	@Before
	public void setup() {
		System.setProperty("java.net.preferIPv4Stack", "true");
		
		try {
			cache = new DefaultCacheManager("bigdata-index-infinispan.xml").getCache();
		} catch (Exception ex) {
			System.err.println("Got exception creating cache manager: " + ex.getMessage());
		}
	}
	
	@After
	public void tearDown() {
		cache.clear();
		cache.stop();
	}

//	@Test
//	public void testCache() throws Exception {
//		try {
//			for (int i = 0; i < messageKeys.length; i++) {
//				cache.put(messageKeys[i], stringIndexes[i]);
//			}
//			
//			for (int i = 0; i < messageKeys.length; i++) {
//				assertTrue(cache.containsKey(messageKeys[i]));
//				
//				Object val = cache.get(messageKeys[i]);
//				assertNotNull(val);
//				assertEquals(val.toString(), stringIndexes[i].toString());
//				cache.remove(messageKeys[i]);
//				
//				val = cache.get(messageKeys[i]);
////				System.out.println("*** val=" + val);
//				assertNull(val);
//			}
//			
//		} catch (Exception ex) {
//			System.err.println("Caught exception: " + ex.getMessage());
//			ex.printStackTrace();
//		}
//	}
//
//	@Test
//	public void testSearch() throws Exception {
//		try {
//			for (int i = 0; i < stringIndexes.length; i++) {
//				cache.put("index" + i, stringIndexes[i]);
//			}
//			
//			SearchManager searchManager = Search.getSearchManager(cache);
//			QueryBuilder queryBuilder = searchManager.buildQueryBuilderForClass(StringIndex.class).get();
//			Query keyQuery = queryBuilder.phrase()
//            	.onField("key")
//            	.sentence("color")
//            	.createQuery();
//			Query valueQuery = queryBuilder.phrase()
//	        	.onField("value")
//	        	.sentence("blue")
//	        	.createQuery();
//			Query allQuery = queryBuilder.bool()
//				.must(keyQuery)
//				.must(valueQuery)
//				.createQuery();
//			
//			System.out.println("Querying on " + allQuery);
//			CacheQuery query = searchManager.getQuery(allQuery, StringIndex.class);
//			List results = query.list();
//			System.out.println("Got " + results.size() + " results.");
//			for (Object result : results) {
//				System.out.println(result);
//				StringIndex si = (StringIndex)result;
//				assertEquals(si.toString(), stringIndexes[1].toString());
//			}
//
//			for (int i = 0; i < stringIndexes.length; i++) {
//				cache.remove("index" + i);
//			}
//		} catch (Exception ex) {
//			System.err.println("Caught exception: " + ex.getMessage());
//			ex.printStackTrace();
//		}
//	}
	
}

