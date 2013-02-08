package org.jboss.tusk.ui;


import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.springframework.web.servlet.ModelAndView;

import com.sun.jersey.api.representation.Form;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

//TODO can we get rid of this???
public class IndexerController extends AbstractTuskController {
	
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest req,
			HttpServletResponse res) throws Exception {

		ModelAndView mav = new ModelAndView("indexer");
		
		String messageKey = "msg0";
		Map<String, Object> indexes = new HashMap<String, Object>();
		indexes.put("state", "co");
		indexes.put("patientId", "666666");
		
		Form f = new Form();
		f.add("indexes", serializeMap(indexes));
		Client c = Client.create();
		WebResource r = c.resource("http://localhost:8888/TuskUI/rest/indexer/add/" + messageKey);
		String indexResponse = r.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).accept(MediaType.TEXT_PLAIN).post(String.class, f);
		
		mav.addObject("messageKey", messageKey);
		mav.addObject("indexes", indexes);
		mav.addObject("indexResponse", indexResponse);
		
		return mav;
	}
	
	private String serializeMap(Map<String, Object> map) {
		StringBuffer buf = new StringBuffer();
		for (Entry<String, Object> entry : map.entrySet()) {
			buf.append(entry.getKey() + ":" + entry.getValue() + "|");
		}
		buf.delete(buf.length()-1, buf.length());
		
		return buf.toString();
	}

}
