To get started with the Angular4 project, the following dependencies must be installed:

1)Node.js with NPM (Node Package Manager)
2)Angular-CLI (Command Line Interface)

The easiest way to get up and running with a fresh Angular 4 project is to use angular CLI.To start a new ng4 project, at the console, type: ng new my-project-name

app/app.module.ts
Defines AppModule, the root module that tells Angular how to assemble the application.Every Angular app has at least one NgModule class, the root module. You bootstrap that NgModule to launch the application.
By convention, the root module class is called AppModule and it exists in a file named app.module.ts. The Angular CLI generates the initial AppModule for you when you create a project.The root AppModule is all you need in a simple application with a few components.
The initial declarations array identifies the application's components.
The imports section identifies application's modules.
The providers array registers services with the top-level dependency injector.
Lastly, the bootstrap list identifies the AppComponent as the bootstrap component. When Angular launches the app, it renders AppComponent.

app/app.component.{ts,html,css,spec.ts}
The CLI creates the first Angular component. This is the root component and it is named app-root. You can find it in ./src/app/app.component.ts.
It defines the AppComponent along with an HTML template, CSS stylesheet, and a unit test. It is the root component of what will become a tree of nested components as the application evolves.

app-routing.module.ts
The Angular Router is an optional service that presents a particular component view for a given URL. It is not part of the Angular core. It is in its own library package, @angular/router. RouterModule and Routes is imported from it. A routed Angular application has one singleton instance of the Router service. When the browser's URL changes, that router looks for a corresponding Route from which it can determine the component to display.
Various route definitions are created, router is configured via the RouterModule.forRoot method, and the result is added to the AppModule's imports array.

facet.component.ts
In this there are methods to capture filter change event for any filter change which is ngOnChanges method. It is called right after the data-bound properties have been checked and before view and content children are checked if at least one of them has changed. The changes parameter contains the changed properties.
Another method updateSelectedFacet is defined which updates the filters, in case the filter is already applied and user adds another filter then this method is invoked.
This methods emits a filter object which is a model object on custom event.

Filter.ts
A custom model object is defined which is a name-value pair wherein both name and value are array of strings.

home.component.ts
This component has method that gets called when search is triggered from landing page and the page is routed to the search results component. It also has method to navigate to print history page from landing page, method to navigate to clear history page from landing page,method that gets invoked on picking an operator from the dropdown on the landing page and also a method to capture the key press enter event for search text box.

print-history.component.ts
Routing happens to this component when the user selects the print-history option. Session hisotry is displayed such as list of queries fired,correposnding timestamp values, Number of hits, Operator selected(Default is AND operator) and the database from which the results were retrieved.

result.component.ts
On page load(ngOnInit), query from the landing page is obtained and populated in the search box.There are methods that get triggered when user selects no. of results to display on the page from dropdown, if user searches on the search results page either by hitting the search button or the enter key on the keyboard,if the user selects print-history or delete history options and that gets invoked on picking an operator from the dropdown on the results page.facetChanged method is also defined that captures the facet change event where filter object is retrieved as the input to the method. 

results-detail.resolve.service.ts
ResultDetailResolve is defined which is a resolver for navigating from landing page to the results page with query passed as parameter. URL is parsed to check if debug parameter is present or not in the same method.

shared.service.ts
This has service which gets triggered when the user selects one of the filtercount values from the dropdown, service that gets triggered after the offset is set. i.e., if the user navigates from page 1 to 2 and the number of results per page is 15. Then the offset is computed as (2-1)*15, service to get the filtered results from Web Service with request parameters containing filter object and the query,Service to get the results from the web service and service to set the operator value before triggering search.