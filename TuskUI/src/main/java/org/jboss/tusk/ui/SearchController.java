package org.jboss.tusk.ui;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.tusk.ispn.index.SearchCriterion;
import org.springframework.web.servlet.ModelAndView;


public class SearchController extends AbstractTuskController {
	
	private static final Log LOG = LogFactory.getLog(SearchController.class);
	
	private SearchHelper helper = null;
	
	public SearchController() {
		helper = new SearchHelper();
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
			LOG.debug("Doing query");
			List<SearchCriterion> searchCriteria = new ArrayList<SearchCriterion>();
			
			if (!isEmpty(field1) && !isEmpty(term1)) {
				searchCriteria.add(new SearchCriterion(field1, term1));
				LOG.debug("Added " + field1 + "=" + term1);
			}
			if (!isEmpty(field2) && !isEmpty(term2)) {
				searchCriteria.add(new SearchCriterion(field2, term2));
				LOG.debug("Added " + field2 + "=" + term2);
			}
			if (!isEmpty(field3) && !isEmpty(term3)) {
				searchCriteria.add(new SearchCriterion(field3, term3));
				LOG.debug("Added " + field3 + "=" + term3);
			}
			
			LOG.debug("About to run query");
			results = helper.doSearch(searchCriteria, operator);
			LOG.debug("Got " + results.size() + " results.");
		} else {
			LOG.debug("Nothing to search on.");
		}
		
		ModelAndView mav = new ModelAndView("search");

		mav.addObject("results", results);
		
		//load data for the matching messages
		if (results.size() > 0) {
			mav.addObject("messages", helper.loadData(results));
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
