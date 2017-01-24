package controllers;

import static play.data.Form.form;

import java.util.List;

import models.Account;
import models.DnsEntry;
import models.Domain;
import models.SubDomain;
import play.data.Form;
import play.mvc.Result;
import views.html.DynDns.index;
import views.txt.DynDns.hosts;
import fileauth.actions.BasicAuth;

@BasicAuth
public class DynDns extends Application {
	final static Form<DnsEntry> ENTRY_FORM = form(DnsEntry.class);

	public static Result index() {
		Account account = Account.geAccountOrCreate(request().username());
		List<DnsEntry> entries = DnsEntry.Find.where().eq("account", account).eq("toDelete", false).order("name ASC").findList();
		return ok(index.render(entries, ENTRY_FORM));
	}

	public static Result hosts() {
		Account account = Account.geAccountOrCreate(request().username());
		List<DnsEntry> entries = DnsEntry.Find.where().eq("account", account).eq("toDelete", false).order("name ASC").findList();
		return ok(hosts.render(entries));		
	}
	
	public static Result delete(Long id){
		DnsEntry entry = DnsEntry.Find.byId(id);
		if(entry != null){
			entry.markToDelete();
			entry.save();
		}
		return redirect(routes.DynDns.index());		
	}
	
	public static Result save() {
		Form<DnsEntry> entryForm = ENTRY_FORM.bindFromRequest();
		Account account = Account.geAccountOrCreate(request().username());
		if (entryForm.hasErrors()) {
			List<DnsEntry> entries = account.dnsEntries;
			return ok(index.render(entries, entryForm));
		} else {
			DnsEntry entry = entryForm.get();
			if (entry != null && !DnsEntry.exists(entry) && entry.checkName()) {
				SubDomain sd = SubDomain.find.byId(entry.subDomain.id);
				entry.domain = Domain.Find.byId(sd.domain.id);				
				entry.save();
			}
			return redirect(routes.DynDns.index());
		}
	}
}
