package org.jboss.tusk.ispn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.tusk.ispn.InfinispanService;
import org.jboss.tusk.ispn.index.SearchCriterion;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * This requires that HBase is running on the local system.
 * @author justin
 *
 */
public class InfinispanServiceTest {
	
	private InfinispanService ispnService = null;

	private String documentId1;
	private Map<String, Object> documentFields1 = new HashMap<String, Object>();

	private String documentId2;
	private Map<String, Object> documentFields2 = new HashMap<String, Object>();

	private List<SearchCriterion> orSingleCriterion = new ArrayList<SearchCriterion>();
	private List<SearchCriterion> orMultipleCriteria1 = new ArrayList<SearchCriterion>();
	private List<SearchCriterion> orMultipleCriteria2 = new ArrayList<SearchCriterion>();
	private List<SearchCriterion> andSingleCriterion = new ArrayList<SearchCriterion>();
	private List<SearchCriterion> andMultipleCriteria1 = new ArrayList<SearchCriterion>();
	private List<SearchCriterion> andMultipleCriteria2 = new ArrayList<SearchCriterion>();
	
	static {
	}
	
	@Before
	public void setup() throws Exception {
		System.setProperty("java.net.preferIPv4Stack", "true");
		
		ispnService = new InfinispanService();
		
		//data for document 1
		documentId1 = "documentId1";
		documentFields1.put("patientId", "123456");
		documentFields1.put("state", "dc");
		documentFields1.put("diseases", "flu, measles, pertussis");
		
		//data for document 2
		documentId2 = "documentId2";
		documentFields2.put("patientId", "999999");
		documentFields2.put("state", "va");
		documentFields2.put("disease", "mumps, zombie, cholera");
		
		//OR search with just a single criterion
		orSingleCriterion.add(new SearchCriterion("state", "dc"));

		//OR search with multiple criteria 1
		orMultipleCriteria1.add(new SearchCriterion("state", "dc"));
		orMultipleCriteria1.add(new SearchCriterion("disease", "smallpox"));

		//OR search with multiple criteria 1
		orMultipleCriteria2.add(new SearchCriterion("state", "dc"));
		orMultipleCriteria2.add(new SearchCriterion("disease", "zombie"));

		//AND search with just a single criterion
		andSingleCriterion.add(new SearchCriterion("disease", "cholera"));

		//AND search with multiple criteria 1
		andMultipleCriteria1.add(new SearchCriterion("state", "va"));
		andMultipleCriteria1.add(new SearchCriterion("disease", "zombie"));

		//AND search with multiple criteria 2
		andMultipleCriteria2.add(new SearchCriterion("state", "dc"));
		andMultipleCriteria2.add(new SearchCriterion("disease", "zombie"));

		Set<String> keys = new HashSet<String>();
		keys.add("disease");
		keys.add("state");
		keys.add("patientId");
		//for (int i = 0; i < 100; i++) {
		//	ispnService.removeIndex("msg" + i, keys);
		//}
	}
	
	@After
	public void tearDown() throws Exception {
		ispnService.removeIndex(documentId1, documentFields1.keySet());
		ispnService.removeIndex(documentId2, documentFields2.keySet());

//		ispnService.clearCache();
		//ispnService.stopCache();
	}

	@Test
	public void testWriteIndex() throws Exception {
		//write indexes
		ispnService.writeIndex(documentId1, documentFields1);
		ispnService.writeIndex(documentId2, documentFields2);
		
		//now do searches
		
		List<String> results = ispnService.searchIndex(orSingleCriterion, false);
		assertEquals(1, results.size());
		for (String docId : results) {
			assertEquals(docId, documentId1);
		}
		
		results = ispnService.searchIndex(orMultipleCriteria1, false);
		assertEquals(1, results.size());
		for (String docId : results) {
			assertEquals(docId, documentId1);
		}
		
		results = ispnService.searchIndex(orMultipleCriteria2, false);
		assertEquals(2, results.size());
		for (String docId : results) {
			assertTrue(docId.equals(documentId1) || docId.equals(documentId2));
		}
		
		results = ispnService.searchIndex(andSingleCriterion, true);
		assertEquals(1, results.size());
		for (String docId : results) {
			assertEquals(docId, documentId2);
		}
		
		results = ispnService.searchIndex(andMultipleCriteria1, true);
		assertEquals(1, results.size());
		for (String docId : results) {
			assertEquals(docId, documentId2);
		}
		
		results = ispnService.searchIndex(andMultipleCriteria2, true);
		assertEquals(0, results.size());
	}

}
