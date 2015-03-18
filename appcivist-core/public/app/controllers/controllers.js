// AppCivist Demo Client - Basic Controllers

/**
 * AccountCtrl - functions to control authentication 
 */
appCivistApp.controller('AccountCtrl', function($scope, $resource, $location,
		$localStorageService, appCivistService, loginService) {
	init();

	function init() {
		// check if there is already a user and a sessionKey in the
		// $localStorage
		$scope.user = $localStorageService.get("user");
		$scope.sessionKey = $localStorageService.get("session_key");

		if ($scope.user != null && $scope.sessionKey != null) {
			// TODO Validate that the Session Key corresponds to the user
			$location.url('/assemblies');
		} else {
			$scope.user = {};
			$scope.sessionKey = null;
		}
	}

	$scope.login = function() {
		console.log("Signing in with email = " + $scope.email);
		loginService.signIn($scope.email, $scope.password);
	}

	$scope.signup = function() {
		$location.url('/signupform');
	}

	$scope.signout = function() {
		loginService.signOut();
	}
});

/**
 * MainCtrl - this controller checks if the user is loggedIn and loads the main
 * view with the public cover or redirects it to the list of assemblies that the
 * user can view
 * 
 */
appCivistApp.controller('MainCtrl', function($scope, $resource, $location,
		$localStorageService, appCivistService, loginService) {

	init();

	function init() {
		// check if there is already a user and a sessionKey in the scope
		if ($scope.user != null && $scope.sessionKey != null) {
			// TODO Validate that the Session Key corresponds to the user
			$location.url('/assemblies');
		} else {
			// check if there is a user and session key in the local storage
			$scope.user = $localStorageService.get("user");
			$scope.sessionKey = $localStorageService.get("session_key");
			if ($scope.user != null && $scope.sessionKey != null) {
				// TODO Validate that the Session Key corresponds to the user
				$location.url('/assemblies');
			} else {
				$scope.user = {};
				$scope.sessionKey = null;
			}
		}
	}

	$scope.login = function(email, password) {
		$scope.user = loginService.signIn(email, password);
	}

});

// This controller retrieves data from the appCivistService and associates it
// with the $scope
// The $scope is bound to the order view
appCivistApp.controller('AssemblyListCtrl', function($scope, $routeParams,
		$resource, appCivistService, loginService) {

	$scope.assemblies = {};
	// I like to have an init() for controllers that need to perform some
	// initialization. Keeps things in
	// one place...not required though especially in the simple example below
	init();

	function init() {
		$scope.assemblies = appCivistService.getAssemblies();
		// $scope.auth = loginService.getAuth();
	}
});

// This controller retrieves data from the appCivistService and associates it
// with the $scope
// The $scope is bound to the order view
appCivistApp
		.controller(
				'AssemblyCtrl',
				function($scope, $routeParams, $resource, appCivistService,
						loginService) {
					$scope.currentAssembly = {};

					// I like to have an init() for controllers that need to
					// perform some initialization. Keeps things in
					// one place...not required though especially in the simple
					// example below
					init();

					function init() {
						// $scope.assemblies = appCivistService.getAssemblies();

						// Grab assemblyID off of the route
						var assemblyID = ($routeParams.assemblyID) ? parseInt($routeParams.assemblyID)
								: 0;
						if (assemblyID > 0) {
							$scope.assembly = appCivistService
									.getCustomer(assemblyID);
						}
					}
				});

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
