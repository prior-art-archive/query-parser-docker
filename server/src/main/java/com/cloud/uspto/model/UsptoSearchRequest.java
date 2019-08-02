package com.cloud.uspto.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class UsptoSearchRequest {
	String searchQuery;
	String searchOperator;
	List<Filter> filters;
	int fetchHits;
	int fetchOffset;
	String sortBy;

	public int getFetchOffset() {
		return fetchOffset;
	}

	public void setFetchOffset(int fetchOffset) {
		this.fetchOffset = fetchOffset;
	}

	public String getSortBy() {
		return sortBy;
	}

	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}

	public int getFetchHits() {
		return fetchHits;
	}

	public void setFetchHits(int fetchHits) {
		this.fetchHits = fetchHits;
	}

	public String getSearchQuery() {
		return searchQuery;
	}

	public String getSearchOperator() {
		return searchOperator;
	}

	public void setSearchOperator(String searchOperator) {
		this.searchOperator = searchOperator;
	}


	public List<Filter> getFilters() {
		return filters;
	}

	public void setFilters(List<Filter> filters) {
		this.filters = filters;
	}

	public void setSearchQuery(String searchQuery) {
		this.searchQuery = searchQuery;
	}

	public UsptoSearchRequest() {
		super();
	}
}
