package controllers;

import java.util.List;

import fileauth.actions.BasicAuth;
import models.Account;
import models.DnsEntry;
import play.mvc.*;
import views.html.Application.*;

@BasicAuth
public class Application extends Controller {
	public static Result index() {
		Account account = Account.geAccountOrCreate(request().username());
		List<DnsEntry> entries = account.dnsEntries;
		if (account.isAdmin()) {
			entries = DnsEntry.find.all();
		}
		return ok(index.render(entries));
	}

	public static Account getAccount() {
		Account account = Account.geAccountOrCreate(request().username());
		if(account == null){
			account = Account.NO_ACCOUNT;
		}
		return account;
	}
}
