package jobs;

import helpers.DnsUpdateHelper;

import java.util.List;
import java.util.Set;

import models.DnsEntry;
import models.Domain;
import play.Logger;

public class DnsUpdateJob implements Runnable {

	public DnsUpdateJob() {
		Logger.info("@"+System.currentTimeMillis()+" DnsUpdate scheduled");
	}

	@Override
	public void run() {
		Logger.info("@" + System.currentTimeMillis() + " DnsUpdate has started");
		List<Domain> domains = Domain.find.all();
		for (Domain domain : domains) {
			if (domain.findNeedsToChanged().size() > 0 || domain.forceUpdate) {
				Logger.info("@"+System.currentTimeMillis()+" updating domain " + domain.name+" "
						+ domain.findNeedsToChanged().size() + "/"
						+ domain.dnsEntries.size());
				new DnsUpdateHelper(domain).update();
			}
			Logger.info("@"+System.currentTimeMillis()+" no update necessary for " + domain.name);
		}
		Logger.info("@" + System.currentTimeMillis() + " DnsUpdate has ended");
	}

}
