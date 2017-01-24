package helpers;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.typesafe.config.ConfigFactory;

import models.Domain;
import models.Ip;
import models.ResourceRecord;
import play.Logger;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;

public class DnsInquireHelper {
	
	private final static String AUTODNS_HOST = ConfigFactory.load().getString("autodns.host");
	private final static String AUTODNS_USERNAME = ConfigFactory.load().getString("autodns.user");
	private final static String AUTODNS_PASSWORD = ConfigFactory.load().getString("autodns.pass");
	private final static String AUTODNS_CONTEXT = ConfigFactory.load().getString("autodns.context");
	private final static String AUTODNS_NS_1 = ConfigFactory.load().getString("autodns.ns1");
	
	private Domain domain;
	
	public DnsInquireHelper(Domain domain) {
		this.domain = domain;
	}
	
	public void inquire() {
		String message = getRequest();
		Logger.debug("@"+System.currentTimeMillis()+" sending Inquire for "+domain.name+":\n" + message + "\n");
		performInquire(message);
	}
	
	private String getRequest() {
		return "<request>" 
				+ "<auth>" 
				+ "<user>"
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
				+ "<code>0205</code>"
				+ "<zone>"
				+ "<system_ns>"
				+ AUTODNS_NS_1
				+ "</system_ns>"
				+ "<name>"
				+ domain.name
				+ "</name>"
				+ "</zone>"
				+ "<key></key>"
				+ "</task>"
				+ "</request>";
	}
		
	private void performInquire(String content) {
		Promise<Document> xmlPromise = WS.url(AUTODNS_HOST)
				.setHeader("Content-Type", "text/xml; charset=utf-8")
				.post(content).map(new Function<WSResponse, Document>() {
					public Document apply(WSResponse response) {
						Logger.debug("response: "+response.getBody());
						Document doc = response.asXml();
						if (getUpdateStatus(doc)) {
							Logger.info("@"+System.currentTimeMillis()+" inquire "+domain.name+" success!");
							domain = updatingDomain(doc, domain);
							domain.save();
						} else {
							Logger.error("@"+System.currentTimeMillis()+" inquire "+domain.name+" error:\n", doc.getTextContent());
						}

						return doc;
					}
				});
	}
	
	private static Domain updatingDomain(Document doc, Domain domain) {
		List<ResourceRecord> oldResourceRecords = domain.getResourceRecords();
		List<ResourceRecord> newResourceRecords = new ArrayList<>();
		domain = updatingMain(doc, domain);
		domain = updatingSoa(doc, domain);
		domain = updatingNS(doc, domain);
		NodeList rrList = doc.getElementsByTagName("rr");
		for(int i = 0; i < rrList.getLength(); i++) {
			Node n = rrList.item(i);
			newResourceRecords.add(getRRFromNode(n, domain));
		}
		return domain;
	}
	private static Domain updatingMain(Document doc, Domain domain) {
		NodeList zoneList = doc.getElementsByTagName("zone");
		if(zoneList.getLength() == 1) {
			NodeList elements = zoneList.item(0).getChildNodes();
			for(int i = 0; i < elements.getLength(); i++) {
				Logger.debug("updatingMain - "+i+" "+elements.item(i).getTextContent());
				switch(elements.item(i).getNodeName().toLowerCase()) {
				case "main":
					Node n = elements.item(i);
					for(int j = 0; j < n.getChildNodes().getLength(); j++) {
						Logger.debug("updatingMain - "+i+"."+j+" "+n.getTextContent());
						if("value".equals( n.getChildNodes().item(j).getNodeName())) {
							domain.ip = n.getChildNodes().item(0).getTextContent();
						}						
					}

					break;
				}
			}			
		}
		return domain;
	}
	
	private static Domain updatingSoa(Document doc, Domain domain) {
		NodeList soaList = doc.getElementsByTagName("soa");
		if(soaList.getLength() > 0) {
			Node soa = soaList.item(0);			
			NodeList elements =  soa.getChildNodes();
			for(int j = 0; j < elements.getLength(); j++) {
				Logger.debug("updatingSoa - "+j+" "+elements.item(j).getTextContent());
				switch(elements.item(j).getNodeName().toLowerCase()) {
				case "refresh":
					domain.soaRefresh = getInteger(elements.item(j).getTextContent());
					break;
				case "retry":
					domain.soaRetry = getInteger(elements.item(j).getTextContent());
					break;
				case "expire":
					domain.soaExpire = getInteger(elements.item(j).getTextContent());
					break;					
				case "ttl":
					domain.soaTtl = getInteger(elements.item(j).getTextContent());
					break;	
				case "email":
					domain.soaEmail = elements.item(j).getTextContent();
					break;	
				case "default":
					domain.soaDefault = getInteger(elements.item(j).getTextContent()); 
					break;
				}
			}
		}
		return domain;
	}
	
	private static Domain updatingNS(Document doc, Domain domain) {
		NodeList nsList = doc.getElementsByTagName("nserver");
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < nsList.getLength(); i++) {
			Node n = nsList.item(i);
			Logger.debug("updatingNS - "+i+" "+n.getTextContent());
			NodeList elements =  n.getChildNodes();
			for(int j = 0; j < elements.getLength(); j++) {
				Logger.debug("updatingMain - "+i+"."+j+" "+elements.item(j).getTextContent());
				if("name".equals(elements.item(j).getNodeName().toLowerCase())) {
					sb.append(elements.item(j).getTextContent().trim()).append("\n");									
				}
			}
		}
		domain.nameservers = sb.toString();
		return domain;
	}
	
	private static ResourceRecord getRRFromNode(Node node, Domain domain) {
		String name = null;
		String value = null;
		String type = null;
		Integer ttl = null;
		Integer pref = null;
		ResourceRecord rr = null;
		NodeList elements =  node.getChildNodes();
		for(int i = 0; i < elements.getLength(); i++) {
			Node n = elements.item(i);
			Logger.debug("getRRFromNode - "+i+" "+n.getTextContent());
			switch(n.getNodeName().toLowerCase()) {
			case "name":
				name = n.getTextContent();
				break;
			case "value":
				value = n.getTextContent();
				if(Ip.valid(value)) {
					Ip.getOrCrate(value);
				}
				break;				
			case "type":
				type = n.getTextContent();
				break;
			case "ttl":
				ttl = getInteger(n.getTextContent());
				break;
			case "pref":
				pref = getInteger(n.getTextContent());
				break;				
			}
		}
		if(name != null && type != null && value != null) {
			rr = ResourceRecord.getOrCreate(domain, name, type);	
			rr.value = value;
		}
		rr.ttl = ttl;
		rr.pref = pref;
		rr.save();
		return rr;
	}
	
	private static Integer getInteger(String value) {
		try {
			return Integer.parseInt(value);
		} catch( NumberFormatException ex) {
			Logger.error(ex.getLocalizedMessage(), ex);
		}
		return 0;
	}
	
	private boolean getUpdateStatus(Document doc) {
		doc.getDocumentElement().normalize();
		NodeList layerConfigList = doc.getElementsByTagName("type");
		for(int i = 0; i < layerConfigList.getLength(); i++) {
			Node n = layerConfigList.item(i);
			if( n.getTextContent().toLowerCase().equals("success") ) {
				return true;
			}
		}
		return false;
	}	
}
