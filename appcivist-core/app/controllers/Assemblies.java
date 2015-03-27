package controllers;


import models.Assembly;
import models.AssemblyCollection;
import play.mvc.*;
import play.libs.Json;

public class Assemblies extends Controller {
	
	@Security.Authenticated(Secured.class)
	public static Result findAssemblies() {
		AssemblyCollection assemblies = Assembly.findAll();
		return ok(Json.toJson(assemblies));
	}

}
