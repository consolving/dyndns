package controllers;

import java.util.List;

import fileauth.actions.BasicAuth;
import models.Account;
import models.DnsEntry;
import play.mvc.*;
import views.html.Application.*;

@BasicAuth
public class Application extends Controller {
	public final static Account USER = Account.geAccountOrCreate(request()
			.username());
	public static Result index() {
		Account account = Account.geAccountOrCreate(request().username());
		List<DnsEntry> entries = account.dnsEntries;
		if (account.isAdmin()) {
			entries = DnsEntry.find.all();
		}
		return ok(index.render(entries));
	}
}
