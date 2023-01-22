//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.masukomi.aspirin.core;

import java.util.ArrayList;
import java.util.Collection;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMessage.RecipientType;
import org.masukomi.aspirin.core.config.Configuration;
import org.masukomi.aspirin.core.delivery.DeliveryManager;
import org.masukomi.aspirin.core.listener.AspirinListener;
import org.masukomi.aspirin.core.listener.ListenerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AspirinInternal {
	private static final Logger log = LoggerFactory.getLogger(AspirinInternal.class);
	private volatile Session defaultSession = null;
	private Integer idCounter = 0;
	private Object idCounterLock = new Object();
	private final Configuration configuration;
	private final ListenerManager listenerManager;
	private final DeliveryManager deliveryManager;
	private final Helper helper;

	public AspirinInternal(Configuration configuration, DeliveryManager deliveryManager, ListenerManager listenerManager) {
		this.configuration = configuration;
		this.deliveryManager = deliveryManager;
		this.listenerManager = listenerManager;
		this.helper = new Helper(configuration);
	}

	public void start() {
		if (!this.deliveryManager.isAlive()) {
			this.deliveryManager.start();
		}

	}

	public Configuration getConfiguration() {
		return this.configuration;
	}

	protected void add(MimeMessage msg) throws MessagingException {
		if (!this.deliveryManager.isAlive()) {
			this.deliveryManager.start();
		}

		this.deliveryManager.add(msg);
	}

	public void add(MimeMessage msg, long expiry) throws MessagingException {
		if (0L < expiry) {
			this.helper.setExpiry(msg, expiry);
		}

		this.add(msg);
	}

	public void addListener(AspirinListener listener) {
		this.listenerManager.add(listener);
	}

	public void remove(String mailid) throws MessagingException {
		this.deliveryManager.remove(mailid);
	}

	public void removeListener(AspirinListener listener) {
		if (this.listenerManager != null) {
			this.listenerManager.remove(listener);
		}

	}

	public MimeMessage createNewMimeMessage() {
		if (this.defaultSession == null) {
			this.defaultSession = Session.getDefaultInstance(System.getProperties());
		}

		MimeMessage mMesg = new MimeMessage(this.defaultSession);
		synchronized(this.idCounterLock) {
			long nowTime = System.currentTimeMillis() / 1000L;
			StringBuilder var10000 = (new StringBuilder()).append(nowTime).append(".");
			Integer var6 = this.idCounter;
			Integer var7 = this.idCounter = this.idCounter + 1;
			String newId = var10000.append(Integer.toHexString(var6)).toString();

			try {
				mMesg.setHeader("X-Aspirin-MailID", newId);
			} catch (MessagingException var9) {
				log.warn("Aspirin Mail ID could not be generated.", var9);
				var9.printStackTrace();
			}

			return mMesg;
		}
	}

	public static Collection<InternetAddress> extractRecipients(MimeMessage message) throws MessagingException {
		Collection<InternetAddress> recipients = new ArrayList();
		Message.RecipientType[] types = new Message.RecipientType[]{RecipientType.TO, RecipientType.CC, RecipientType.BCC};
		Message.RecipientType[] var4 = types;
		int var5 = types.length;

		for(int var6 = 0; var6 < var5; ++var6) {
			Message.RecipientType recType = var4[var6];
			Address[] addresses = message.getRecipients(recType);
			if (addresses != null) {
				Address[] var8 = addresses;
				int var9 = addresses.length;

				for(int var10 = 0; var10 < var9; ++var10) {
					Address addr = var8[var10];

					try {
						recipients.add((InternetAddress)addr);
					} catch (Exception var13) {
						log.warn("Recipient parsing failed.", var13);
					}
				}
			}
		}

		return recipients;
	}

	public DeliveryManager getDeliveryManager() {
		return this.deliveryManager;
	}

	public ListenerManager getListenerManager() {
		return this.listenerManager;
	}

	public void shutdown() {
		this.deliveryManager.shutdown();
	}
}
