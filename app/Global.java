import java.util.List;
import java.util.Map;

import jobs.DnsUpdateJob;
import models.Domain;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.libs.Yaml;

import com.avaje.ebean.Ebean;
import com.typesafe.config.ConfigFactory;

import fileauth.FileAuthScanJob;

public class Global extends GlobalSettings {
	
	private final static String AUTODNS_NS_1 = ConfigFactory.load().getString("autodns.ns1");
	private final static String AUTODNS_NS_2 = ConfigFactory.load().getString("autodns.ns2");
	private final static String AUTODNS_NS_3 = ConfigFactory.load().getString("autodns.ns3");
	private final static String AUTODNS_NS_4 = ConfigFactory.load().getString("autodns.ns4");

	public void onStart(Application app) {
		InitialData.insert(app);
		Logger.info("@" + System.currentTimeMillis() + " Application has started");

		FileAuthScanJob.schedule();
		DnsUpdateJob.schedule();
	}

	public void onStop(Application app) {
		Logger.info("@" + System.currentTimeMillis() + " Application shutdown...");
	}

	static class InitialData {
		public static void insert(Application app) {
			try {
				Logger.info("@" + System.currentTimeMillis() + " InitialData...");
				if (Ebean.find(Domain.class).findRowCount() == 0) {
					//TODO map secure cast here!
					Map<String, List<Object>> all = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
					// Insert domains first
					Ebean.save(all.get("domains"));
					// Insert subdomains then
					Ebean.save(all.get("subdomains"));
					for(Domain domain : Domain.Find.all()) {
						domain.setNameservers(AUTODNS_NS_1, AUTODNS_NS_2, AUTODNS_NS_3, AUTODNS_NS_4);
						domain.save();
					}
				}
				

				Logger.info("@" + System.currentTimeMillis() + " InitialData done.");
			} catch (org.yaml.snakeyaml.error.YAMLException ex) {
				Logger.warn(ex.getLocalizedMessage(), ex);
			}
		}
	}
}
