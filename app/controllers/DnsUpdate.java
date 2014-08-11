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

	public static Result update(String k, String ip) {
		Logger.info("path: "+request().path());
		//String ip = request().getQueryString("ip");
		debug(request().queryString());
		Logger.info("k: " + k + ", ip: " + ip);
		DnsEntry entry = DnsEntry.find.where().eq("apiKey", k.trim())
				.findUnique();
		if (entry != null) {
			if (ip != null) {
				entry.update(ip, k);
			} else {
				entry.update(request().remoteAddress(), k);
			}
		}
		return ok();
	}

	private static void debug(Map<String,String[]> query){		
		for(String key : query.keySet()){
			StringBuilder sb = new StringBuilder("queryString: ");
			for(String part : query.get(key)){
				sb.append(part).append(" ");
			}
			Logger.info(key+": "+sb.toString());
		}
	}
}
