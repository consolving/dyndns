package controllers;

import fileauth.actions.BasicAuth;
import play.mvc.Controller;
import play.mvc.Result;

@BasicAuth
public class Admin extends Controller {
	public static Result index() {
		return ok(":-)");
	}
}
