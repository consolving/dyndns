package jobs;

import helpers.DnsInquireHelper;
import models.Domain;

public class DnsInquireJob implements Runnable {

	private Domain domain;
	
	public DnsInquireJob(Domain domain) {
		this.domain = domain;
	}
	
	@Override
	public void run() {
		DnsInquireHelper helper = new DnsInquireHelper(domain);
		helper.inquire();
	}

}
