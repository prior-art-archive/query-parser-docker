package com.cloud.uspto.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.cloud.uspto.model.SearchConstants;
import com.cloud.uspto.model.UsptoSearchRequest;
import com.cloud.uspto.service.UsptoService;

public class UsptoServiceTest {

	// Get Log4j Logger
	final static Logger logger = Logger.getLogger(UsptoServiceTest.class);

	@Test
	public void testUsptoService() throws ClientProtocolException, IOException, JSONException {

		String usptoQuery = "{\"searchQuery\":\"cisco.ti.\",\"searchOperator\":\"AND\",\"filters\":[],\"fetchHits\":10,\"fetchOffset\":0}";

		String debug = SearchConstants.trueDebug;

		UsptoService us = new UsptoService();

		UsptoSearchRequest sr = new UsptoSearchRequest();

		ObjectMapper mapper = new ObjectMapper();

		sr = mapper.readValue(usptoQuery, UsptoSearchRequest.class);

		String usptoResponse = us.getResults(sr, debug);

		JSONObject jsonResponse = new JSONObject(usptoResponse);

		// Test the Search Query is Same as Request

		assertEquals(sr.getSearchQuery(), jsonResponse.get("searchQuery"));

		// Check when the debug is true , debugQuery is added in the Response
		assertEquals(SearchConstants.trueDebug, Boolean.toString(jsonResponse.has("debugQuery")));

	}

	@Test
	public void testUsptoServiceFilters() throws ClientProtocolException, IOException, JSONException {

		String usptoQuery = "{\"searchQuery\":\"cisco systems\",\"searchOperator\":\"AND\",\"filters\":[{\"filterName\":\"Source\",\"filterData\":[\"Cisco\"]}],\"fetchHits\":10,\"fetchOffset\":0,\"sortBy\":null}";
		;

		String debug = SearchConstants.trueDebug;

		UsptoService us = new UsptoService();

		UsptoSearchRequest sr = new UsptoSearchRequest();

		ObjectMapper mapper = new ObjectMapper();

		sr = mapper.readValue(usptoQuery, UsptoSearchRequest.class);

		String usptoResponse = us.getResults(sr, debug);

		JSONObject jsonResponse = new JSONObject(usptoResponse);

		// Test the Search Query is Same as Request

		assertEquals(sr.getSearchQuery(), jsonResponse.get("searchQuery"));

	}

	@Test
	public void testUsptoServiceDateFilters() throws ClientProtocolException, IOException, JSONException {

		String usptoQuery = "{\"searchQuery\":\"cisco\",\"searchOperator\":\"AND\",\"filters\":[{\"filterName\":\"Date Range\",\"filterData\":[\"Last One Year\",\"Last Two Years\",\"Last Ten Years\",\"Last Five Years\"]}],\"fetchHits\":10,\"fetchOffset\":0}";

		UsptoService us = new UsptoService();

		UsptoSearchRequest sr = new UsptoSearchRequest();

		ObjectMapper mapper = new ObjectMapper();

		sr = mapper.readValue(usptoQuery, UsptoSearchRequest.class);

		String usptoResponse = us.getResults(sr, "false");

		JSONObject jsonResponse = new JSONObject(usptoResponse);

		// Test the Search Query is Same as Request

		assertEquals(sr.getSearchQuery(), jsonResponse.get("searchQuery"));

	}
	
