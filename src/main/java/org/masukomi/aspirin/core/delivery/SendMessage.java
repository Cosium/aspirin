//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.masukomi.aspirin.core.delivery;

import com.sun.mail.smtp.SMTPTransport;
import java.net.ConnectException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.URLName;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.masukomi.aspirin.core.config.Configuration;
import org.masukomi.aspirin.core.store.mail.MailStore;
import org.masukomi.aspirin.core.store.queue.DeliveryState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendMessage implements DeliveryHandler {
	private static final Logger log = LoggerFactory.getLogger(SendMessage.class);
	private final Configuration configuration;
	private final MailStore mailStore;

	public SendMessage(Configuration configuration, MailStore mailStore) {
		this.configuration = configuration;
		this.mailStore = mailStore;
	}

	public void handle(DeliveryContext dCtx) throws DeliveryException {
		Collection<URLName> targetServers = (Collection)dCtx.getContextVariable("targetservers");
		Session session = this.configuration.newMailSession();
		MimeMessage message = this.mailStore.get(dCtx.getQueueInfo().getMailid());
		if (message == null) {
			log.info("Got a null message");
		} else {
			Iterator<URLName> urlnIt = targetServers.iterator();

			InternetAddress[] addr;
			try {
				addr = new InternetAddress[]{new InternetAddress(dCtx.getQueueInfo().getRecipient())};
			} catch (AddressException var24) {
				throw new DeliveryException("Recipient could not be parsed:" + dCtx.getQueueInfo().getRecipient(), true, var24);
			}

			boolean sentSuccessfully = false;

			while(!sentSuccessfully && urlnIt.hasNext()) {
				try {
					URLName outgoingMailServer = (URLName)urlnIt.next();
					Properties props = session.getProperties();
					if (message.getSender() == null) {
						props.put("mail.smtp.from", "<>");
						log.debug("SendMessage.handle(): Attempting delivery of '{}' to recipient '{}' on host '{}' from unknown sender", new Object[]{dCtx.getQueueInfo().getMailid(), dCtx.getQueueInfo().getRecipient(), outgoingMailServer});
					} else {
						String sender = message.getSender().toString();
						props.put("mail.smtp.from", sender);
						log.debug("SendMessage.handle(): Attempting delivery of '{}' to recipient '{}' on host '{}' from sender '{}'", new Object[]{dCtx.getQueueInfo().getMailid(), dCtx.getQueueInfo().getRecipient(), outgoingMailServer, sender});
					}

					long tm = System.currentTimeMillis();
					Transport transport = null;

					try {
						transport = session.getTransport(outgoingMailServer);

						try {
							transport.connect();
							Address[] addresses = new Address[addr.length];
							int i = 0;
							InternetAddress[] var15 = addr;
							int var16 = addr.length;
							int var17 = 0;

							while(true) {
								if (var17 >= var16) {
									Date now = new Date();
									message.setSentDate(now);
									if (message.getRecipients(RecipientType.TO) == null) {
										message.setRecipients(RecipientType.TO, addresses);
									}

									long nowMillis = System.currentTimeMillis();
									transport.sendMessage(message, addr);
									if (transport instanceof SMTPTransport) {
										String response = ((SMTPTransport)transport).getLastServerResponse();
										if (response != null) {
											log.info("SendMessage.handle(): Last server response: {}.", response);
											dCtx.getQueueInfo().setResultInfo(response);
										} else {
											dCtx.getQueueInfo().setResultInfo("No server response after " + (System.currentTimeMillis() - nowMillis) + "ms connecting to " + outgoingMailServer);
										}
									} else {
										dCtx.getQueueInfo().setResultInfo("Unknown transport: " + transport);
									}
									break;
								}

								InternetAddress add = var15[var17];
								log.info("sendMessage to: " + add.getAddress());
								addresses[i++] = add;
								++var17;
							}
						} catch (MessagingException var25) {
							if (this.resolveException(var25) instanceof ConnectException) {
								log.warn("SendMessage.handle(): Connection failed.", var25);
								if (!urlnIt.hasNext()) {
									throw var25;
								}
								continue;
							}

							log.error("Exception sending message with messageId: " + message.getMessageID(), var25);
							throw var25;
						}

						tm = System.currentTimeMillis() - tm;
						log.info("SendMessage.handle(): Mail '{}' sent successfully to '{}' duration={}ms", new Object[]{dCtx.getQueueInfo().getMailid(), outgoingMailServer, tm});
						sentSuccessfully = true;
						dCtx.addContextVariable("newstate", DeliveryState.SENT);
					} finally {
						if (transport != null) {
							transport.close();
							transport = null;
						}

					}
				} catch (MessagingException var27) {
					String exMessage = this.resolveException(var27).getMessage();
					log.warn("SendMessage: messaging exception: " + exMessage);
					if ('5' == exMessage.charAt(0)) {
						throw new DeliveryException(exMessage, true);
					}

					throw new DeliveryException(exMessage, false);
				}
			}

			if (!sentSuccessfully) {
				throw new DeliveryException("SendMessage.handle(): Mail '{}' sending failed, try later.", false);
			}
		}
	}

	private Exception resolveException(MessagingException msgExc) {
		MessagingException me = msgExc;
		Exception nextException = null;

		Object lastException;
		for(lastException = msgExc; (nextException = me.getNextException()) != null; me = (MessagingException)nextException) {
			lastException = nextException;
			if (!MessagingException.class.getCanonicalName().equals(nextException.getClass().getCanonicalName())) {
				break;
			}
		}

		return (Exception)lastException;
	}
}
