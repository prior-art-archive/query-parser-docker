package com.cloud.uspto.parser;

import java.util.List;
import java.util.HashSet;
import java.util.Iterator;

import org.parboiled.Parboiled;
import org.parboiled.errors.ErrorUtils;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.parboiled.support.ParsingResult;

import com.cloud.uspto.model.Filter;
import com.cloud.uspto.model.ParserConstants;
import com.cloud.uspto.model.UsptoSearchRequest;
import com.cloud.uspto.parser.query.BoolParsingRules;
import com.cloud.uspto.parser.query.BoolParsingRules.BoolNode;
import com.cloud.uspto.parser.query.SpanParsingRules;
import com.cloud.uspto.parser.query.SpanParsingRules.SpanNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ElasticQueryParser {

	SpanParsingRules spanParser = Parboiled.createParser(SpanParsingRules.class);
	BoolParsingRules boolParser = Parboiled.createParser(BoolParsingRules.class);

	ObjectMapper mapper = new ObjectMapper();

	public String pasrseToElastic(UsptoSearchRequest sr) {

		ArrayNode mustClauses;
		ObjectNode bool;
		ObjectNode jsonQuery;
		ObjectNode json;
		Object obj;
		ObjectNode aggregation;
		ObjectNode fieldValue;
		ObjectNode termsNode;
		ObjectNode highlightNode = null;
		ObjectNode highlightQuery = null;
		ArrayNode sourceFields;
		ArrayNode terms;
		ArrayNode boolFilter = null;
		String cpcCode;
		Object boolQuery = null;
		Object spanQuery = null;
		List<Filter> filters = sr.getFilters();
		String sort = sr.getSortBy();
		String query = sr.getSearchQuery().toLowerCase();
		//String query = sr.getSearchQuery();
		HashSet<String> cpcCodes = new HashSet<String>();

		// Fields to be fetched from ES Response
		String[] fields = new String[] { ParserConstants.CUSTOM_META_DATA_URL, ParserConstants.CUSTOM_META_DATA_TITLE,
				ParserConstants.CUSTOM_META_DATA_COMPANYNAME, ParserConstants.META_RAW_UPLOADDATE,
				ParserConstants.META_RAW_COPYRIGHTYEAR, ParserConstants.META_DATE,
				ParserConstants.CUSTOM_META_DATA_PUBLISH_DATE, ParserConstants.CUSTOM_META_DATA_PUBLICATION_DATE, ParserConstants.CPC_CODES };

		// Aggregation Fields
		String[] aggregationFields = new String[] { ParserConstants.CUSTOM_META_DATA_COMPANYNAME,
				ParserConstants.CUSTOM_META_DATA_CUSTOMFILETYPE };

		// Aggregation Fields
		String aggregationDateFields = ParserConstants.CUSTOM_META_DATA_PUBLICATION_DATE;

		mustClauses = mapper.createArrayNode();
		obj = parseToBoolQuery(query, sr.getSearchOperator().toUpperCase(),cpcCodes);
		if (obj instanceof String) {
			// This sceanrio occurs if parser fails because of the rules. So we are submitting DSL query
			fieldValue = mapper.createObjectNode();
			
			
			termsNode = mapper.createObjectNode();
			termsNode.put("query",(String)obj);
			
			terms = mapper.createArrayNode();
			terms.add("search_content");
			termsNode.put("fields",terms);
			termsNode.put("default_operator","and");
			
			fieldValue.put("query_string",termsNode);
			
			
			mustClauses.add((JsonNode) fieldValue);
		}
		else{
			mustClauses.add((JsonNode) obj);
			boolQuery = obj;
			obj = parseToSpanQuery(query, sr.getSearchOperator().toUpperCase());
			if(obj != null){
				mustClauses.add((JsonNode) obj);
				spanQuery = obj;
			}
		}
		bool = mapper.createObjectNode();
		bool.replace(ParserConstants.MUST, mustClauses);

		/*jsonQuery = mapper.createObjectNode();
		jsonQuery.replace(ParserConstants.BOOL, bool);
		json = mapper.createObjectNode();

		json.replace(ParserConstants.QUERY, jsonQuery);*/

		json = mapper.createObjectNode();
		
		// Adding filters
		boolFilter = mapper.createArrayNode();
		if (filters != null) {
			
			for (Filter filter : filters) {
				if (filter.getFilterName().equals(ParserConstants.SOURCE)) {
					terms = mapper.createArrayNode();
					for (String str : filter.getFilterData()) {
						terms.add(str);
					}
					fieldValue = mapper.createObjectNode();
					fieldValue.replace(ParserConstants.CUSTOM_META_DATA_COMPANYNAME, terms);
					termsNode = mapper.createObjectNode();
					termsNode.replace(ParserConstants.TERMS, fieldValue);
					boolFilter.add((JsonNode) termsNode);
				}

				if (filter.getFilterName().equals(ParserConstants.FILE_TYPE)) {
					terms = mapper.createArrayNode();
					for (String str : filter.getFilterData()) {
						terms.add(str);
					}
					fieldValue = mapper.createObjectNode();
					fieldValue.replace(ParserConstants.CUSTOM_META_DATA_CUSTOMFILETYPE, terms);
					termsNode = mapper.createObjectNode();
					termsNode.replace(ParserConstants.TERMS, fieldValue);
					boolFilter.add((JsonNode) termsNode);
				}

				if (filter.getFilterName().equals("Date Range")) {
					fieldValue = mapper.createObjectNode();
					String rangeYears = null;

					for (String str : filter.getFilterData()) {
						if (str.equals("Last Ten Years")) {
							rangeYears = "now-10y";
						} else if (str.equals("Last Five Years")
								&& (rangeYears == null || rangeYears.equals("now-2y") || rangeYears.equals("now-1y"))) {
							rangeYears = "now-5y";
						} else if (str.equals("Last Two Years")
								&& (rangeYears == null || rangeYears.equals("now-1y"))) {
							rangeYears = "now-2y";
						} else if (str.equals("Last One Year") && rangeYears == null) {
							rangeYears = "now-1y";
						}
					}
					fieldValue.put("gte", rangeYears);
					ObjectNode fieldValueRange = mapper.createObjectNode();
					fieldValueRange.replace(ParserConstants.CUSTOM_META_DATA_PUBLICATION_DATE, fieldValue);
					termsNode = mapper.createObjectNode();
					termsNode.replace("range", fieldValueRange);
					boolFilter.add((JsonNode) termsNode);
				}
			}
			bool.replace(ParserConstants.FILTER, boolFilter);
			jsonQuery = mapper.createObjectNode();
			jsonQuery.replace(ParserConstants.BOOL, bool);

			json.replace(ParserConstants.QUERY, jsonQuery);
			
			//Commented below code and added filter as part of main BOOL query
			
			/*bool = mapper.createObjectNode();
			bool.replace(ParserConstants.MUST, boolFilter);

			jsonQuery = mapper.createObjectNode();
			jsonQuery.replace(ParserConstants.BOOL, bool);

			json.put("post_filter", jsonQuery);*/

		}

		// Adding from and size
		json.put(ParserConstants.FROM, sr.getFetchOffset());
		json.put(ParserConstants.SIZE, sr.getFetchHits());

		// Adding sort by
		terms = mapper.createArrayNode();
		fieldValue = mapper.createObjectNode();
		ObjectNode fieldValue1 = mapper.createObjectNode();
		if(cpcCodes.size() == 1){
			Iterator iter = cpcCodes.iterator();
			if(iter.hasNext()){
				cpcCode = (String)iter.next();
				if(cpcCode !=null){
					cpcCode = cpcCode.replace("/","_");
				}
				cpcCode = ParserConstants.CPC_FIELD_PREFIX + cpcCode;
				fieldValue1 = mapper.createObjectNode();
				fieldValue = mapper.createObjectNode();
				
				fieldValue1.put("unmapped_type", "float");
				fieldValue1.put("order", ParserConstants.DESC);
				fieldValue.put(cpcCode, fieldValue1);
				terms.add((JsonNode) fieldValue);
			}
		}else{
			if (sort == null || sort.equals(ParserConstants.DATE)) {
	
				fieldValue1.put("unmapped_type", "date");
				fieldValue1.put("order", ParserConstants.ASC);
				fieldValue.put(ParserConstants.CUSTOM_META_DATA_PUBLICATION_DATE, fieldValue1);
				terms.add((JsonNode) fieldValue);
	
				fieldValue1 = mapper.createObjectNode();
				fieldValue = mapper.createObjectNode();
	
				fieldValue1.put("unmapped_type", "keyword");
				fieldValue1.put("order", ParserConstants.ASC);
				fieldValue.put(ParserConstants.META_RAW_COPYRIGHTYEAR_KEYWORD, fieldValue1);
				terms.add((JsonNode) fieldValue);
	
				fieldValue1 = mapper.createObjectNode();
				fieldValue = mapper.createObjectNode();
	
				fieldValue1.put("unmapped_type", "date");
				fieldValue1.put("order", ParserConstants.ASC);
				fieldValue.put(ParserConstants.META_RAW_UPLOADDATE, fieldValue1);
				terms.add((JsonNode) fieldValue);
	
			} else if (sort.equals(ParserConstants.RELEVANCY)) {
				fieldValue.put(ParserConstants._SCORE, ParserConstants.DESC);
				terms.add((JsonNode) fieldValue);
			}
		}

		json.replace(ParserConstants.SORT, terms);

		// adding source fields to be included in the response.
		sourceFields = mapper.createArrayNode();
		for (String str : fields) {
			sourceFields.add(str);
		}
		json.replace(ParserConstants._SOURCE, sourceFields);

		// adding highlighting for field search_content
		fieldValue = mapper.createObjectNode();
		fieldValue.put(ParserConstants.REQUIRE_FIELD_MATCH, false);
		fieldValue.put(ParserConstants.FRAGMENT_SIZE, 100);
		fieldValue.put(ParserConstants.NUMBER_OF_FRAGMENTS, 3);
		fieldValue.put(ParserConstants.FRAGMENTER, ParserConstants.SIMPLE);
		if(spanQuery != null){
			mustClauses = mapper.createArrayNode();
			mustClauses.add((JsonNode) spanQuery);
			bool = mapper.createObjectNode();
			bool.replace(ParserConstants.MUST, mustClauses);
			highlightQuery  = mapper.createObjectNode();
			highlightQuery.put(ParserConstants.BOOL,bool);
			fieldValue.put(ParserConstants.HIGHLIGHT_QUERY, highlightQuery);
		}
		termsNode = mapper.createObjectNode();
		termsNode.put(ParserConstants.SEARCH_CONTENT, fieldValue);
		
		highlightNode = mapper.createObjectNode();
		highlightNode.put(ParserConstants.FIELDS, termsNode);
		
		// adding highlighting for field title
		fieldValue = mapper.createObjectNode();
		fieldValue.put(ParserConstants.REQUIRE_FIELD_MATCH, false);
		fieldValue.put(ParserConstants.NUMBER_OF_FRAGMENTS, 0);
		if(boolQuery != null){
			mustClauses = mapper.createArrayNode();
			mustClauses.add((JsonNode) boolQuery);
			bool = mapper.createObjectNode();
			bool.replace(ParserConstants.MUST, mustClauses);
			highlightQuery  = mapper.createObjectNode();
			highlightQuery.put(ParserConstants.BOOL,bool);
			fieldValue.put(ParserConstants.HIGHLIGHT_QUERY, highlightQuery);
		}
		termsNode.put(ParserConstants.CUSTOM_META_DATA_TITLE, fieldValue);

		highlightNode.put(ParserConstants.FIELDS, termsNode);
		highlightNode.put(ParserConstants.PRE_TAGS, ParserConstants.START);
		highlightNode.put(ParserConstants.POST_TAGS, ParserConstants.END);

		json.replace(ParserConstants.HIGHLIGHT, highlightNode);

		// adding aggregations required
		aggregation = mapper.createObjectNode();
		for (String str : aggregationFields) {
			fieldValue = mapper.createObjectNode();
			fieldValue.put(ParserConstants.FIELD, str);
			termsNode = mapper.createObjectNode();
			termsNode.replace(ParserConstants.TERMS, fieldValue);
			aggregation.replace(str, termsNode);
		}

		fieldValue = mapper.createObjectNode();
		fieldValue.put(ParserConstants.FIELD, aggregationDateFields);
		ArrayNode aggRanges = mapper.createArrayNode();
		ObjectNode range = mapper.createObjectNode();
		range.put("from", "now-1y");
		range.put("to", "now");
		range.put("key", "Last One Year");
		aggRanges.add(range);
		range = mapper.createObjectNode();
		range.put("from", "now-2y");
		range.put("to", "now");
		range.put("key", "Last Two Years");
		aggRanges.add(range);
		range = mapper.createObjectNode();
		range.put("from", "now-5y");
		range.put("to", "now");
		range.put("key", "Last Five Years");
		aggRanges.add(range);
		range = mapper.createObjectNode();
		range.put("from", "now-10y");
		range.put("to", "now");
		range.put("key", "Last Ten Years");
		aggRanges.add(range);
		fieldValue.put("ranges", aggRanges);
		termsNode = mapper.createObjectNode();
		termsNode.replace("date_range", fieldValue);
		aggregation.replace("range", termsNode);

		json.replace(ParserConstants.AGGS, aggregation);

		return json.toString().replace(ParserConstants.BOOL_AND, ParserConstants.BOOL).replace(ParserConstants.BOOL_OR,
				ParserConstants.BOOL);
	}

	public Object parseToBoolQuery(String query, String defaultOperator, HashSet<String> cpcCode) {
		Object value;
		ParsingResult<?> result = new RecoveringParseRunner(boolParser.InputLine()).run(query);

		if (result.hasErrors()) {
			System.out.println((ErrorUtils.printParseError(result.parseErrors.get(0))));
			value = (Object) query;
			return value;
		} else {
			value = result.parseTreeRoot.getValue();
			BoolNode boolNode = BoolNode.class.cast(value);
			if (defaultOperator != null && defaultOperator.equalsIgnoreCase("OR")) {
				defaultOperator = "OR";
			} else {
				defaultOperator = "AND";
			}
			return boolNode.getValue(defaultOperator,cpcCode);
		}
	}

	public Object parseToSpanQuery(String query, String operator) {
		Object value;
		ParsingResult<?> result = new RecoveringParseRunner(spanParser.InputLine()).run(query);

		if (result.hasErrors()) {
			value = (Object) query;
			return value;
		} else {
			value = result.parseTreeRoot.getValue();
			SpanNode spanNode = SpanNode.class.cast(value);
			spanNode.setDefaultField(ParserConstants._ALL);
			return spanNode.getValue(operator);
		}
	}
}
