(function() {
	console.log("Welcome to AppCivist!"
	
	var app, dependencies;
	dependencies = [ 'ngRoute', 'ui.bootstrap'];
	app = angular.module('appCivist', dependencies);
	angular.module('appCivist.routeConfig', [ 'ngRoute' ]).config();
);
	});
}).call(this);
