package org.drools.support.json;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.drools.support.MessagePayload;

/**
 * This accepts a JSON string, converts it to a Map representation, and uses
 * that as the actual payload. This is done to avoid having to parse the JSON
 * multiple times later on when the JSON values are accessed. A performance 
 * optimization is to keep it in String form and whenever it's used later on
 * use a streaming API.
 * 
 * @author Justin Hayes
 *
 */
public class JsonMessagePayload extends MessagePayload<Map<String, Object>> {
	
	private static final Log LOG = LogFactory.getLog(JsonMessagePayload.class);

	public JsonMessagePayload(String json) {
		super(JsonMessagePayload.convertToMap(json));
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> convertToMap(String json) {
		ObjectMapper mapper = new ObjectMapper();
		Map<String,Object> map = new HashMap<String, Object>();
		
		try {
			map = mapper.readValue(json, Map.class);
		} catch (JsonMappingException ex) {
			LOG.error("Caught JsonMappingException parsing JSON string: " + 
					ex.getMessage() + ". Returning empty map.", ex);
		} catch (JsonParseException ex) {
			LOG.error("Caught JsonParseException parsing JSON string: " + 
					ex.getMessage() + ". Returning empty map.", ex);
		} catch (IOException ex) {
			LOG.error("Caught IOException parsing JSON string: " + 
					ex.getMessage() + ". Returning empty map.", ex);
		}
		
		return map;
	}

}
