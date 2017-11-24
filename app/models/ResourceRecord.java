package models;

import static play.data.Form.form;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.data.Form;
import play.data.validation.Constraints.Required;
import play.data.validation.ValidationError;
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
	public Integer pref;

	@ManyToOne
	@Required
	public Domain domain;
	
	public static Finder<Long, ResourceRecord> Find = new Finder<Long, ResourceRecord>(Long.class, ResourceRecord.class);
	
	public final static Form<ResourceRecord> RECORD_FORM = form(ResourceRecord.class);
	
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
	
	public String getType() {
		return this.type;
	}
	
	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public Integer getTtl() {
		return ttl;
	}

	public Integer getPref() {
		return pref;
	}
	
	public DnsEntry getDnsEntry() {	
		return DnsEntry.Find.where().eq("name", name).eq("domain", domain).findUnique();
	}
	
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<ValidationError>();
	    	if("AAAA".equals(type) && !Ip.validIpv6(value)) {
	    		errors.add(new ValidationError("value", value+" is not a valid IPv6!"));
	    	}
	    	if("A".equals(type) && !Ip.validIpv4(value)) {
	    		errors.add(new ValidationError("value", value+" is not a valid IPv4!"));
	    	}         
        return errors.isEmpty() ? null : errors;
    }
    
	public String toString() {
		return id+" "+name+" "+type+" "+value+" "+ttl+" "+domain.toString();
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
		ResourceRecord rr = getOrCreate(dnsEntry.domain, dnsEntry.getSubdomainPart(), type);
		rr.value = value;
		rr.save();
		return rr;
	}	
	
	public static Map<String, String> options() {
		String[] types = { "A", "AAAA", "CAA", "CNAME", "HINFO", "MX", "NAPTR", "NS", "PTR", "SRV", "TXT" };
		LinkedHashMap<String, String> rrTypes = new LinkedHashMap<String, String>();
		for(String type : types) {
			rrTypes.put(type, type);
		}
		return rrTypes;
	}
}
