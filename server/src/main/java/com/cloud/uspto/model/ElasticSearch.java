package com.cloud.uspto.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Properties;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.log4j.Logger;

public class ElasticSearch {

	static String esPort;
	static String esHost;
	static String esUsername;
	static String esPassword;
	static HttpUriRequest request;
	static Properties prop = new Properties();
	static InputStream input;

	//Get Log4j Logger
	final static Logger logger = Logger.getLogger(ElasticSearch.class);

	public static HttpUriRequest getEs() {
		if (request == null) {
			logger.info("HTTP Client Creation for the first time.");
			input = ElasticSearch.class.getClassLoader().getResourceAsStream(SearchConstants.configFile);
			if (input == null) {
				logger.info("Sorry, unable to find " + SearchConstants.configFile);
			}

			// load a properties file
			try {
				prop.load(input);
			} catch (IOException e) {
				logger.error("Properties File Missing", e);
			}

			// Check for Environment Variable for ES PORT

			if (System.getenv().get(SearchConstants.esPort) == null) {
				
				logger.info("Getting ES Details from Property File");

				esPort = prop.getProperty(SearchConstants.esPort);
			} else {
				esPort = System.getenv().get(SearchConstants.esPort);
			}

			// Check for Environment Variable for ES HOST

			if (System.getenv().get(SearchConstants.esHost) == null) {
				esHost = prop.getProperty(SearchConstants.esHost);
			} else {
				esHost = System.getenv().get(SearchConstants.esHost);
			}

			// Check for Environment Variable for ES USERNAME

			if (System.getenv().get(SearchConstants.esUsername) == null) {
				esUsername = prop.getProperty(SearchConstants.esUsername);
			} else {
				esUsername = System.getenv().get(SearchConstants.esUsername);
			}

			// Check for Environment Variable for ES HOST

			if (System.getenv().get(SearchConstants.esPassword) == null) {
				esPassword = prop.getProperty(SearchConstants.esPassword);
			} else {
				esPassword = System.getenv().get(SearchConstants.esPassword);
			}

			String httpRequest = esHost + SearchConstants.colon + esPort + SearchConstants.searchUrl;

			request = new HttpPost(httpRequest);

			String esCredentials = esUsername + SearchConstants.colon + esPassword;

			String encoding = Base64.getEncoder().encodeToString((new String(esCredentials).getBytes()));

			request.addHeader(SearchConstants.authorization, SearchConstants.basic + encoding);

			request.addHeader(SearchConstants.contentType, SearchConstants.applicationJson);

			return request;
		} else {
			return request;
		}

	}

}
