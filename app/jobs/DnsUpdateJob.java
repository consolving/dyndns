package jobs;

import helpers.DnsUpdateHelper;

import java.util.List;
import java.util.Set;

import models.DnsEntry;
import models.Domain;
import play.Logger;

public class DnsUpdateJob implements Runnable {

	public DnsUpdateJob() {
		Logger.info("DnsUpdate scheduled");
	}

	@Override
	public void run() {
		Logger.info("DnsUpdate has started @" + System.currentTimeMillis());
		List<Domain> domains = Domain.find.all();
		for (Domain domain : domains) {
			if (domain.findNeedsToChanged().size() > 0) {
				Logger.info("domain " + domain.name + " updating "
						+ domain.findNeedsToChanged().size() + "/"
						+ domain.dnsEntries.size());
				new DnsUpdateHelper(domain).update();
			}
			Logger.info("no update necessary for " + domain.name);
		}
		Logger.info("DnsUpdate has ended @" + System.currentTimeMillis());
	}

}
