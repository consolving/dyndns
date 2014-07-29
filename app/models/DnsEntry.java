/**
 * DNSEntry
 * 03.01.2014
 * @author Philipp Haussleiter
 *
 */
package models;

import java.util.Date;

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

	public String name;
	public String fullName;
	public String password;

	@ManyToOne
	@Required
	public Account account;
	@ManyToOne
	@Required
	public Domain domain;

	public static Finder<Long, DnsEntry> find = new Finder<Long, DnsEntry>(Long.class, DnsEntry.class);

	public DnsEntry(Domain domain, Account account, String name) {
		this.domain = domain;
		this.account = account;
		this.name = name;
		this.fullName = name + "." + domain.name;
	}

	public void update(String ip, String pw) {
		if (password.equals(pw.trim()) && !this.actualIp.equals(ip)) {
			this.updatedIp = ip;
			this.changed = new Date();
			this.save();
		}
	}
}
