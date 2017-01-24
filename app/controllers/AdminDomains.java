package controllers;

import static play.data.Form.form;

import java.util.List;

import fileauth.actions.BasicAuth;
import jobs.DnsInquireJob;
import models.Account;
import models.DnsEntry;
import models.Domain;
import models.SubDomain;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.AdminDomains.index;
import views.html.AdminDomains.show;

@BasicAuth
public class AdminDomains extends Controller {
	final static Form<Domain> DOMAIN_FORM = form(Domain.class);
	
	public static Result index() {
		Account account = Account.geAccountOrCreate(request().username());
		if(!account.isAdmin()) {
			return forbidden();
		}
		List<Domain> domains = Domain.Find.order("name ASC").findList();
		return ok(index.render(domains, DOMAIN_FORM));
	}
	
	public static Result show(Long id) {
		Account account = Account.geAccountOrCreate(request().username());
		if(!account.isAdmin()) {
			return forbidden();
		}
		Domain domain = Domain.Find.byId(id);
		return ok(show.render(domain));
	}
	
	public static Result save() {
		Account account = Account.geAccountOrCreate(request().username());
		if(!account.isAdmin()) {
			return forbidden();
		}
		Form<Domain> domainForm = DOMAIN_FORM.bindFromRequest();
		if (domainForm.hasErrors()) {
			List<Domain> domains = Domain.Find.all();
			return ok(index.render(domains, domainForm));
		} else {
			Domain domain = domainForm.get();
			if (domain != null && !Domain.exists(domain) && domain.checkName()) {
				domain.save();
			}
			return redirect(routes.AdminDomains.index());
		}
	}
	
	public static Result inquire(Long id) {
		Account account = Account.geAccountOrCreate(request().username());
		if(!account.isAdmin()) {
			return forbidden();
		}	
		Domain domain = Domain.Find.byId(id);
		if(domain != null) {
			DnsInquireJob job = new DnsInquireJob(domain);
			job.run();
		}
		return redirect(routes.AdminDomains.index());
	}
}