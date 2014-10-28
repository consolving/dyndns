/**
 * DnsUpdate 30.12.2013
 *
 * @author Philipp Haussleiter
 *
 */
package controllers;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.DnsEntry;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;

public class DnsUpdate extends Controller {

	private static final Pattern PATTERN = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
	public static Result updateIp(String k, String ip) {
		DnsEntry entry = DnsEntry.find.where().eq("apiKey", k.trim()).findUnique();
		if (ip == null) {
			ip = getIp();
		}
		
		if (validate(ip) && entry != null) {
			entry.update(ip, k);
			if(entry.actualIp.equals(ip)) {
				return ok("nochg " + ip);
			} else {
				return ok("good " + ip);				
			}
		}
		
		return badRequest("nohost");
	}

	public static Result update(String k) {
		return updateIp(k, null);
	}

	private static boolean validate(final String ip) {
	      Matcher matcher = PATTERN.matcher(ip);
	      return matcher.matches();             
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
