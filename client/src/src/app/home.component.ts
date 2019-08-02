import { Component, OnInit } from '@angular/core';
import { SharedService } from './shared.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./app.component.css']
})
export class HomeComponent implements OnInit {
  id_query = '';
  results = '';
  busy: Promise<any>;
  operatorNames=[{'val':'AND'},{'val':'OR'},{'val':'ADJ'},{'val':'NEAR'},{'val':'WITH'},{'val':'SAME'}];
  selectedOperator=this.operatorNames[0];
  operatorTest='';

constructor(private router: Router, private _sharedService: SharedService) { }

ngOnInit() {
  }

onChangeOp(selectValue){
  this.operatorTest=selectValue.val;
  console.log("The operator is",this.operatorTest);
  this._sharedService.getOperator(this.operatorTest);
  
}
  // method to call when search is triggered from landing page, here the page is routed to the search results component.
  getResults() {
    localStorage.setItem('searchQuery', this.id_query);
    this.busy = this.router.navigateByUrl('/search/' + this.id_query);
  }

//method to navigate to print history page from landing page
  printResults()
  {
    this.router.navigate(['/print']);
    console.log("Routed successfully to print history");
  }

  //method to navigate to clear history page from landing page
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
  // this method is used for capturing the key press enter event for search text box
  keyDownFunction(event) {
    if (event.keyCode == 13) {
      this.getResults();
    }
  }
}
