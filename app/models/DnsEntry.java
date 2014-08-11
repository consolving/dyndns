/**
 * DNSEntry
 * 03.01.2014
 * @author Philipp Haussleiter
 *
 */
package models;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class DnsEntry extends Model {
	@Id
	public Long id;

	public Date created = new Date();
	public Date updated;
	public Date changed = new Date();

	public String updatedIp;
	public String actualIp;

	@Required
	public String name;
	public String apiKey;

	@ManyToOne
	@Required
	public Account account;
	@ManyToOne
	@Required
	public Domain domain;

	public static Finder<Long, DnsEntry> find = new Finder<Long, DnsEntry>(
			Long.class, DnsEntry.class);

	public DnsEntry() {
		this.apiKey = generateApiKey();
	}

	public DnsEntry(Domain domain, Account account, String name) {
		this.domain = domain;
		this.account = account;
		this.name = name;
		this.apiKey = generateApiKey();
	}

	public void update(String ip, String pw) {
		if (apiKey.equals(pw.trim()) && !this.actualIp.equals(ip)) {
			this.updatedIp = ip;
			this.changed = new Date();
			this.save();
		}
	}

	public static boolean exists(String name) {
		return find.where().eq("name", name).findRowCount() > 0;
	}

	private final static String generateApiKey() {
		String part = "" + System.currentTimeMillis();
		return (UUID.randomUUID().toString().substring(0, 5) + part.substring(
				0, 5)).toLowerCase();
	}
}
