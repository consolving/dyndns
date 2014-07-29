package helpers;

import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import play.Logger;
import play.libs.ws.*;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.ws.WSResponse;

import com.typesafe.config.ConfigFactory;

import models.DnsEntry;
import models.Domain;

public class DnsUpdateHelper {

	private final static String AUTODNS_HOST = ConfigFactory.load().getString(
			"autodns.host");
	private final static String AUTODNS_USERNAME = ConfigFactory.load()
			.getString("autodns.user");
	private final static String AUTODNS_PASSWORD = ConfigFactory.load()
			.getString("autodns.pass");
	private final static String AUTODNS_CONTEXT = ConfigFactory.load()
			.getString("autodns.context");
	private final static String AUTODNS_NS_1 = ConfigFactory.load().getString(
			"autodns.ns1");
	private final static String AUTODNS_NS_2 = ConfigFactory.load().getString(
			"autodns.ns2");
	private final static String AUTODNS_NS_3 = ConfigFactory.load().getString(
			"autodns.ns3");
	private final static String AUTODNS_NS_4 = ConfigFactory.load().getString(
			"autodns.ns4");
	private Domain domain;

	public DnsUpdateHelper(Domain domain) {
		this.domain = domain;
	}

	public void update() {
		String message = getHeader() + buildUpdateList() + getFooter();
		Logger.debug("sending Update: \n" + message + "\n");
		performUpdate(message);
	}

	// TODO make ttl configurable
	// TODO move string to template
	private String getHeader() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
				+ "<request>" + "<auth>" + "<user>"
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
				+ "<code>"
				+ domain.code
				+ "</code>"
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
				+ "<ttl>3600</ttl>"
				+ "</main>"
				+ "<www_include>1</www_include>"
				+ "<soa>"
				+ "<ttl>86400</ttl>"
				+ "<refresh>39940</refresh>"
				+ "<retry>14400</retry>"
				+ "<expire>604800</expire>"
				+ "<email>"
				+ domain.hostmaster
				+ "</email>"
				+ "</soa>"
				+ "<nserver>"
				+ "<name>"
				+ AUTODNS_NS_1
				+ "</name>"
				+ "<ttl>86400</ttl>"
				+ "</nserver>"
				+ "<nserver>"
				+ "<name>"
				+ AUTODNS_NS_2
				+ "</name>"
				+ "<ttl>86400</ttl>"
				+ "</nserver>"
				+ "<nserver>"
				+ "<name>"
				+ AUTODNS_NS_3
				+ "</name>"
				+ "<ttl>86400</ttl>"
				+ "</nserver>"
				+ "<nserver>"
				+ "<name>"
				+ AUTODNS_NS_4
				+ "</name>"
				+ "<ttl>86400</ttl>"
				+ "</nserver>"
				+ "<allow_transfer_from/>"
				+ "<free/>"
				+ "<rr>"
				+ "<name>*</name>"
				+ "<ttl>600</ttl>"
				+ "<type>A</type>"
				+ "<value>" + domain.ip + "</value>" + "</rr>";
	}

	// TODO move string to template
	private String getFooter() {
		return "</zone>" + "<ns_group>default</ns_group>" + "<ctid/>"
				+ "</task>" + "</request>";
	}

	/**
	 * We need to update all Entries all Time. API is an all or nothing approach.
	 */
	private void updateEntries() {
		for (DnsEntry entry : domain.dnsEntries) {
			entry.actualIp = entry.updatedIp;
			entry.updated = new Date();
			Logger.info("did update for " + entry.fullName);
			entry.save();
		}
	}

	private void performUpdate(String content) {
		Promise<Document> xmlPromise = WS.url(AUTODNS_HOST)
				.setHeader("Content-Type", "text/xml; charset=utf-8")
				.post(content).map(new Function<WSResponse, Document>() {
					public Document apply(WSResponse response) {
						Document doc = response.asXml();
						if (getUpdateStatus(doc)) {
							Logger.info("success!");
							updateEntries();
						}
						return doc;
					}
				});
	}

	private String buildUpdateList() {
		StringBuilder sb = new StringBuilder();
		for (DnsEntry entry : domain.findNeedsToChanged()) {
			sb.append("<rr>").append("<name>").append(entry.fullName)
					.append("</name>").append("<ttl>300</ttl>")
					.append("<type>A</type>").append("<value>")
					.append(entry.updatedIp).append("</value>").append("</rr>");
		}
		return sb.toString();
	}

	private boolean getUpdateStatus(Document doc) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc.getDocumentElement().normalize();
			NodeList layerConfigList = doc.getElementsByTagName("type");
			Node node = layerConfigList.item(0);
			return node != null
					&& node.getTextContent().toLowerCase().equals("success");
		} catch (ParserConfigurationException ex) {
			Logger.warn("could not parse response: %s",
					ex.getLocalizedMessage());
			return false;
		}
	}
}
