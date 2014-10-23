import java.util.List;
import java.util.Map;

import jobs.DnsUpdateJob;
import models.Domain;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.libs.Yaml;
import play.mvc.Action;
import play.mvc.Http;

import com.avaje.ebean.Ebean;

import fileauth.FileAuthScanJob;

public class Global extends GlobalSettings {
	public void onStart(Application app) {
		InitialData.insert(app);
		Logger.info("@" + System.currentTimeMillis() + " Application has started");

		FileAuthScanJob.schedule();
		DnsUpdateJob.schedule();
	}

	public void onStop(Application app) {
		Logger.info("@" + System.currentTimeMillis() + " Application shutdown...");
	}
	public Action onRequest(Http.Request request, java.lang.reflect.Method actionMethod) {
		
		return super.onRequest(request, actionMethod);
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
				}
				Logger.info("@" + System.currentTimeMillis() + " InitialData done.");
			} catch (org.yaml.snakeyaml.error.YAMLException ex) {
				Logger.warn(ex.getLocalizedMessage(), ex);
			}
		}
	}
}
