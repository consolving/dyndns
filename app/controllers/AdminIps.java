package controllers;

import java.util.List;

import fileauth.actions.BasicAuth;
import models.Account;
import models.Ip;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.AdminIps.index;
import views.html.AdminIps.show;

@BasicAuth
public class AdminIps extends Application {

	public static Result index() {
		Account account = Account.geAccountOrCreate(request().username());
		if(!account.isAdmin()) {
			return forbidden();
		}		
		List<Ip> ips = Ip.Find.all();
		return ok(index.render(ips));
	}
	
	public static Result show(String value) {
		Account account = Account.geAccountOrCreate(request().username());
		if(!account.isAdmin()) {
			return forbidden();
		}		
		Ip ip = Ip.Find.where().eq("value", value).findUnique();
		if(ip == null) {
			return redirect(routes.AdminIps.index());
		}
		return ok(show.render(ip));		
	}
}
