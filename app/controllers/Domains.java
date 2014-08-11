package controllers;

import java.util.List;

import fileauth.actions.BasicAuth;
import models.Account;
import models.DnsEntry;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.Domains.*;

@BasicAuth
public class Domains extends Controller {
	public static Result index() {
		Account account = Account.geAccountOrCreate(request().username());
		List<DnsEntry> entries = account.dnsEntries;
		return ok(index.render(entries));
	}

	public static Result create(String name, Long domainId) {
		Account account = Account.geAccountOrCreate(request().username());
		return redirect(routes.Domains.index());
	}
}
