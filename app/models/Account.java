package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import fileauth.FileAuth;
import play.db.ebean.Model;

@Entity
public class Account extends Model {
	@Id
	public Long id;

	public String username;

	@OneToMany(mappedBy = "domain")
	public List<DnsEntry> dnsEntries;

	public static Finder<Long, Account> find = new Finder<Long, Account>(
			Long.class, Account.class);

	public Account(String username) {
		this.username = username;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		return sb.toString();
	}

	public static Account authenticate(String username, String password) {
		if (FileAuth.validate(username, password)) {
			return new Account(username);
		}
		return null;
	}

	public static Account geAccount(String username) {
		Account account = new Account(username);
		return account;
	}
}
