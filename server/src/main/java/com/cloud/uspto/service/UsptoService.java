package com.cloud.uspto.service;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.time.StopWatch;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.cloud.uspto.model.ElasticSearch;
import com.cloud.uspto.model.SearchConstants;
import com.cloud.uspto.model.UsptoSearchRequest;
import com.cloud.uspto.parser.ElasticQueryParser;
import com.fasterxml.jackson.databind.ObjectMapper;

@Path(SearchConstants.esResults)
public class UsptoService {

	// Get Log4j Logger
	final static Logger applicationLogger = Logger.getLogger("applicationLogger");
	final static Logger perfLogger = Logger.getLogger("perfLogger");

	// Get Parser Instance

	ElasticQueryParser elasticQueryParser = new ElasticQueryParser();

	// POST Rest API Which takes Json Input and Produces Json
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String getResults(UsptoSearchRequest sr, @QueryParam(SearchConstants.debug) String debug)
			throws JsonGenerationException, JsonMappingException, IOException, JSONException {

		// Setting the Error Message for Success full Request

		String errorCode = "";
		String errorMessage = "";

		JSONObject timeLogger = new JSONObject();

		// Get all the settings from environment variables / properties file and
		// set the http request

		HttpUriRequest request = ElasticSearch.getEs();

		// Objectmapper to Log Request

		ObjectMapper mapper = new ObjectMapper();

		applicationLogger.info("Search Request:" + mapper.writeValueAsString(sr));

		StopWatch s1 = new StopWatch();

		s1.start();

		String querySource = elasticQueryParser.pasrseToElastic(sr);

		timeLogger.put("parserTime", s1.getTime());

		s1.stop();

		s1.reset();

		applicationLogger.info("Parser Output:" + querySource);
		
		/*if( sr.getSearchQuery() != null && !sr.getSearchQuery().isEmpty() && sr.getSearchQuery().equalsIgnoreCase(querySource)){
			querySource = "{ \"query\": { \"term\" : { \"invalidfield\" : \"invalidvalue\" } } }";
		}
*/
		StringEntity params = new StringEntity(querySource);

		((HttpEntityEnclosingRequestBase) request).setEntity(params);

		ResponseHandler<String> responseHandler = new BasicResponseHandler();

		s1.start();

		// Request ES using HTTP using response Handler

		String responseBody = HttpClientBuilder.create().build().execute(request, responseHandler);

		timeLogger.put("elasticTime", s1.getTime());

		s1.stop();

		s1.reset();

		applicationLogger.info("Elastic Search Response:" + responseBody);

		s1.start();

		// Parse the Elastic Search Response and set the return Json

		JSONObject jsonResponse = new JSONObject(responseBody);

		JSONArray facets = new JSONArray();

		JSONObject aggregations = jsonResponse.getJSONObject(SearchConstants.aggregations);

		JSONObject aggregation = aggregations.getJSONObject(SearchConstants.companyName);

		JSONObject facet = new JSONObject();

		facet.put(SearchConstants.facetName, SearchConstants.source);

		JSONArray facetResults = new JSONArray();

		JSONArray aggBuckets = aggregation.getJSONArray(SearchConstants.buckets);

		for (int i = 0; i < aggBuckets.length(); i++) {

			JSONObject companyNameAggBucket = aggBuckets.getJSONObject(i);

			JSONObject facetResult = new JSONObject();

			facetResult.append(SearchConstants.key, companyNameAggBucket.get(SearchConstants.key));
			facetResult.append(SearchConstants.value, companyNameAggBucket.get(SearchConstants.docCount));

			facetResults.put(facetResult);

		}

		facet.put(SearchConstants.data, facetResults);

		facets.put(facet);

		aggregation = aggregations.getJSONObject(SearchConstants.fileType);

		facet = new JSONObject();

		facet.put(SearchConstants.facetName, SearchConstants.fileTypeFacet);

		facetResults = new JSONArray();

		aggBuckets = aggregation.getJSONArray(SearchConstants.buckets);

		for (int i = 0; i < aggBuckets.length(); i++) {

			JSONObject companyNameAggBucket = aggBuckets.getJSONObject(i);

			JSONObject facetResult = new JSONObject();

			facetResult.append(SearchConstants.key, companyNameAggBucket.get(SearchConstants.key));
			facetResult.append(SearchConstants.value, companyNameAggBucket.get(SearchConstants.docCount));

			facetResults.put(facetResult);

		}

		facet.put(SearchConstants.data, facetResults);

		facets.put(facet);

		aggregation = aggregations.getJSONObject("range");

		facet = new JSONObject();

		facet.put(SearchConstants.facetName, "Date Range");

		facetResults = new JSONArray();

		aggBuckets = aggregation.getJSONArray(SearchConstants.buckets);

		for (int i = 0; i < aggBuckets.length(); i++) {

			JSONObject companyNameAggBucket = aggBuckets.getJSONObject(i);

			JSONObject facetResult = new JSONObject();

			facetResult.append(SearchConstants.key, companyNameAggBucket.get(SearchConstants.key));
			facetResult.append(SearchConstants.value, companyNameAggBucket.get(SearchConstants.docCount));

			facetResults.put(facetResult);

		}

		facet.put(SearchConstants.data, facetResults);

		facets.put(facet);

		JSONObject json = new JSONObject();

		json.put(SearchConstants.searchQuery, sr.getSearchQuery());

		JSONObject hits = jsonResponse.getJSONObject(SearchConstants.hits);

		JSONArray esHits = hits.getJSONArray(SearchConstants.hits);

		JSONArray usptoResponses = new JSONArray();

		// Get the Hits from ES Response and Add it to Rest Response
		JSONObject esSingleHitSourceHighlight;
		for (int i = 0; i < esHits.length(); i++) {
			esSingleHitSourceHighlight = null;
			JSONObject esSingleHit = esHits.getJSONObject(i);

			JSONObject esSingleHitSource = esSingleHit.getJSONObject(SearchConstants.esSource);

			if(esSingleHit.has(SearchConstants.highlight)){
				esSingleHitSourceHighlight = esSingleHit.getJSONObject(SearchConstants.highlight);
			}

			JSONObject esSingleHitSourceCustomData = esSingleHitSource.getJSONObject(SearchConstants.customMetaData);

			JSONObject usptoResponse = new JSONObject();
			
			// Check whether CPC Codes Exists
			if (esSingleHitSource.has(SearchConstants.cpcCodes)) {
				usptoResponse.append(SearchConstants.cpcCodes,
						esSingleHitSource.get(SearchConstants.cpcCodes));
			}

			// Check whether Publication Date Exists
			if (esSingleHitSourceCustomData.has(SearchConstants.publicationDate)) {
				usptoResponse.append(SearchConstants.publicationDate,
						esSingleHitSourceCustomData.get(SearchConstants.publicationDate));
			}

			if (esSingleHitSource.has(SearchConstants.meta)) {
				JSONObject esSingleHitSourceMeta = esSingleHitSource.getJSONObject(SearchConstants.meta);
				
				if(esSingleHitSourceMeta.has(SearchConstants.raw)){
					JSONObject esSingleHitSourceMetaRaw = esSingleHitSourceMeta.getJSONObject(SearchConstants.raw);
					
					if(esSingleHitSourceMetaRaw.has(SearchConstants.uploadDateValue)){
						usptoResponse.append(SearchConstants.uploadDate,
							esSingleHitSourceMetaRaw.get(SearchConstants.uploadDateValue));
					}
					// Check whether Copyright Year Exists
					if (esSingleHitSourceMetaRaw.has(SearchConstants.copyrightYear)) {
						usptoResponse.append(SearchConstants.copyright,
								esSingleHitSourceMetaRaw.get(SearchConstants.copyrightYear));
					}
				}

			}

			usptoResponse.append(SearchConstants.url, esSingleHitSourceCustomData.get(SearchConstants.url));

			// Check if title Exists
			if (esSingleHitSourceCustomData.has(SearchConstants.title)) {
				usptoResponse.append(SearchConstants.title, esSingleHitSourceCustomData.get(SearchConstants.title));
			}else{
				usptoResponse.append(SearchConstants.title, SearchConstants.noTitle);
			}
			//usptoResponse.append(SearchConstants.title, esSingleHitSourceCustomData.get(SearchConstants.title));

			usptoResponse.append(SearchConstants.sourceRequest,
					esSingleHitSourceCustomData.get(SearchConstants.esCompanyName));

			// Modify the Highlighted results to properly display it in UI

			if(esSingleHitSourceHighlight != null){
				usptoResponse.append(SearchConstants.teaser,
						esSingleHitSourceHighlight.get(SearchConstants.searchContent).toString()
								.replace(SearchConstants.findTeaser1, SearchConstants.replaceTeaser1)
								.replace(SearchConstants.findTeaser2, SearchConstants.replaceTeaser2)
								.replace(SearchConstants.findTeaser3, SearchConstants.replaceTeaser3)
								.replace(SearchConstants.findTeaser4, SearchConstants.replaceTeaser4).replace("\\t", ""));
			}
			usptoResponses.put(usptoResponse);
		}

		json.put(SearchConstants.totalHits, hits.getLong(SearchConstants.total));

		json.put(SearchConstants.usptoResponses, usptoResponses);

		json.put(SearchConstants.facets, facets);

		// Set Error Message and Error Code
		json.put(SearchConstants.errorCode, errorCode);
		json.put(SearchConstants.errorMessage, errorMessage);

		timeLogger.put("responseParseTime", s1.getTime());

		s1.stop();

		perfLogger.info(timeLogger);

		// If Debug is enabled , Add the debug query with Response

		if (Boolean.parseBoolean(debug)) {

			JSONObject debugQuery = new JSONObject(querySource);
			debugQuery.put("performanceStats", timeLogger);

			json.put(SearchConstants.debugQuery, debugQuery.toString());

			// json.put("performanceStats", timeLogger);
		}

		applicationLogger.info(
				"Rest API Response: " + json.toString().replace(SearchConstants.startTag, SearchConstants.startBoldTag)
						.replace(SearchConstants.endTag, SearchConstants.endBoldTag));

		return json.toString().replace(SearchConstants.startTag, SearchConstants.startBoldTag)
				.replace(SearchConstants.endTag, SearchConstants.endBoldTag); // Replace
																				// Tab
																				// Characters

	}
}
