package models;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class SubDomain extends Model {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long id;

	@Required
	public String name;

	@ManyToOne
	@Required
	public Domain domain;

	@OneToMany(mappedBy = "subDomain")
	public List<DnsEntry> dnsEntries;

	public static Finder<Long, SubDomain> find = new Finder<Long, SubDomain>(Long.class, SubDomain.class);

	public String toString() {
		return name;
	}

	public static Map<String, String> optionsFor(Account account) {
		LinkedHashMap<String, String> subDomains = new LinkedHashMap<String, String>();
		for (SubDomain d : find.all()) {
			subDomains.put(d.id.toString(), d.name);
		}
		return subDomains;
	}
}
