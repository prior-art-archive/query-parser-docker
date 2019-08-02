import { Component, OnInit } from '@angular/core';
import { SharedService } from './shared.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-print-history',
  templateUrl: './print-history.component.html',
  styleUrls: ['./print-history.component.css']
})
export class PrintHistoryComponent implements OnInit {
printHistoryJSONObject=[];

  constructor(private _sharedService: SharedService,private router: Router) { }

  ngOnInit() {
    //window.onbeforeunload = function() { return "You will  leave this page"; };   
   console.log("Entered ngOnInit");
    if(this._sharedService.printHistory!=[] && this._sharedService.queryArray!=[])
   {
     console.log("Entered if block");
  this.printHistoryJSONObject=this._sharedService.printHistory;
   console.log("The query array is",this._sharedService.queryArray);
  //console.log("The query of printHistoryJSONObject",this.printHistoryJSONObject[0]['query']);
   console.log("The type of printHistoryJSONObject is",typeof(this._sharedService.printHistory));
   console.log("The value of queryArray is",this.printHistoryJSONObject);
   }
   else{
     console.log("Entered else block");

   }
   
}

returnHome()
{
  this.router.navigate(['/home']);
}



}
