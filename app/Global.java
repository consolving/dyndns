import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import models.Domain;

import com.avaje.ebean.Ebean;

import fileauth.FileAuthScanJob;
import jobs.DnsUpdateJob;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.libs.Akka;
import play.libs.Yaml;
import scala.concurrent.duration.Duration;

public class Global extends GlobalSettings {
	public void onStart(Application app) {
		InitialData.insert(app);
		Logger.info("@"+System.currentTimeMillis()+" Application has started");
		DnsUpdateJob job = new DnsUpdateJob();
		Akka.system().scheduler()
				.schedule(Duration.create(500, TimeUnit.MILLISECONDS), // initial delay
						Duration.create(1, TimeUnit.MINUTES), // run job every 1 minutes
						job, Akka.system().dispatcher());
		FileAuthScanJob.schedule();

	}

	public void onStop(Application app) {
		Logger.info("@"+System.currentTimeMillis()+" Application shutdown...");
	}

	static class InitialData {
		public static void insert(Application app) {
			try {
				Logger.info("@"+System.currentTimeMillis()+" InitialData...");
				if (Ebean.find(Domain.class).findRowCount() == 0) {
					Map<String, List<Object>> all = (Map<String, List<Object>>) Yaml.load("initial-data.yml");
					// Insert domains first
					Ebean.save(all.get("domains"));
					// Insert subdomains then
					Ebean.save(all.get("subdomains"));
				}
				Logger.info("@"+System.currentTimeMillis()+" InitialData done.");		
			} catch(org.yaml.snakeyaml.error.YAMLException ex) {
				Logger.warn(ex.getLocalizedMessage(), ex);
			}
		}
	}
}
