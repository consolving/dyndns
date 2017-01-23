package helpers;

import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.typesafe.config.ConfigFactory;

import models.DnsEntry;
import models.Domain;
import models.ResourceRecord;
import play.Logger;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;

public class DnsUpdateHelper {

	private final static String AUTODNS_HOST = ConfigFactory.load().getString("autodns.host");
	private final static String AUTODNS_USERNAME = ConfigFactory.load().getString("autodns.user");
	private final static String AUTODNS_PASSWORD = ConfigFactory.load().getString("autodns.pass");
	private final static String AUTODNS_CONTEXT = ConfigFactory.load().getString("autodns.context");
	private final static String AUTODNS_NS_1 = ConfigFactory.load().getString("autodns.ns1");
	private final static String AUTODNS_NS_2 = ConfigFactory.load().getString("autodns.ns2");
	private final static String AUTODNS_NS_3 = ConfigFactory.load().getString("autodns.ns3");
	private final static String AUTODNS_NS_4 = ConfigFactory.load().getString("autodns.ns4");
	private Domain domain;

	private final static int SUBDOMAIN_TTL = parseInteger("autodns.subdomain.ttl", 60);
	private final static int DOMAIN_TTL = parseInteger("autodns.domain.ttl", 3600);	
	private final static int NS_TTL = parseInteger("autodns.nameserver.ttl", 86400);	
	
	public DnsUpdateHelper(Domain domain) {
		this.domain = domain;
	}

	public void update() {
		String message = getHeader() + buildUpdateList() + getFooter();
		Logger.debug("@"+System.currentTimeMillis()+" sending Update for "+domain.name+":\n" + message + "\n");
		performUpdate(message);
	}
	
	// TODO move string to template
	private String getHeader() {
		return "<request>" + "<auth>" + "<user>"
				+ AUTODNS_USERNAME
				+ "</user>"
				+ "<password>"
				+ AUTODNS_PASSWORD
				+ "</password>"
				+ "<context>"
				+ AUTODNS_CONTEXT
				+ "</context>"
				+ "</auth>"
				+ "<task>"
				+ "<code>0202</code>"
				+ "<zone>"
				+ "<internal_ns>"
				+ AUTODNS_NS_1
				+ "</internal_ns>"
				+ "<name>"
				+ domain.name
				+ "</name>"
				+ "<ns_action>complete</ns_action>"
				+ "<main>"
				+ "<value>"
				+ domain.ip
				+ "</value>"
				+ "<ttl>"+DOMAIN_TTL+"</ttl>"
				+ "</main>"
				+ "<www_include>1</www_include>"
				+ "<soa>"
				+ "<ttl>86400</ttl>"
				+ "<refresh>39940</refresh>"
				+ "<retry>14400</retry>"
				+ "<expire>604800</expire>"
				+ "<email>"
				+ domain.soaEmail
				+ "</email>"
				+ "</soa>"
				+ "<nserver>"
				+ "<name>"
				+ AUTODNS_NS_1
				+ "</name>"
				+ "<ttl>"+NS_TTL+"</ttl>"
				+ "</nserver>"
				+ "<nserver>"
				+ "<name>"
				+ AUTODNS_NS_2
				+ "</name>"
				+ "<ttl>"+NS_TTL+"</ttl>"
				+ "</nserver>"
				+ "<nserver>"
				+ "<name>"
				+ AUTODNS_NS_3
				+ "</name>"
				+ "<ttl>"+NS_TTL+"</ttl>"
				+ "</nserver>"
				+ "<nserver>"
				+ "<name>"
				+ AUTODNS_NS_4
				+ "</name>"
				+ "<ttl>"+NS_TTL+"</ttl>"
				+ "</nserver>"
				+ "<allow_transfer_from/>"
				+ "<free/>"
				+ "<rr>"
				+ "<name>*</name>"
				+ "<ttl>"+SUBDOMAIN_TTL+"</ttl>"
				+ "<type>A</type>"
				+ "<value>" + domain.ip + "</value>" + "</rr>";
	}

