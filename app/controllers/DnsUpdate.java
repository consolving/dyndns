/**
 * DnsUpdate 30.12.2013
 *
 * @author Philipp Haussleiter
 *
 */
package controllers;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.kenshoo.play.metrics.MetricsRegistry;

import models.DnsEntry;
import models.Ip;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;

public class DnsUpdate extends Controller {
	private static final String USER_AGENT = "User-Agent";
	
	public static Result updateIp(String k, String ip) {
		DnsEntry entry = DnsEntry.Find.where().eq("apiKey", k.trim()).findUnique();
		Logger.info("update for "+entry+" by "+ getAgent());
		if (ip == null) {
			ip = getIp();
		}
		
		if (Ip.valid(ip) && entry != null) {
			entry.update(ip, k);
			if(entry.actualIp != null && entry.actualIp.equals(ip)) {
				MetricsRegistry.defaultRegistry().meter("DnsUpdate-nochg-ip4").mark();
				return ok("nochg " + ip);
			} else if(entry.actualIp6 != null && entry.actualIp6.equals(ip)) {
				MetricsRegistry.defaultRegistry().meter("DnsUpdate-nochg-ip6").mark();
				return ok("nochg " + ip);
			} else {
				MetricsRegistry.defaultRegistry().meter("DnsUpdate-good").mark();
				// TODO this should be checked for each, ipv4 and ipv6.
				return ok("good " + ip);				
			}
		}
		return badRequest("nohost");
	}

	public static Result update(String k) {
		return updateIp(k, null);
	}
	
	private static String getAgent() {
		return request().hasHeader(USER_AGENT) ? request().getHeader(USER_AGENT) : "N/A";
	}
	
	private static String getIp() {
		StringBuilder sb = new StringBuilder();
		for(String key : request().headers().keySet()) {
			if(!key.equals("Authorization")) {
				sb.append(key).append(" = ").append(request().getHeader(key)).append("\n");				
			}
		}
		Logger.info("headers:\n"+sb.toString());
		String ip = request().getQueryString("ip") != null ? 
				request().getQueryString("ip") : request().getHeader("X-Forwarded-For") != null ? 
						request().getHeader("X-Forwarded-For") : request().remoteAddress();
		return ip;
	}
}
