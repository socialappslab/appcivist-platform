appCivistApp.service('loginService', function () {
    
	 $scope.auth = {};
	
	this.getLogintState = function () {
        return auth;
    };

    this.signIn = function (username, password) {
    	console.log(auth);
    	
    	// ToDo: call the actual auth service to authenticate the user
    	$scope.auth.user = username;
    	$scope.auth.authenticated = true;
    };

    this.signOut = function (username) {
    	$scope.auth.user = '';
    	$scope.auth.authenticated = false;
    };

    this.getAuth = function () {
    	console.log(auth);
        return $scope.auth;
    };
    

    this.userIsAuthenticated = function () {
    	console.log(auth);
        return $scope.auth.authenticated;
    };
});