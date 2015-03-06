/**
 * AppCivist Main Directives
 * 
 * # footer and header based on http://gon.to/2013/03/23/the-right-way-of-coding-angularjs-how-to-organize-a-regular-webapp/
 */

module.directive('footer', function () {
    return {
        restrict: 'A',  // This means that it will be used as an attribute and NOT as an element. 
        				// I don't like creating custom HTML elements
        replace: true,
        templateUrl: "/assets/partials/footer.html",
        controller: ['$scope', '$filter', function ($scope, $filter) {
            // Your behaviour goes here :)
        }]
    }
});

module.directive('header', function () {
    return {
        restrict: 'A', 
        replace: true,
        scope: {user: '='}, // This is one of the cool things :). Will be explained in post.
        templateUrl: "/assets/partials/header.html",
        controller: ['$scope', '$filter', function ($scope, $filter) {
            // Your behaviour goes here :)
        }]
    }
});