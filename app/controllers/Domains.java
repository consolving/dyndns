package controllers;

import java.util.List;

import fileauth.actions.BasicAuth;
import models.Account;
import models.Domain;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.Domains.index;
import views.html.Domains.show;

@BasicAuth
public class Domains extends Controller {
	
	public static Result index() {
		Account account = Account.geAccountOrCreate(request().username());
		if(!account.isAdmin()) {
			return forbidden();
		}
		List<Domain> domains = Domain.Find.all();
		return ok(index.render(domains));
	}
	
	public static Result show(Long id) {
		Account account = Account.geAccountOrCreate(request().username());
		if(!account.isAdmin()) {
			return forbidden();
		}
		Domain domain = Domain.Find.byId(id);
		return ok(show.render(domain));
	}
}
