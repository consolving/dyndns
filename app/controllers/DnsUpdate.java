/**
 * DnsUpdate 30.12.2013
 *
 * @author Philipp Haussleiter
 *
 */
package controllers;

import models.DnsEntry;
import play.mvc.Controller;
import play.mvc.Result;

public class DnsUpdate extends Controller {

	public static Result update(String fullname, String password) {
		DnsEntry entry = DnsEntry.find.where().eq("fullname", fullname)
				.findUnique();
		if (entry != null) {
			entry.update(request().remoteAddress(), password);
		}
		return ok();
	}

}
