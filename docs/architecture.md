# AppCivist Core Platform Architecture

AppCivist Core's platform is implemented as a REST API built with [playframework](https://www.playframework.com), [play-authenticate](https://github.com/joscha/play-authenticate) (for user authentication) and [deadbolt](http://deadbolt.ws/) (for user authorization). This document describes how the source code is organized for developers to contribute and extend or improve our codebase. 

## Summary

For each RESTful endpoint (e.g., GET /assembly), a line in the **Routes file** (conf/routes) indicates which **Controller** (controllers/\*) will be used to answer a request that matches that route, including the specific method in that controller (e.g., *controlers.Contributions.findAssemblyContributions*).  Controllers then operate on **Model** objects that represent entities in the database and contain the logic to access the data. 

The outcome of each operation is then rendered in JSON to provide the resulting **View** (which can also be, in some cases, generated through play's templating framework - views/\*). Playframework provides response helpers as part of the **Results** class (*play.mvc.Results*). These helpers can be used to generate the response to requests with the appropriate HTTP data on them (Results.ok(..) for HTTP 200, Results.notFound(..) for HTTP 404, etc.). These helpers receive the data to return in the body as a parameter. Playframework also provides helper Json functions (*play.libs.Json*) to convert an object to json (e.g., *Json.toJson(object)*) and viceversa (e.g., *Json.fromJson(object)*).  

In some cases, as a way of reducing the complexity and amount of code in controllers, an extra layer of abstraction was introduced through **Delegate** classes, which simply implement part of the logic that the controller needs. Similarly, to reduce the complexity of the object we will render as a view in JSON, **Transference Models** are objects that map the model but contain no data access logic, they simply serve as the means of specifying the desired outcome of an operation. When they are used, [dozer](dozer.sourceforge.net/documentation/gettingstarted.html) is used to map the model to transference models and viceversa. 

In addition to this general organization, authentication and authorization are provided through [play-authenticate](https://github.com/joscha/play-authenticate) and [deadbolt](http://deadbolt.ws/). The **Users** controller implements most of the play-authenticate methods for user/password authentication (the only supported right now). Authenticated users receive an **Auth Token** that must be included as the header **PLAY_SESSION** on every request. Annotations like **@SubjectPresent** (which only checks that there is an authenticated user associated to the request) and **@Dynamic** (which redirects to a specific class and method in the **security** package) are used to enforce authorization logics (based on user roles for example). To use these annotations, they are placed on top of controller methods. 

**@Api\*** annotations are used to generate API documentation through swagger. 

## Data model and persistence

## API resources and controllers

## Authentication

## Authorization

## Utilities

### Etherpad connection

### MapBox API

### Amazon S3 connection


[API Docs]()