package models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class ResourceRecord extends Model {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long id;
	
	@Required
	public String name;
	@Required
	public String type;
	public String value;
	public Integer ttl = 1000;

	@ManyToOne
	@Required
	public Domain domain;
	
	public static Finder<Long, ResourceRecord> Find = new Finder<Long, ResourceRecord>(Long.class, ResourceRecord.class);
	
	public static ResourceRecord getOrCreate(Domain domain, String name, String type) {
		ResourceRecord rr = ResourceRecord.Find.where().eq("domain", domain).eq("name", name).eq("type", type).findUnique();
		if(rr == null) {
			rr = new ResourceRecord();
			rr.domain = Domain.Find.byId(domain.id);
			rr.name = name.trim();
			rr.type = type.trim();
			rr.save();
		}
		return rr;
	}
	
	public static ResourceRecord getOrCreateFromDNSEntry(DnsEntry dnsEntry) {
		String type, value;
		if(dnsEntry.updatedIp != null ) {
			type="A";
			value = dnsEntry.actualIp;
		}else{
			type="AAAA";
			value = dnsEntry.actualIp6;
		}
		ResourceRecord rr = getOrCreate(dnsEntry.domain, dnsEntry.name, type);
		rr.value = value;
		rr.save();
		return rr;
	}
	
	public String toString() {
		return name+" "+value+" "+ttl+" "+domain.toString();
	}
	
}
