package jobs;

import helpers.DnsUpdateHelper;

import java.util.List;
import java.util.concurrent.TimeUnit;

import models.Domain;
import play.Logger;
import play.libs.Akka;
import scala.concurrent.duration.Duration;

public class DnsUpdateJob implements Runnable {

	public DnsUpdateJob() {
		Logger.info("@" + System.currentTimeMillis() + " DnsUpdate scheduled");
	}

	public static void schedule() {
		DnsUpdateJob job = new DnsUpdateJob();
		Akka.system().scheduler().schedule(Duration.create(500, TimeUnit.MILLISECONDS), // initial
																						// delay
				Duration.create(1, TimeUnit.MINUTES), // run job every 1 minutes
				job, Akka.system().dispatcher());
	}

	@Override
	public void run() {
		Logger.info("@" + System.currentTimeMillis() + " DnsUpdate has started");
		try {
			List<Domain> domains = Domain.find.all();
			for (Domain domain : domains) {
				if (domain.findNeedsToChanged().size() > 0 || domain.forceUpdate) {
					Logger.info("@" + System.currentTimeMillis() + " updating domain " + domain.name + " "
							+ domain.findNeedsToChanged().size() + "/" + domain.dnsEntries.size());
					new DnsUpdateHelper(domain).update();
				}
				Logger.info("@" + System.currentTimeMillis() + " no update necessary for " + domain.name);
			}
		} catch (Exception ex) {
			Logger.warn(ex.getLocalizedMessage(), ex);
		}
		Logger.info("@" + System.currentTimeMillis() + " DnsUpdate has ended");
	}

}
