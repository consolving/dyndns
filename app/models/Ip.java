package models;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import play.db.ebean.Model;

@Entity
public class Ip extends Model {
	private static final Pattern IPV4_PATTERN = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
	private static final Pattern IPV6_STD_PATTERN =  Pattern.compile("^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");
	private static final Pattern IPV6_HEX_COMPRESSED_PATTERN = Pattern.compile("^((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)$");

	public static Finder<Long, Ip> Find = new Finder<Long, Ip>(Long.class, Ip.class);
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long id;
	
	public String value;
	public String type;
	
	public static Ip getOrCrate(String value) {
		Ip ip = Ip.Find.where().eq("value", value.trim()).findUnique();
		if(ip == null && valid(value)) {
			ip = new Ip();
			ip.value = value.trim();
			Matcher matcher = IPV4_PATTERN.matcher(value.trim());
			if(matcher.matches()) {
				ip.type = "ipv4";
			} else {
				ip.type ="ipv6";
			}
			ip.save();
		}
		return ip;
	}
	
	public static boolean valid(String value) {
		Matcher matcher;
		matcher = IPV4_PATTERN.matcher(value.trim());
		if(matcher.matches()) {
			return true;
		}
		matcher = IPV6_STD_PATTERN.matcher(value.trim());
		if(matcher.matches()) {
			return true;
		}
		matcher = IPV6_HEX_COMPRESSED_PATTERN.matcher(value.trim());
		if(matcher.matches()) {
			return true;
		}		
		return false;
	}
}
