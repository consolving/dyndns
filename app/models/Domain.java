package models;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import play.db.ebean.Model;

@Entity
public class Domain extends Model {
	@Id
	public Long id;

	public String name;

	public String hostmaster;

	public String ip;

	public String code;

	public boolean forceUpdate = false;
	
	@OneToMany(mappedBy = "domain")
	public List<DnsEntry> dnsEntries;

	public static Finder<Long, Domain> find = new Finder<Long, Domain>(
			Long.class, Domain.class);

	public Set<DnsEntry> findNeedsToChanged() {
		return DnsEntry.find.where().isNotNull("updatedIp")
				.raw("updatedIp IS NOT actualIp").eq("domain", this).findSet();
	}

	public Set<DnsEntry> findValidEntries() {
		return DnsEntry.find.where().isNotNull("updatedIp").eq("domain", this)
				.findSet();
	}

	public String toString() {
		return name;
	}

	public static Map<String, String> optionsFor(Account account) {
		LinkedHashMap<String, String> domains = new LinkedHashMap<String, String>();
		for (Domain d : find.all()) {
			domains.put(d.id.toString(), d.name);
		}
		return domains;
	}

}
