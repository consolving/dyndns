package controllers;

import java.util.List;

import fileauth.actions.BasicAuth;
import models.Domain;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.Domains.index;

@BasicAuth
public class Domains extends Controller {
	public static Result index() {
		List<Domain> domains = Domain.Find.all();
		return ok(index.render(domains));
	}
}
