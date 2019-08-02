import {Injectable} from '@angular/core';
import {Http, Headers, Response} from '@angular/http';
import {Observable} from 'rxjs';
import 'rxjs/add/operator/toPromise';
import {Filter} from './Filter';
import { FacetComponent } from './facet.component';

@Injectable()
export class SharedService {
  usptoRestUrl = '/rest/esresults';
  totReqsMade = 0;
  printHistory=[];
  filterObject = {};
  selectedFilterObject={};
  json_request = {};
  queryArray = [];
  filtersAll=[];//To keep track of all the filters applied
  selectedFilters=[];
  fetchHits=10; 
  fetchOffset=0;
  debug=false;
  sortBy='date';
  //Arrays to maintain array of filters that can be applied
  source=[];
  fileType=[];
  dateRange=[];
  flagSource=0;
  flagFileType=0;
  flagDateRange=0;
  //Initialised to default operator
  operatorName='AND';
  constructor(private _http: Http) {}

//This service is triggered when the user selects one of the filtercount values from the dropdown
getFilterCount(filterCount){
this.fetchHits=filterCount;
console.log("The filter count is",filterCount);
return;
}

//This service is triggered after the offset is set. i.e., if the user navigates from page 1 to 2 and the number of results per page is 15. Then the offset
//is computed as (2-1)*15
getFetchOffset(offset){
this.fetchOffset=offset;
console.log("The fetch offset is",this.fetchOffset);
return;
}

getSortOrder(sort){
  this.sortBy=sort;
  console.log("The sort order value is",this.sortBy);
  return;
}

getOperator(op){
this.operatorName=op;
console.log("The operator is",this.operatorName);
}
// Service to get the results from the web service
getResults(query) {
var d = new Date();
var utc = d.getTime();
console.log("The current time in UTC is",new Date(utc));
var queryJson;
console.log("Entered getResults() method");

      const headers = {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      'Access-Control-Allow-Headers': 'Content-Type',
    };
    const headerObj = {headers: new Headers(headers)};
    this.json_request = {searchQuery: query, searchOperator: this.operatorName, filters: [], fetchHits:this.fetchHits,fetchOffset:this.fetchOffset,sortBy:this.sortBy};
    
    this.totReqsMade = this.totReqsMade + 1;
if(query!=this.queryArray[this.queryArray.length-1])
{
  console.log("Entered if block");
  this.queryArray.push(query);
  this.printHistory.push({"query":query,"timestamp":new Date(utc),"operator":this.json_request['searchOperator'],"Numberofhits":45,"Database":"All"});
  console.log("The printHistory object is",this.printHistory);
}

    console.log(JSON.stringify(this.json_request));
    if(this.debug==true && this.usptoRestUrl.indexOf('debug') == -1){
      this.usptoRestUrl+='?debug=true';
    }
    return this._http.post(this.usptoRestUrl, JSON.stringify(this.json_request), headerObj)
      .map(response => {
        {
          console.log("The response json is",response.json());
          return response.json(); }
      })
      .catch(error => Observable.throw(error.json()));
  }

  // Service to get the filtered results from Web Service
  // request parameters contain filter object and the query
  getFilteredResults(query, filter) {
    var d = new Date();
    var utc = d.getTime();
    console.log("The current time in UTC is",new Date(utc));
    var queryJson;
    this.json_request = {searchQuery: query, searchOperator: this.operatorName, filters: [], fetchHits: this.fetchHits,fetchOffset:this.fetchOffset};
    if (filter !== null) {
    this.source=[];
    this.fileType=[];
    this.dateRange=[];
    for(var i=0;i<filter.name.length;i++){
      if(filter.name[i]=='Source'){
        if (this.source.indexOf(filter.value[i]) == -1) {
      if(filter.value[i]!=null){
      this.source.push(filter.value[i]);
      this.flagSource=0;
      }}}
      if(filter.name[i]=='File Type'){
      if (this.fileType.indexOf(filter.value[i]) == -1) {
      if(filter.value[i]!=null){
      this.fileType.push(filter.value[i]);
      this.flagFileType=0;
      }}}
      if(filter.name[i]=='Date Range'){
      if (this.dateRange.indexOf(filter.value[i]) == -1) {
      if(filter.value[i]!=null){
      this.dateRange[0]=filter.value[i];}
      this.flagDateRange=0;
      }}}
     
     this.filterObject={};
     this.filtersAll=[];
     this.selectedFilterObject={};
     this.selectedFilters=[];
     if(this.source.length>0 && !this.flagSource){
     this.filterObject={filterName:'Source',filterData:this.source};
     this.selectedFilterObject={facetName:'Source',data:this.source};
     this.filtersAll.push(this.filterObject);
     this.selectedFilters.push(this.selectedFilterObject);
     this.flagSource=1; 
    }

      if(this.fileType.length>0 && !this.flagFileType){
     this.filterObject={filterName:'File Type',filterData:this.fileType};
     this.filtersAll.push(this.filterObject);
     this.selectedFilterObject={facetName:'File Type',data:this.fileType};
     this.selectedFilters.push(this.selectedFilterObject);
     this.flagFileType=1;
     }
     
      if(this.dateRange.length>0 && !this.flagDateRange){
     this.filterObject={filterName:'Date Range',filterData:this.dateRange};
     this.filtersAll.push(this.filterObject);
     this.selectedFilterObject={facetName:'Date Range',data:this.dateRange};
     this.selectedFilters.push(this.selectedFilterObject);
     this.flagDateRange=1;
     }
      console.log("The filterObject and filtersAll is",this.filterObject,this.filtersAll);
      
    this.json_request = {searchQuery: query, searchOperator: this.operatorName, filters: this.filtersAll, fetchHits: this.fetchHits,fetchOffset:this.fetchOffset};
    

    this.queryArray.push(query);
    if(filter.name=="Source"){
    console.log("It is source If",filter.value);
    this.printHistory.push({"query":query,"timestamp":new Date(utc),"operator":this.json_request['searchOperator'],"Numberofhits":45,"Database":filter.value});}
    else{
    console.log("It is source Else");
    this.printHistory.push({"query":query,"timestamp":new Date(utc),"operator":this.json_request['searchOperator'],"Numberofhits":45,"Database":"All"});
    }
    console.log("The printHistory object is",this.printHistory);

    }
    const headers = {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      'Access-Control-Allow-Headers': 'Content-Type',
    };
    const headerObj = {headers: new Headers(headers)};
    this.totReqsMade = this.totReqsMade + 1;
    console.log(JSON.stringify(this.json_request));
    if(this.debug==true && this.usptoRestUrl.indexOf('debug') == -1){
      this.usptoRestUrl+='?debug=true';
    }
    return this._http.post(this.usptoRestUrl, JSON.stringify(this.json_request), headerObj)
      .map(response => {

        { console.log("The response json is",response.json());
          return response.json(); }
      })
      .catch(error => Observable.throw(error.json()));
  }
}
