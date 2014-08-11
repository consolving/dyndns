package controllers;

import java.util.List;

import models.Account;
import models.DnsEntry;
import fileauth.actions.BasicAuth;
import play.data.*;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.Admin.*;

@BasicAuth
public class Admin extends Controller {
	public static Result index() {
		List<DnsEntry> entries = DnsEntry.find.all();
		List<Account> accounts = Account.find.all();
		return ok(index.render(entries, accounts));
	}

	public static Result update() {
		Form<DnsEntry> entryForm = Form.form(DnsEntry.class).bindFromRequest();
				// new Form(DnsEntry.class).bindFromRequest();
		DnsEntry entry = entryForm.get();
		entry.save();
		
		return redirect(routes.Admin.index());
	}

	public static Result resetApiKey(Long entryId) {
		DnsEntry entry = DnsEntry.find.byId(entryId);
		entry.apiKey = DnsEntry.generateApiKey();
		entry.save();
		
		return redirect(routes.Admin.index());
	}
}
