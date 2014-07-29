package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import play.db.ebean.Model;

@Entity
public class Account extends Model {
	@Id
	public Long id;

	public String username;

	@OneToMany(mappedBy = "domain")
	public List<DnsEntry> dnsEntries;
	
	public static Finder<Long, Account> find = new Finder<Long, Account>(Long.class, Account.class);	
	
}
