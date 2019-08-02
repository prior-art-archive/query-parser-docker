package com.cloud.uspto.model;

//Put all the Constant Variables here

public class SearchConstants {
	public static final String searchUrl = "/uspto/_search";
	public static final String esHost = "ES_HOST";
	public static final String esPort = "ES_PORT";
	public static final String esResults = "/esresults";
	public static final String debug = "debug";
	public static final String esUsername = "ES_USERNAME";
	public static final String esPassword = "ES_PASSWORD";
	public static final String authorization = "Authorization";
	public static final String basic = "Basic ";
	public static final String contentType = "Content-Type";
	public static final String applicationJson = "application/json";
	public static final String facetName = "facetName";
	public static final String source = "Source";
	public static final String buckets = "buckets";
	public static final String key = "key";
	public static final String aggregations = "aggregations";
	public static final String value = "value";
	public static final String companyName = "custom_meta_data.companyname";
	public static final String docCount = "doc_count";
	public static final String data = "data";
	public static final String fileType = "custom_meta_data.customFileType";
	public static final String fileTypeFacet = "File Type";
	public static final String searchQuery = "searchQuery";
	public static final String hits = "hits";
	public static final String esSource = "_source";
	public static final String highlight = "highlight";
	public static final String customMetaData = "custom_meta_data";
	public static final String meta = "meta";
	public static final String raw = "raw";
	public static final String publicationDate = "publicationDate";
	public static final String uploadDate = "uploadDate";
	public static final String copyright = "copyright";
	public static final String date = "publishDate";
	public static final String uploadDateValue = "UploadDate";
	public static final String copyrightYear = "copyrightYear";
	public static final String url = "url";
	public static final String title = "title";
	public static final String sourceRequest = "source";
	public static final String origin = "origin";
	public static final String teaser = "teaser";
	public static final String searchContent = "search_content";
	public static final String totalHits = "totalHits";
	public static final String total = "total";
	public static final String usptoResponses = "usptoResponses";
	public static final String facets = "facets";
	public static final String debugQuery = "debugQuery";
	public static final String startTag = "<start>";
	public static final String endTag = "<end>";
	public static final String startBoldTag = "<b>";
	public static final String endBoldTag = "</b>";
	public static final String colon = ":";
	public static final String findTeaser1 = "\",\"";
	public static final String replaceTeaser1 = "...";
	public static final String findTeaser2 = "\\n";
	public static final String replaceTeaser2 = " ";
	public static final String findTeaser3 = "[\"";
	public static final String replaceTeaser3 = "";
	public static final String findTeaser4 = "\"]";
	public static final String replaceTeaser4 = "";
	public static final String esCompanyName = "companyname";
	public static final String usptoQuery = "USPTO_QUERY";
	public static final String trueDebug = "true";
	public static final String configFile = "config.properties";
	public static final String cpcCodes = "cpccodes";
	
	public static final String noTitle = "Title";

	// Constants to Add Exception Details in Rest Response

	public static final String errorCode = "errorCode";
	public static final String errorMessage = "errorMessage";
}
