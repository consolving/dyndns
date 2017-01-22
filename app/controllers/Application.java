package controllers;

import java.util.List;

import play.mvc.Controller;
import play.mvc.Result;

import com.typesafe.config.ConfigFactory;

import fileauth.actions.BasicAuth;
import models.Account;
import models.DnsEntry;
import views.html.Application.*;

@BasicAuth
public class Application extends Controller {
	
	
	public final static boolean REQUEST_SECURE = getRequestSecure();
			
	public static Result index() {
		Account account = Account.geAccountOrCreate(request().username());
		List<DnsEntry> entries = account.dnsEntries;
		if (account.isAdmin()) {
			entries = DnsEntry.Find.all();
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

	private static Boolean getRequestSecure() {
		if(ConfigFactory.load().hasPath("request.secure")){
			return Boolean.parseBoolean(ConfigFactory.load().getString("request.secure"));
		}
		return false;
	}
}
