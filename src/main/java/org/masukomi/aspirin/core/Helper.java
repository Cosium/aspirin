//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.masukomi.aspirin.core;

import java.text.SimpleDateFormat;
import java.util.Date;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.masukomi.aspirin.core.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Helper {
	private static final Logger log = LoggerFactory.getLogger(Helper.class);
	public final SimpleDateFormat expiryFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	private final Configuration configuration;

	public Helper(Configuration configuration) {
		this.configuration = configuration;
	}

	public String formatExpiry(Date date) {
		return this.expiryFormat.format(date);
	}

	public long getExpiry(MimeMessage message) {
		try {
			String[] headers = message.getHeader("X-Aspirin-Expiry");
			if (headers != null && 0 < headers.length) {
				return this.expiryFormat.parse(headers[0]).getTime();
			}
		} catch (Exception var5) {
			log.error("Expiration header could not be get from MimeMessage.", var5);
		}

		if (this.configuration.getExpiry() == -1L) {
			return Long.MAX_VALUE;
		} else {
			try {
				Date sentDate = message.getReceivedDate();
				if (sentDate != null) {
					return sentDate.getTime() + this.configuration.getExpiry();
				}
			} catch (MessagingException var4) {
				log.error("Expiration calculation could not be based on message date.", var4);
			}

			return System.currentTimeMillis() + this.configuration.getExpiry();
		}
	}

	public void setExpiry(MimeMessage message, long expiry) {
		try {
			message.setHeader("X-Aspirin-Expiry", this.expiryFormat.format(new Date(System.currentTimeMillis() + expiry)));
		} catch (MessagingException var5) {
			log.error("Could not set Expiry of the MimeMessage: " + this.getMailID(message) + ".", var5);
		}

	}

	public String getMailID(MimeMessage message) {
		try {
			String[] headers = message.getHeader("X-Aspirin-MailID");
			if (headers != null && 0 < headers.length) {
				return headers[0];
			}
		} catch (MessagingException var4) {
			log.error("MailID header could not be get from MimeMessage.", var4);
		}

		return message.toString();
	}
}
