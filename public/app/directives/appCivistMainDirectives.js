/**
 * AppCivist Main Directives
 * 
 * # footer and header based on http://gon.to/2013/03/23/the-right-way-of-coding-angularjs-how-to-organize-a-regular-webapp/
 */

appCivistApp.directive('footer', function () {
    return {
        restrict: 'A',  // This means that it will be used as an attribute and NOT as an element. 
        				// I don't like creating custom HTML elements
        replace: true,
        templateUrl: "/public/app/partials/footer.html",
        controller: ['$scope', '$filter', function ($scope, $filter) {
            // Your behaviour goes here :)
        }]
    }
});

appCivistApp.directive('header', function () {
    return {
        restrict: 'A', 
        replace: false,
        scope: {user: '='}, // This is one of the cool things :). Will be explained in post.
        templateUrl: "/public/app/partials/header.html",
        controller: ['$scope', '$filter', function ($scope, $filter) {
            console.log("User = "+$scope.user);
            console.log("User = "+$scope.sessionKey);            
        }]
    }
});