package controllers;

import java.util.List;

import models.Account;
import models.DnsEntry;
import models.Domain;
import play.data.Form;
import play.mvc.Result;
import views.html.AdminDynDNS.index;
import fileauth.actions.BasicAuth;
import jobs.DnsUpdateJob;
import jobs.MaintenanceJob;

@BasicAuth
public class AdminDynDNS extends Application {
	
	public static Result index() {
		Account account = Account.geAccountOrCreate(request().username());
		if(!account.isAdmin()) {
			return forbidden();
		}
		List<DnsEntry> entries = DnsEntry.Find.order("name ASC").findList();
		List<Account> accounts = Account.Find.order("username ASC").findList();
		return ok(index.render(entries, accounts));
	}

	public static Result update(Long id) {
		Account account = Account.geAccountOrCreate(request().username());
		if(!account.isAdmin()) {
			return forbidden();
		}
		Form<DnsEntry> entryForm = Form.form(DnsEntry.class).bindFromRequest();
		DnsEntry entry = entryForm.get();
		DnsEntry oldEntry = DnsEntry.Find.byId(id);
		entry.updated = null;
		// FIXME work around for resetting APIKey Bug....
		entry.apiKey = oldEntry.apiKey;
		entry.update(id);
		return redirect(routes.AdminDynDNS.index());
	}

	public static Result startJob(String jobName) {
		Account account = Account.geAccountOrCreate(request().username());
		if(!account.isAdmin()) {
			return forbidden();
		}
		if("DnsUpdate".equals(jobName)) {
			DnsUpdateJob job = new DnsUpdateJob();
			job.run();
		}
		if("Maintenance".equals(jobName)) {
			MaintenanceJob job = new MaintenanceJob();
			job.run();			
		}
		return redirect(routes.AdminDynDNS.index());		
	}
	
	public static Result delete(Long id) {
		Account account = Account.geAccountOrCreate(request().username());
		if(!account.isAdmin()) {
			return forbidden();
		}
		DnsEntry entry = DnsEntry.Find.byId(id);
		if (entry != null) {
			Domain domain = Domain.Find.byId(entry.domain.id);
			entry.delete();
			domain.forceUpdate = true;
			domain.save();
		}
		return redirect(routes.AdminDynDNS.index());
	}

	public static Result resetApiKey(Long id) {
		Account account = Account.geAccountOrCreate(request().username());
		if(!account.isAdmin()) {
			return forbidden();
		}
		DnsEntry entry = DnsEntry.Find.byId(id);
		if (entry != null) {
			entry.apiKey = DnsEntry.generateApiKey();
			entry.update();
		}
		return redirect(routes.AdminDynDNS.index());
	}
}
