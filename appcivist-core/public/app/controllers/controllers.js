/**
 * AppCivist Demo Client - Basic Controllers
 * 
 */


// This controller gets data about the assemblies and associates it with the $scope
// The $scope is ultimately bound to the main view
appCivistApp.controller('MainController', function ($scope, appCivistService, loginService) {

    // I like to have an init() for controllers that need to perform some initialization. Keeps things in
    //one place...not required though especially in the simple example below
    init();

    function init() {
        $scope.assemblies = appCivistService.getAssemblies();
        $scope.auth = loginService.getAuth();
    }

    $scope.insertAssembly = function () {
        var title = $scope.newAssembly.title;
        var description = $scope.newAssembly.description;
        var city = $scope.newAssembly.city;
        appCivistService.insertAssembly(title, description, city);
        $scope.newAssembly.title = '';
        $scope.newAssembly.description = '';
        $scope.newAssembly.city = '';
    };

    $scope.deleteAssembly = function (id) {
        appCivistService.deleteAssembly(id);
    };
});

//This controller retrieves data from the appCivistService and associates it with the $scope
//The $scope is bound to the order view
//app.controller('AssemblyController', function ($scope, $routeParams, appCivistService) {
//    $scope.assembly = {};
//    $scope.ordersTotal = 0.00;
//
//    //I like to have an init() for controllers that need to perform some initialization. Keeps things in
//    //one place...not required though especially in the simple example below
//    init();
//
//    function init() {
//        //Grab assemblyID off of the route        
//        var assemblyID = ($routeParams.assemblyID) ? parseInt($routeParams.assemblyID) : 0;
//        if (assemblyID > 0) {
//            $scope.assembly = appCivistService.getCustomer(assemblyID);
//        }
//    }
//
//});
//
////This controller retrieves data from the appCivistService and associates it with the $scope
////The $scope is bound to the orders view
//app.controller('OrdersController', function ($scope, appCivistService) {
//    $scope.assemblies = [];
//
//    //I like to have an init() for controllers that need to perform some initialization. Keeps things in
//    //one place...not required though especially in the simple example below
//    init();
//
//    function init() {
//        $scope.assemblies = appCivistService.getAssemblies();
//    }
//});
//
//app.controller('NavbarController', function ($scope, $location) {
//    $scope.getClass = function (path) {
//        if ($location.path().substr(0, path.length) == path) {
//            return true
//        } else {
//            return false;
//        }
//    }
//});
//
////This controller is a child controller that will inherit functionality from a parent
////It's used to track the orderby parameter and ordersTotal for a assembly. Put it here rather than duplicating 
////setOrder and orderby across multiple controllers.
//app.controller('OrderChildController', function ($scope) {
//    $scope.orderby = 'product';
//    $scope.reverse = false;
//    $scope.ordersTotal = 0.00;
//
//    init();
//
//    function init() {
//        //Calculate grand total
//        //Handled at this level so we don't duplicate it across parent controllers
//        if ($scope.assembly && $scope.assembly.orders) {
//            var total = 0.00;
//            for (var i = 0; i < $scope.assembly.orders.length; i++) {
//                var order = $scope.assembly.orders[i];
//                total += order.orderTotal;
//            }
//            $scope.ordersTotal = total;
//        }
//    }
//
//    $scope.setOrder = function (orderby) {
//        if (orderby === $scope.orderby)
//        {
//            $scope.reverse = !$scope.reverse;
//        }
//        $scope.orderby = orderby;
//    };
//
//});
