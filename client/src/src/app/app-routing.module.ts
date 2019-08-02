import {AppComponent} from './app.component';
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {HomeComponent} from './home.component';
import { ResultDetailResolve } from './results-detail.resolve.service';
import { SearchPageComponent } from './search-page.component';
import {PrintHistoryComponent} from './print-history.component';

// routes for different components
const routes: Routes = [
    { path: '', redirectTo: 'home', pathMatch: 'full' },
    { path: 'home', component: HomeComponent },
    { path: 'search/:query', component: SearchPageComponent, resolve: { results: ResultDetailResolve }},
    {path: 'print',component:PrintHistoryComponent},
    { path: 'search', component: SearchPageComponent },
    { path:'search/:query/debug',component: SearchPageComponent,resolve:{ results: ResultDetailResolve }},
    { path: '**', redirectTo: 'home' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule],
  providers: [
    ResultDetailResolve
  ]
})
export class AppRoutingModule {}
