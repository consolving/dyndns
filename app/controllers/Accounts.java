package controllers;

import java.util.List;

import fileauth.actions.BasicAuth;
import models.Account;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.Accounts.index;

@BasicAuth
public class Accounts extends Controller {
	public static Result index() {
		List<Account> accounts = Account.Find.all();
		return ok(index.render(accounts));
	}
}
