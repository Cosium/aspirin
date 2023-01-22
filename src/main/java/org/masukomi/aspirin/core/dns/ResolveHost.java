//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.masukomi.aspirin.core.dns;

import java.util.Collection;
import jakarta.mail.URLName;
import org.masukomi.aspirin.core.delivery.DeliveryContext;
import org.masukomi.aspirin.core.delivery.DeliveryException;
import org.masukomi.aspirin.core.delivery.DeliveryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResolveHost implements DeliveryHandler {
	private static final Logger log = LoggerFactory.getLogger(ResolveHost.class);

	public ResolveHost() {
	}

	public void handle(DeliveryContext dCtx) throws DeliveryException {
		String currentRecipient = dCtx.getQueueInfo().getRecipient();
		String host = currentRecipient.substring(currentRecipient.lastIndexOf("@") + 1);
		Collection<URLName> targetServers = null;

		try {
			targetServers = DnsResolver.getMXRecordsForHost(host);
			if (targetServers != null && !targetServers.isEmpty()) {
				log.trace("ResolveHost.handle(): {} servers found for '{}'.", new Object[]{targetServers.size(), host});
				dCtx.addContextVariable("targetservers", targetServers);
			} else {
				log.warn("ResolveHost.handle(): No mail server found for: '{}'.", new Object[]{host});
				throw new DeliveryException("No MX record found. Temporary failure, trying again.", false);
			}
		} catch (DeliveryException var6) {
			throw var6;
		} catch (Exception var7) {
			log.error("ResolveHost.handle(): Could not get MX for host '" + host + "' defined by recipient '" + currentRecipient + "'.", var7);
			throw new DeliveryException("No MX record found. Temporary failure, trying again.", false);
		}
	}
}
