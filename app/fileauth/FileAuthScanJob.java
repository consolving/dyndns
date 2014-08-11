package fileauth;

import java.util.concurrent.TimeUnit;

import play.libs.Akka;
import scala.concurrent.duration.Duration;

/**
 * ScanJob Periodically Scan of user/group files.
 * 
 * @author Philipp Haussleiter
 * 
 */

public class FileAuthScanJob implements Runnable {
	@Override
	public void run() {
		FileAuth.scanUsers();
		FileAuth.scanGroups();
	}

	public static void schedule() {
		FileAuthScanJob job = new FileAuthScanJob();
		Akka.system()
				.scheduler()
				.schedule(Duration.create(200, TimeUnit.MILLISECONDS),
						Duration.create(1, TimeUnit.MINUTES),  // run job every 1 minutes
						job, Akka.system().dispatcher());
	}
}
