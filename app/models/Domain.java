package models;

import java.util.List;
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

	@OneToMany(mappedBy = "domain")
	public List<DnsEntry> dnsEntries;

	public static Finder<Long, Domain> find = new Finder<Long, Domain>(
			Long.class, Domain.class);

	public Set<DnsEntry> findNeedsToChanged() {
		return DnsEntry.find.where().raw("updatedIp IS NOT actualIp").eq("domain", this).findSet();
	}
}
