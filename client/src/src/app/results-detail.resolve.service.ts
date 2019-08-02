import { Injectable } from '@angular/core';
import {
    Router, Resolve,
    ActivatedRouteSnapshot
} from '@angular/router';
import { SharedService } from './shared.service';
import { Observable } from 'rxjs';
import {Location} from '@angular/common';

//This is a resolver for navigating from landing page to the results page with query passed as parameter
@Injectable()
export class ResultDetailResolve implements Resolve<string> {
  constructor(
    private sharedService: SharedService, private location: Location
  ) {}

  resolve(route: ActivatedRouteSnapshot): Observable<string> {
    console.log("The query in resolve method",route.params);
    var value=this.location.path();
    var parts = value.split('/');
    var queryNew = parts[2];
if(parts[3]&&parts[3]=='debug'){
console.log("The new param is",parts[3]);
console.log("Debug set to true");
this.sharedService.debug=true;
}
    return this.sharedService.getResults(route.params.query);
  }
}
