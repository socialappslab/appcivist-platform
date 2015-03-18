appCivistApp.service('loginService', function($resource, $http, $location, $localStorageService) {

	var User = $resource('/api/user/:id', {id: '@id'});
	user = {};
	user.authenticated = false;

	this.getUser = function() {
		return user;
	}
	
	this.getLogintState = function() {
		return user.authenticated;
	};

	this.signIn = function(email, password) {
		console.log(user);
		user = {};
		user.email = email;
		user.password = password;
		//$http.post('/user/login', {email:user.email,password:user.password})
		$http.post('/api/user/login', user)
			.success(function(user) {
				if (user !== '0') {
					$localStorageService.set("user",user);
					$localStorageService.set("session_key",user.sessionKey);
					User.get({id:user.id})
					user = $resource
					$location.url('/assemblies');
					// Not Authenticated
				} else {
					$rootScope.message = 'You need to log in.';
					// $timeout(function(){deferred.reject();}, 0);
					//deferred.reject();
					$location.url('/');
				}
			});

	};

	this.signOut = function(username) {
		user.username = '';
		user.authenticated = false;
		$http.post('/api/user/logout').success();
		$location.url('/');
		
	};

	this.userIsAuthenticated = function() {
		console.log(user);
		return auth.authenticated;
	};
});