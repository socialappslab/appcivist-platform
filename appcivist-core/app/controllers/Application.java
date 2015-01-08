package controllers;

import play.*;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {

	public static Result index() {
		return ok(index
				.render("Welcome to AppCivist!",
						"AppCivist is a service oriented platform to support civic participation and social activism"));
	}

}
