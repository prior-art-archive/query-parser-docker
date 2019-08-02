package com.cloud.uspto.model;

import java.util.List;

public class Filter {
	String filterName;
	List<String> filterData;
	public String getFilterName() {
		return filterName;
	}
	public void setFilterName(String filterName) {
		this.filterName = filterName;
	}
	public List<String> getFilterData() {
		return filterData;
	}
	public void setFilterData(List<String> filterData) {
		this.filterData = filterData;
	}

}
