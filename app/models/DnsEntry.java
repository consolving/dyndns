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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class DnsEntry extends Model {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long id;

	public Date created = new Date();
	public Date changed = new Date();
	public Date updated = new Date();
	public Date updated6 = new Date();
	
	public String updatedIp = null;
	public String actualIp = null;

	public String updatedIp6 = null;
	public String actualIp6 = null;
	
	@Required
	public String name;
	public String apiKey = generateApiKey();
	public Boolean toDelete = false;

	@ManyToOne
	@Required
	public Account account;

	@ManyToOne
	public Domain domain;

	@ManyToOne
	@Required
	public SubDomain subDomain;

	public static Finder<Long, DnsEntry> Find = new Finder<Long, DnsEntry>(Long.class, DnsEntry.class);

	public void update(String ip, String pw) {
		if (apiKey.equals(pw.trim()) && (this.actualIp == null || !this.actualIp.equals(ip))) {
			if(ip.indexOf(":") > 0) {
				this.updatedIp6 = ip;
				Ip.getOrCrate(actualIp6);
			} else {
				this.updatedIp = ip;	
				Ip.getOrCrate(actualIp);
			}
			this.changed = new Date();
			this.updated = null;
			this.save();
			ResourceRecord.getOrCreateFromDNSEntry(this);
		}
	}

	public boolean needsUpdate() {
		return !toDelete && updatedIp != null && !updatedIp.equals(actualIp);
	}
	
	public boolean needsUpdate6() {
		return !toDelete && updatedIp6 != null && !updatedIp6.equals(actualIp6);
	}
	
	public boolean hasUpdate() {
		return !toDelete && updatedIp != null && updatedIp.equals(actualIp);
	}
	
	public boolean hasUpdate6() {
		return !toDelete && updatedIp6 != null && updatedIp6.equals(actualIp6);
	}

	public boolean needsSetup6() {
		return  !toDelete && updatedIp6 == null && actualIp6 == null;
	}
	
	public boolean needsSetup() {
		return !toDelete && updatedIp == null && actualIp == null;
	}
	
	public boolean checkName() {
		SubDomain sd = SubDomain.find.byId(subDomain.id);
		if (name.trim().endsWith(sd.name)) {
			name = name.replace("." + sd.name, "").trim();
		}
		return name != null;
	}

	public String toString() {
		return name + "." + subDomain.name;
	}

	public void markToDelete() {
		toDelete = true;
		changed = new Date();
		updatedIp = "";
	}
	
	public static boolean exists(DnsEntry entry) {
		return entry != null && entry.name != null && Find.where().eq("name", entry.name).eq("domain", entry.domain).findRowCount() > 0;
	}

	public static String generateApiKey() {
		String part = "" + System.currentTimeMillis();
		return (UUID.randomUUID().toString().substring(0, 5) + part.substring(part.length() - 5)).toLowerCase();
	}
}
