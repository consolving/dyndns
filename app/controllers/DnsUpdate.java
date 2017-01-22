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
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;

public class DnsUpdate extends Controller {
	private static final String USER_AGENT = "User-Agent";
	private static final Pattern IPV4_PATTERN = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
	private static final Pattern IPV6_STD_PATTERN =  Pattern.compile("^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");
	private static final Pattern IPV6_HEX_COMPRESSED_PATTERN = Pattern.compile("^((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)$");
	
	public static Result updateIp(String k, String ip) {
		DnsEntry entry = DnsEntry.Find.where().eq("apiKey", k.trim()).findUnique();
		Logger.info("update for "+entry+" by "+ getAgent());
		if (ip == null) {
			ip = getIp();
		}
		
		if (validate(ip) && entry != null) {
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

	private static boolean validate(final String ip) {
		Matcher matcher;
		matcher = IPV4_PATTERN.matcher(ip);
		if (matcher.matches()) {
			Logger.debug(ip + " matches for IPV4_PATTERN");
			return true;
		}
		matcher = IPV6_STD_PATTERN.matcher(ip);
		if (matcher.matches()) {
			Logger.debug(ip + " matches for IPV6_STD_PATTERN");
			return true;
		}
		matcher = IPV6_HEX_COMPRESSED_PATTERN.matcher(ip);
		if (matcher.matches()) {
			Logger.debug(ip + " matches for IPV6_HEX_COMPRESSED_PATTERN");
			return true;
		}
		return false;
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
