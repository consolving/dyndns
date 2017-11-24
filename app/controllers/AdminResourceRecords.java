package controllers;

import fileauth.actions.BasicAuth;
import helpers.DnsInquireHelper;
import helpers.DnsUpdateHelper;
import models.Account;
import models.Domain;
import models.ResourceRecord;
import play.Logger;
import play.data.Form;
import play.mvc.Result;
import views.html.AdminDomains.show;
import views.html.AdminResourceRecords.edit;

@BasicAuth
public class AdminResourceRecords extends Application {
	
	public static Result save(String name) {
		if(name == null || name.isEmpty()) {
			return notFound();
		}		
		Account account = Account.geAccountOrCreate(request().username());
		if(!account.isAdmin()) {
			return forbidden();
		}	
		Domain domain = Domain.Find.where().eq("name", name).findUnique();
		if(domain == null) {
			return notFound();
		}		
		Form<ResourceRecord> resourceForm = ResourceRecord.RECORD_FORM.bindFromRequest();	
		Logger.debug("form-data: "+resourceForm.data());
		if (resourceForm.hasErrors()) {
			return ok(show.render(domain, resourceForm));
		} 
		ResourceRecord rr = resourceForm.get();
		rr.domain = domain;
		if(rr.ttl == null) {
			rr.ttl = 3600;
		}
		Logger.debug("got: "+rr.toString());
		if(rr.id != null) {
			rr.update();
		} else {
			rr.save();			
		}
		
		domain.forceUpdate = true;
		domain.save();
		
		new DnsUpdateHelper(domain).update();
		new DnsInquireHelper(domain).inquire();
		
		return redirect(routes.AdminDomains.show(domain.name));	
	}
	
	public static Result edit(String name, Long id) {
		if(name == null || name.isEmpty()) {
			return notFound();
		}		
		Account account = Account.geAccountOrCreate(request().username());
		if(!account.isAdmin()) {
			return forbidden();
		}	
		Domain domain = Domain.Find.where().eq("name", name).findUnique();
		if(domain == null) {
			return notFound();
		}	
		ResourceRecord rr = ResourceRecord.Find.byId(id);
		return ok(edit.render(domain, rr, ResourceRecord.RECORD_FORM));		
	}
	
	public static Result delete(String name, Long id) {
		if(name == null || name.isEmpty()) {
			return notFound();
		}		
		Account account = Account.geAccountOrCreate(request().username());
		if(!account.isAdmin()) {
			return forbidden();
		}	
		Domain domain = Domain.Find.where().eq("name", name).findUnique();
		if(domain == null) {
			return notFound();
		}	
		ResourceRecord rr = ResourceRecord.Find.byId(id);
		if(rr != null) {
			rr.delete();
			Logger.info("rr "+rr.toString()+" #"+id+" was deleted!");	
			domain.forceUpdate = true;
			domain.save();			
		}
		return redirect(routes.AdminDomains.show(domain.name));			
	}
}