	@Test
	public void testRegressionCases() throws ClientProtocolException, IOException, JSONException {
		
		String usptoResponse;
		String usptoQuery;
		JSONObject jsonResponse;
		
		UsptoService us = new UsptoService();

		UsptoSearchRequest sr = new UsptoSearchRequest();

		ObjectMapper mapper = new ObjectMapper();
		
		//Query in Uppercase
		
		usptoQuery = "{\"searchQuery\":\"CISCO\",\"searchOperator\":\"AND\",\"filters\":[],\"fetchHits\":10,\"fetchOffset\":0}";

		sr = mapper.readValue(usptoQuery, UsptoSearchRequest.class);

		usptoResponse = us.getResults(sr, "false");

		jsonResponse = new JSONObject(usptoResponse);

		// Test the Search Query is Same as Request

		assertEquals(sr.getSearchQuery(), jsonResponse.get("searchQuery"));
		
		
		//Hyphenated query
		usptoQuery = "{\"searchQuery\":\"MAC-layer\",\"searchOperator\":\"AND\",\"filters\":[],\"fetchHits\":10,\"fetchOffset\":0}";

		sr = mapper.readValue(usptoQuery, UsptoSearchRequest.class);

		usptoResponse = us.getResults(sr, "false");

		jsonResponse = new JSONObject(usptoResponse);

		// Test the Search Query is Same as Request

		assertEquals(sr.getSearchQuery(), jsonResponse.get("searchQuery"));

		//Lemmatized query
		usptoQuery = "{\"searchQuery\":\"backed\",\"searchOperator\":\"AND\",\"filters\":[],\"fetchHits\":10,\"fetchOffset\":0}";

		sr = mapper.readValue(usptoQuery, UsptoSearchRequest.class);

		usptoResponse = us.getResults(sr, "false");

		jsonResponse = new JSONObject(usptoResponse);

		// Test the Search Query is Same as Request

		assertEquals(sr.getSearchQuery(), jsonResponse.get("searchQuery"));
		
		//Lemmatized query
		usptoQuery = "{\"searchQuery\":\"cisco ADJ router AND AppleTalk.ti.\",\"searchOperator\":\"AND\",\"filters\":[],\"fetchHits\":10,\"fetchOffset\":0}";

		sr = mapper.readValue(usptoQuery, UsptoSearchRequest.class);

		usptoResponse = us.getResults(sr, "false");

		jsonResponse = new JSONObject(usptoResponse);

		// Test the Search Query is Same as Request

		assertEquals(sr.getSearchQuery(), jsonResponse.get("searchQuery"));
		
		//Fielded query
		usptoQuery = "{\"searchQuery\":\"tests.ti. AND bootstrap.ti.\",\"searchOperator\":\"AND\",\"filters\":[],\"fetchHits\":10,\"fetchOffset\":0}";

		sr = mapper.readValue(usptoQuery, UsptoSearchRequest.class);

		usptoResponse = us.getResults(sr, "false");

		jsonResponse = new JSONObject(usptoResponse);

		// Test the Search Query is Same as Request

		assertEquals(sr.getSearchQuery(), jsonResponse.get("searchQuery"));
		
		
		//Fielded query with expression
		usptoQuery = "{\"searchQuery\":\"(tests AND bootstrap).ti,ab.\",\"searchOperator\":\"AND\",\"filters\":[],\"fetchHits\":10,\"fetchOffset\":0}";
	
		sr = mapper.readValue(usptoQuery, UsptoSearchRequest.class);
	
		usptoResponse = us.getResults(sr, "false");
	
		jsonResponse = new JSONObject(usptoResponse);
	
		// Test the Search Query is Same as Request
	
		assertEquals(sr.getSearchQuery(), jsonResponse.get("searchQuery"));
		
		
		usptoQuery = "{\"searchQuery\":\"abc~3\",\"searchOperator\":\"AND\",\"filters\":[],\"fetchHits\":10,\"fetchOffset\":0}";
		
		sr = mapper.readValue(usptoQuery, UsptoSearchRequest.class);
	
		usptoResponse = us.getResults(sr, "false");
	
		jsonResponse = new JSONObject(usptoResponse);
	
		// Test the Search Query is Same as Request
	
		assertEquals(sr.getSearchQuery(), jsonResponse.get("searchQuery"));
		
		
		
		usptoQuery = "{\"searchQuery\":\"\\\"RFC 903\\\"~3\",\"searchOperator\":\"AND\",\"filters\":[],\"fetchHits\":10,\"fetchOffset\":0}";
		
		sr = mapper.readValue(usptoQuery, UsptoSearchRequest.class);
	
		usptoResponse = us.getResults(sr, "false");
	
		jsonResponse = new JSONObject(usptoResponse);
	
		// Test the Search Query is Same as Request
	
		assertEquals(sr.getSearchQuery(), jsonResponse.get("searchQuery"));
		
		
		/*usptoQuery = "{\"searchQuery\":\"\\\"RFC 903\\\"~3 AND H04L45/04.CPC.\",\"searchOperator\":\"AND\",\"filters\":[],\"fetchHits\":10,\"fetchOffset\":0}";
		
		sr = mapper.readValue(usptoQuery, UsptoSearchRequest.class);
	
		usptoResponse = us.getResults(sr, "false");
	
		jsonResponse = new JSONObject(usptoResponse);
	
		// Test the Search Query is Same as Request
	
		assertEquals(sr.getSearchQuery(), jsonResponse.get("searchQuery"));*/
		
		

	}

}
