/// AppCivist Simple Demo Client
/**
 * AppCivist Platform Demo Client developed with AngularJS
 * Folders: 
 * 	/app
 * 		/controllers
 * 		/directives
 * 		/services
 * 		/partials
 * 		/views
 */

console.log("Welcome to AppCivist!");
	

var dependencies = [ 'ngRoute', 'ui.bootstrap', 'ngResource'];
var appCivistApp = angular.module('appCivistApp', dependencies);

// This configures the routes and associates each route with a view and a controller
appCivistApp.config(function ($routeProvider) {
    $routeProvider
        .when('/',
            {
                controller: 'MainController',
                templateUrl: '/assets/app/partials/main.html'
            })
//        .when('/assembly/:assemblyID',
//            {
//                controller: 'AssemblyController',
//                templateUrl: '/app/partials/assemblyView.html'
//            })
//        //Define a route that has a route parameter in it (:customerID)
//        .when('/assembly/:assemblyID/campaign/:campaignId',
//            {
//                controller: 'AssemblyController',
//                templateUrl: '/app/partials/assemblyCampaignView.html'
//            })
        .otherwise({ redirectTo: '/' });
});

