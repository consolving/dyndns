/**
 * DnsUpdate 30.12.2013
 *
 * @author Philipp Haussleiter
 *
 */
package controllers;


import models.DnsEntry;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;

public class DnsUpdate extends Controller {

	// TODO move to config
	private final static String LOCAL_NET = "10.10.10";
	public static Result updateIp(String k, String ip) {
		DnsEntry entry = DnsEntry.find.where().eq("apiKey", k.trim())
				.findUnique();
		if (ip == null) {
			ip = getIp();
		}

		if(ip.startsWith(LOCAL_NET)){
			return status(METHOD_NOT_ALLOWED, "won't update with local IPs");
		}
		if (entry != null) {
			entry.update(ip, k);
			return ok("will update "+entry.toString()+" to " + ip);
		}
		return badRequest();
	}

	public static Result update(String k) {
		return updateIp(k, null);
	}

	private static String getIp() {
		StringBuilder sb = new StringBuilder();
		for(String key : request().headers().keySet()){
			sb.append(key).append(" = ").append(request().getHeader(key)).append("\n");
		}
		Logger.info("headers:\n "+sb.toString());
		return request().getQueryString("ip") != null ? request().getQueryString("ip") : request().remoteAddress();
	}
}
