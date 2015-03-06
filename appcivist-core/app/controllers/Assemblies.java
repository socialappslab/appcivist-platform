package controllers;


import models.Assembly;
import models.AssemblyCollection;
import play.mvc.*;
import play.libs.Json;

public class Assemblies extends Controller {
	
	public static Result findAssemblies() {
		AssemblyCollection assemblies = Assembly.findAll();
		
		if (request().accepts("application/xml")) { 
	        return ok("<errorMessage>Not Implemented Yet</errorMessage>");
	    } else {
			return ok(Json.toJson(assemblies));
	    }
		
	}

}
