package com.cloud.uspto.model;

public class ParserConstants {
	public static final String CUSTOM_META_DATA_URL = "custom_meta_data.url";
	public static final String CUSTOM_META_DATA_TITLE = "custom_meta_data.title";
	public static final String CUSTOM_META_DATA_COMPANYNAME = "custom_meta_data.companyname";
	public static final String CUSTOM_META_DATA_CUSTOMFILETYPE = "custom_meta_data.customFileType";
	public static final String META_RAW_UPLOADDATE = "meta.raw.UploadDate";
	public static final String META_RAW_COPYRIGHTYEAR = "meta.raw.copyrightYear";
	public static final String META_DATE = "meta.date";
	public static final String CUSTOM_META_DATA_COMPANYNAME_KEYWORD = "custom_meta_data.companyname.keyword";
	public static final String CUSTOM_META_DATA_CUSTOMFILETYPE_KEYWORD = "custom_meta_data.customFileType.keyword";
	public static final String MUST = "must";
	public static final String SOURCE = "Source";
	public static final String TERMS = "terms";
	public static final String FILE_TYPE = "File Type";
	public static final String FILTER = "filter";
	public static final String BOOL = "bool";
	public static final String QUERY = "query";
	public static final String FROM = "from";
	public static final String SIZE = "size";
	public static final String DATE = "date";
	public static final String SCORE = "score";
	public static final String DESC = "DESC";
	public static final String ASC = "ASC";
	
	//Field Search Constants
	public static final String DESCRIPTION = "description";
	public static final String CPC_CODES = "cpccodes";
	public static final String CPC_FIELD_PREFIX = "cpc_";
	public static final String CPC_FIELD_SEPERATOR = " ";

	// Sort Constants
	public static final String RELEVANCY = "relevancy";
	public static final String SORT = "sort";
	public static final String _SCORE = "_score";

	// HighLighter
	public static final String REQUIRE_FIELD_MATCH = "require_field_match";
	public static final String FRAGMENT_SIZE = "fragment_size";
	public static final String NUMBER_OF_FRAGMENTS = "number_of_fragments";
	public static final String FRAGMENTER = "fragmenter";
	public static final String _SOURCE = "_source";
	public static final String SIMPLE = "simple";
	public static final String SEARCH_CONTENT = "search_content";
	public static final String FIELDS = "fields";
	public static final String PRE_TAGS = "pre_tags";
	public static final String START = "<start>";
	public static final String POST_TAGS = "post_tags";
	public static final String END = "<end>";
	public static final String HIGHLIGHT = "highlight";
	public static final String HIGHLIGHT_QUERY = "highlight_query";
	public static final String FIELD = "field";
	public static final String AGGS = "aggs";
	public static final String BOOL_AND = "BOOL_AND";
	public static final String BOOL_OR = "BOOL_OR";
	public static final String AND = "AND";
	public static final String _ALL = "_all";

	// For Aggregation on Date
	public static final String META_UPLOADDATE = "meta.raw.UploadDate";

	public static final String CUSTOM_META_DATA_PUBLISH_DATE = "custom_meta_data.publishDate";

	public static final String CUSTOM_META_DATA_PUBLICATION_DATE = "custom_meta_data.publicationDate";
	public static final String META_RAW_COPYRIGHTYEAR_KEYWORD = "meta.raw.copyrightYear.keyword";
}
