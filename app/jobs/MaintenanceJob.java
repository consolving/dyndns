package jobs;

import models.DnsEntry;
import models.Domain;
import models.Ip;
import models.ResourceRecord;
import play.Logger;

public class MaintenanceJob implements Runnable {

	@Override
	public void run() {
		Logger.info("@" + System.currentTimeMillis() + " MaintenanceJob started");
		for(Domain domain : Domain.Find.all()) {
			Ip.getOrCrate(domain.ip);
			for(DnsEntry entry : domain.findValidEntries()) {
				Ip.getOrCrate(entry.actualIp6);
				Ip.getOrCrate(entry.actualIp);
				ResourceRecord.getOrCreateFromDNSEntry(entry);
			}
		}
		Logger.info("@" + System.currentTimeMillis() + " MaintenanceJob ended");
	}
}
