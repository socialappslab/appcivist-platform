# AppCivist Core Platform

Core of AppCivist services. 

## Getting Started

1. Download and install [Typesafe Activator][3]
2. Clone this repository
3. On a console, access your local copy of appcivist-core source code. 

	```sh 
		cd <PATH_TO_YOUR_LOCAL_REPO>/appcivist-core
	```

4. Run *activator* to enter the typsafe activator console. I will automatically download all the dependencies and setup the [play framework][1]. 
5. Follow Play Framework's [Get Started][2] guide to learn more about how the project is organized

## Run on server

1. Rename conf/application.conf.sample to conf/application.conf.  
1. On a console, access your local copy of appcivist-core source code. 

	```sh 
		cd <PATH_TO_YOUR_LOCAL_REPO>/appcivist-core
	```

2. Run *activator run* 

## Package 

This file will be packaged with your application, when using `activator dist`.


[1]: https://www.playframework.com/
[2]: https://www.playframework.com/documentation/2.3.x/Home
[3]: http://typesafe.com/get-started