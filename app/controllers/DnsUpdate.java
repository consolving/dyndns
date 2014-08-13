/**
 * DnsUpdate 30.12.2013
 *
 * @author Philipp Haussleiter
 *
 */
package controllers;

import java.util.Map;

import models.DnsEntry;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;

public class DnsUpdate extends Controller {

	public static Result updateIp(String k, String ip) {
		DnsEntry entry = DnsEntry.find.where().eq("apiKey", k.trim())
				.findUnique();
		if (ip == null) {
			ip = request().getQueryString("ip") != null ? request()
					.getQueryString("ip") : request().remoteAddress();
		}

		if (entry != null) {
			entry.update(ip, k);
		}
		return ok("will update to " + ip);
	}

	public static Result update(String k) {
		return updateIp(k, null);
	}

}