	// TODO move string to template
	private String getFooter() {
		return 	"</zone>" 
				+ "<ns_group>default</ns_group>" 
				+ "<ctid/>"
				+ "</task>" 
				+ "</request>";
	}

	/**
	 * We need to update all Entries all Time. API is an all or nothing approach.
	 */
	private void updateEntries() {
		for (DnsEntry entry : domain.findNeedsToChanged()) {
			entry.actualIp = entry.updatedIp;
			entry.actualIp6 = entry.updatedIp6;
			if(entry.needsUpdate6()) { 
				entry.updated6 = new Date();
			} else {
				entry.updated = new Date();				
			}
			Logger.info("@"+System.currentTimeMillis()+" did update for " + entry);
			entry.save();
		}
		domain.forceUpdate = false;
		domain.save();
	}

	private void performUpdate(String content) {
		Promise<Document> xmlPromise = WS.url(AUTODNS_HOST)
				.setHeader("Content-Type", "text/xml; charset=utf-8")
				.post(content).map(new Function<WSResponse, Document>() {
					public Document apply(WSResponse response) {
						Document doc = response.asXml();
						if (getUpdateStatus(doc)) {
							Logger.info("@"+System.currentTimeMillis()+" updating "+domain.name+" success!");
							updateEntries();
						} else {
							Logger.error("@"+System.currentTimeMillis()+" updating "+domain.name+" error:\n", doc.getTextContent());
						}
						return doc;
					}
				});
	}
	private static String getCName(String fullName, String domainName){
		return fullName.replace("."+domainName, "").trim();
	}
	
	private String buildUpdateList() {
		StringBuilder sb = new StringBuilder();
		for (DnsEntry entry : domain.findValidEntries()) {
			if(!entry.toDelete) {
				ResourceRecord rr = ResourceRecord.getOrCreateFromDNSEntry(entry);
				if(entry.updatedIp6 != null) {
					sb.append("<rr>")
					.append("<name>")
					.append(entry.getSubdomainPart())
					.append("</name>")
					.append("<ttl>"+SUBDOMAIN_TTL+"</ttl>")	
					.append("<type>AAAA</type>")
					.append("<value>")
					.append(entry.updatedIp6)
					.append("</value>")
					.append("</rr>");	
					rr.value = entry.updatedIp6;
				}
				if(entry.updatedIp != null) {
					sb.append("<rr>")
					.append("<name>")
					.append(getCName(entry.name+"."+entry.subDomain.name, entry.domain.name))
					.append("</name>")
					.append("<ttl>"+SUBDOMAIN_TTL+"</ttl>")							
					.append("<type>A</type>")
					.append("<value>")
					.append(entry.updatedIp)
					.append("</value>")
					.append("</rr>");	
					rr.value = entry.updatedIp;
				}
				rr.save();
			} else {
				ResourceRecord rr = ResourceRecord.getOrCreateFromDNSEntry(entry);
				if(rr != null) {
					rr.delete();
					Logger.info("@"+System.currentTimeMillis()+" deleted "+rr);
				}
				entry.delete();
				Logger.info("@"+System.currentTimeMillis()+" deleted "+entry);
			}
		}
		return sb.toString();
	}

	private boolean getUpdateStatus(Document doc) {
		doc.getDocumentElement().normalize();
		NodeList layerConfigList = doc.getElementsByTagName("type");
		Node node = layerConfigList.item(0);
		return node != null && node.getTextContent().toLowerCase().equals("success");
	}
	
	private static int parseInteger(String key, int fallback){
		if(key != null && ConfigFactory.load().hasPath(key)){	
			try {
				return Integer.parseInt(ConfigFactory.load().getString(key));
			} catch(NumberFormatException ex){
				Logger.warn("cannot parse "+ex.getLocalizedMessage());
			}
		}
		return fallback;
	}	
}
