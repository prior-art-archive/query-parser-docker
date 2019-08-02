import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import {NgxPaginationModule} from 'ngx-pagination';
import { AppComponent } from './app.component';
import { HttpModule } from '@angular/http';
import { HomeComponent } from './home.component';
import { HeaderComponent } from './header.component';
import { ResultComponent } from './result.component';
import { FacetComponent } from './facet.component';
import { SharedService } from './shared.service';
import { FormsModule } from '@angular/forms';
import { AppRoutingModule } from './app-routing.module';
import { SearchPageComponent } from './search-page.component';
import { BusyModule } from 'angular2-busy';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { PrintHistoryComponent } from './print-history.component';



@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    HeaderComponent,
    ResultComponent,
    FacetComponent,
    SearchPageComponent,
    PrintHistoryComponent
  ],
  imports: [
    BrowserModule,
    HttpModule,
    FormsModule,
    AppRoutingModule,
    BusyModule,
    BrowserAnimationsModule,
    NgxPaginationModule
],
  providers: [SharedService],
  bootstrap: [AppComponent]
})
export class AppModule {
}

