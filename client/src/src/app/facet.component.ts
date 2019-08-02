import { Component, OnInit } from '@angular/core';
import { Input, EventEmitter, Output, OnChanges, SimpleChanges, SimpleChange } from '@angular/core';
import { Filter } from './Filter';
import {SharedService} from './shared.service';
import {Subscription} from 'rxjs';
import { ResultComponent } from './result.component';
@Component({
  selector: 'app-facet',
  templateUrl: './facet.component.html',
  styleUrls: ['./app.component.css']
})
// filter or facet component
  export class FacetComponent implements OnChanges, OnInit {
  private _facetData: Array<any>;
  filter: Filter;
  filterArray=[];
  results = '';
  eventNew='date'
  facetArray=[];
  selectedFilter=[];
  busy: Subscription;
  query;
  loadingText='';
  @Input() facetData: Array<any>;
  @Input() isFiltered: boolean;
  @Output() facetChangeEvent = new EventEmitter<any>();
  
  constructor(private _sharedService: SharedService, private comp: ResultComponent) { }

  // method to capture filter change event for filter change
  ngOnChanges(changes: SimpleChanges): void {
    if (changes.facetData !== undefined) {
      
      console.log("The facetData is",this.facetData);
      const facetD: SimpleChange = changes.facetData;
      console.log("The simple changes are",changes.facetData);
      console.log('is_Filtered: ', this.isFiltered);
      if (this.isFiltered) {
      for(var i=0;i<this.facetData.length;i++){
        for(var j=0;j<this._sharedService.selectedFilters.length;j++){
        if(this.facetData[i]['facetName']==this._sharedService.selectedFilters[j]['facetName']){
          for(var k=0;k<this.facetData[i]['data'].length;k++){
            for(var l=0;l<this.facetData[i]['data'][k]['key'].length;l++)
            if(this._sharedService.selectedFilters[j]['data'].indexOf(this.facetData[i]['data'][k]['key'][l])!==-1){
            console.log("Selected filters data is",this.facetData[i]['data'][k]['key'],"is selected");
            this.facetData[i]['data'][k]['selected']='checked';
            console.log("The selected field in the facet is",this.facetData);  
            }
            else{
              console.log("False!!!");
            }
          }
        }
      }
    }
    console.log('The current value is: ', this.facetData);
    }  
    }
  }

  ngOnInit() { }

handleClick(event)
{
  if(event=='Publication Date')
  { this.eventNew='date'
    console.log("Check if filtered is true or not",this.comp.is_Filtered); //Comment just to check if we select some filter and then click on sort by whether the facets are reset or not
    this._sharedService.getSortOrder(this.eventNew);
    this.query=localStorage.getItem('searchQuery');
    this.comp.getResults();
    this.busy=this._sharedService.getResults(this.query).subscribe(r => this.results = r);
    this.loadingText = '';
  }
  else if(event=='Relevancy')
  {
    this.eventNew='relevancy';
    this._sharedService.getSortOrder(this.eventNew);
    this.query=localStorage.getItem('searchQuery');
    this.comp.getResults();
    this.busy =  this._sharedService.getResults(this.query).subscribe(r => this.results = r);


  }
}
  // method that will update the filters, in case the filter is already applied and user adds another filter then this method is invoked
  updateSelectedFacet(facetName, event) {
    const index: number = this.filterArray.indexOf(event.target.value);
    if (event.target.checked) {
     if(facetName=='Date Range'){
       this.filterArray[0]=event.target.value;
       this.facetArray[0]=facetName;
     }
     else{
      if (index === -1) {

        this.filterArray.push(event.target.value);
        this.facetArray.push(facetName);
      
     }}
      this.filter = new Filter(this.facetArray, this.filterArray);  //Change made to pass facetName
      //this.isChecked=true;  
} 
  else {
    if(facetName=='Date Range'){
       this.filterArray[0]=event.target.value;
       this.facetArray[0]=facetName;
     }
     else{
      if (index !== -1) {
        this.filterArray.splice(index, 1);
        this.facetArray.splice(index,1);
     }}
      if (this.filterArray === null || this.filterArray.length === 0) {
        this.filter = null;
      }
    }
    
    this.facetChangeEvent.emit(this.filter);
  }
}
