import {Filter} from './Filter';
import {Component, OnInit} from '@angular/core';
import {Http} from '@angular/http';
import {Headers, RequestOptions} from '@angular/http';
import 'rxjs/add/operator/toPromise';
import {SharedService} from './shared.service';
import {ActivatedRoute, Params} from '@angular/router';
import {Location} from '@angular/common';
import {FacetComponent} from './facet.component';
import {Subscription} from 'rxjs';
import { Router } from '@angular/router';


@Component({
  selector: 'app-result',
  templateUrl: './result.component.html',
  styleUrls: ['./app.component.css']
})
export class ResultComponent implements OnInit {
  newOffset=0; // Initailising the offset parameter which needs to be sent as a part of the request
  newPage=1; // Current page that the user is on is by default page1
  handleError: any;
  operatorTest='';
  id_query = '';
  results = '';
  facet = {};
  is_Filtered = false;
  loadingText = '';
  filterCount=10; //By default 10 results are shown on the page
  busy: Subscription;
  resCount = [{'val': '5'}, {'val': '10'}, {'val': '15'}]; // Options for the user to choose from the dropdown
  selectedresCount = this.resCount[1]; //By default 10 is selected
  operatorNames=[{'val':'AND'},{'val':'OR'},{'val':'ADJ'},{'val':'NEAR'},{'val':'WITH'},{'val':'SAME'}];
  selectedOperator=this.operatorNames[0];
  constructor(private _sharedService: SharedService, private route: ActivatedRoute, private location: Location,private router: Router) {
 }
  
  // getting the query from the landing page
  ngOnInit() {
    if (localStorage.getItem('searchQuery') !== undefined) {
      this.id_query = localStorage.getItem('searchQuery');
    }
    this.route.data.subscribe((data: {results: string}) => {
      this.results = data.results;
          });
}

//when user selects a value of no. of results to display on a page from dropdown
onChange(selectValue) {
  this.filterCount=selectValue.val;
  console.log("The difference is",this.results['totalHits']-this.newOffset);
  if(this.results['totalHits']-this.newOffset<=this.filterCount)
  {
  this._sharedService.getFilterCount(this.results['totalHits']-this.newOffset);//Not sure if it has to be -3. Ideally should work without -3
  this.busy = this._sharedService.getResults(this.id_query) 
  .subscribe(r => this.results = r);
  this.loadingText = '';  
  }
  else{
  this._sharedService.getFilterCount(this.filterCount);
  this.busy = this._sharedService.getResults(this.id_query) 
  .subscribe(r => this.results = r);
  this.loadingText = '';
  }
  console.log("The results returned from getFilterCount method are",this.results);
  
}

onChangeOp(selectValue){
this.operatorTest=selectValue.val;
console.log("The operator is",this.operatorTest);
this._sharedService.getOperator(this.operatorTest);
this.busy = this._sharedService.getResults(this.id_query) 
  .subscribe(r => this.results = r);
  this.loadingText = '';  
  
}

  // this method is invoked if user searches on the search results page
  getResults() {
    this.location.go("/search/"+this.id_query); //added to rewrite URL on search
    this.loadingText = 'Loading...';
    //this.is_Filtered = false; //Commented out this coz this was causing the filters to reset
    console.log("The query fired from search results page is",this.id_query);
    this.busy = this._sharedService.getResults(this.id_query)
      .subscribe(r => this.results = r);
    this.loadingText = '';
    console.log("The results are #",this.results); 
   
}

//This method captures the page change event when the user selects the next or previous pages
 pageChanged(event){
  console.log("pageChanged")
  console.log("The current page is",event);
  this.newPage=event;
  this.newOffset =(event-1)*this.filterCount;
  console.log("The difference is",this.results['totalHits']-this.newOffset);
  if(this.results['totalHits']-this.newOffset<=this.filterCount)
  { console.log("Entered if");
    this._sharedService.getFilterCount(this.results['totalHits']-this.newOffset);
    this._sharedService.getFetchOffset(this.newOffset);
    this.busy = this._sharedService.getResults(this.id_query) 
  .subscribe(r => this.results = r);
  this.loadingText = '';
  }
  else{
  console.log("Entered else");
  this._sharedService.getFilterCount(this.filterCount);
  this._sharedService.getFetchOffset(this.newOffset);
  this.busy = this._sharedService.getResults(this.id_query)
  .subscribe(r => this.results = r);
  this.loadingText = '';}
  console.log("The results returned from getFetchOffset method are",this.results);
}

//method that successfully routes to print history page
 printResults()
  {
    this.router.navigate(['/print']);
    console.log("Routed successfully to print history page");
  }
  //method that successfully routes to clear history page
  deleteHistory()
  {
    if (window.confirm('All Searches made for this application will be cleared. Do you want to continue?'))
{
     this._sharedService.printHistory=[];
     this._sharedService.queryArray=[];
}
else
{
    console.log("User clicked no!!!");
}
     console.log("Routed successfully to clear history");
  }
  
 // method that captures the facets change event
  facetChanged(filter: Filter) {
    this.loadingText = 'Loading...';
    console.log("Filter in the emitted method with another",filter);
    if (filter !== null) {
      this.is_Filtered = true;
    } else {
      this.is_Filtered = false;
    }
    this.busy = this._sharedService.getFilteredResults(this.id_query, filter)
      .subscribe(r => this.results = r);
      this.loadingText = '';
  }

clickURL(event){
  console.log("The event is",event);
  var win=window.open(event,'_blank');
  win.focus();
}
  // this method is used for capturing the key press enter event for search text box
   keyDownFunction(event) {
    if (event.keyCode == 13) {
      this.getResults();
    }
  }
}
