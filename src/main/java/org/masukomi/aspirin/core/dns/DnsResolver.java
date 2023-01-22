//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.masukomi.aspirin.core.dns;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Vector;
import jakarta.mail.URLName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;

public class DnsResolver {
	private static final Logger log = LoggerFactory.getLogger(DnsResolver.class);
	public static final String SMTP_PROTOCOL_PREFIX = "smtp://";

	public DnsResolver() {
	}

	public static Collection<URLName> getMXRecordsForHost(String hostName) {
		Vector<URLName> recordsColl = null;

		try {
			boolean foundOriginalMX = true;
			Record[] records = (new Lookup(hostName, 15)).run();
			if (records == null || records.length == 0) {
				foundOriginalMX = false;

				for(String upperLevelHostName = hostName; records == null && upperLevelHostName.indexOf(".") != upperLevelHostName.lastIndexOf(".") && upperLevelHostName.lastIndexOf(".") != -1; records = (new Lookup(upperLevelHostName, 15)).run()) {
					upperLevelHostName = upperLevelHostName.substring(upperLevelHostName.indexOf(".") + 1);
				}
			}

			if (records != null) {
				Arrays.sort(records, new Comparator<Record>() {
					public int compare(Record arg0, Record arg1) {
						return ((MXRecord)arg0).getPriority() - ((MXRecord)arg1).getPriority();
					}
				});
				recordsColl = new Vector(records.length);

				for(int i = 0; i < records.length; ++i) {
					MXRecord mx = (MXRecord)records[i];
					String targetString = mx.getTarget().toString();
					URLName uName = new URLName("smtp://" + targetString.substring(0, targetString.length() - 1));
					recordsColl.add(uName);
				}
			} else {
				foundOriginalMX = false;
				recordsColl = new Vector();
			}

			if (!foundOriginalMX) {
				Record[] recordsTypeA = (new Lookup(hostName, 1)).run();
				if (recordsTypeA != null && recordsTypeA.length > 0) {
					recordsColl.add(0, new URLName("smtp://" + hostName));
				}
			}
		} catch (TextParseException var8) {
			log.warn("DnsResolver.getMXRecordsForHost(): Failed get MX record for host '" + hostName + "'.", var8);
		}

		return recordsColl;
	}
}
