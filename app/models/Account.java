package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import fileauth.FileAuth;
import play.db.ebean.Model;

@Entity
public class Account extends Model {

	public final static Account NO_ACCOUNT = new Account("guest");
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long id;

	public String username;

	@OneToMany(mappedBy = "account")
	public List<DnsEntry> dnsEntries;

	public static Finder<Long, Account> Find = new Finder<Long, Account>(Long.class, Account.class);

	public Account(String username) {
		this.username = username;
	}

	public List<DnsEntry> getEntries() {
		return DnsEntry.Find.where().eq("account", this).findList();
	}
	
	public boolean isAdmin() {
		return FileAuth.contains("root", this.username);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(username);
		return sb.toString();
	}

	public static Account authenticate(String username, String password) {
		if (FileAuth.validate(username, password)) {
			return geAccountOrCreate(username);
		}
		return null;
	}

	public static Account geAccountOrCreate(String username) {
		Account account = Account.Find.where().eq("username", username).findUnique();
		if (account == null) {
			account = new Account(username);
			account.save();
		}
		return account;
	}
}
