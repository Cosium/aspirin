//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.masukomi.aspirin.core.delivery;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.masukomi.aspirin.core.AspirinInternal;
import org.masukomi.aspirin.core.Helper;
import org.masukomi.aspirin.core.config.Configuration;
import org.masukomi.aspirin.core.config.ConfigurationChangeListener;
import org.masukomi.aspirin.core.dns.ResolveHost;
import org.masukomi.aspirin.core.store.mail.MailStore;
import org.masukomi.aspirin.core.store.queue.DeliveryState;
import org.masukomi.aspirin.core.store.queue.QueueInfo;
import org.masukomi.aspirin.core.store.queue.QueueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DeliveryManager extends Thread implements ConfigurationChangeListener {
	private static final Logger log = LoggerFactory.getLogger(DeliveryManager.class);
	private final Configuration configuration;
	private final MailStore mailStore;
	private final QueueStore queueStore;
	private Object mailingLock = new Object();
	private ObjectPool deliveryThreadObjectPool = null;
	private boolean running = false;
	private GenericPoolableDeliveryThreadFactory deliveryThreadObjectFactory = null;
	private Map<String, DeliveryHandler> deliveryHandlers = new HashMap();
	private final Helper helper;

	public DeliveryManager(Configuration configuration, QueueStore queueStore, MailStore mailStore) {
		this.configuration = configuration;
		this.helper = new Helper(configuration);
		this.mailStore = mailStore;
		this.setName("Aspirin-" + this.getClass().getSimpleName() + "-" + this.getId());
		GenericObjectPool.Config gopConf = new GenericObjectPool.Config();
		gopConf.lifo = false;
		gopConf.maxActive = configuration.getDeliveryThreadsActiveMax();
		gopConf.maxIdle = configuration.getDeliveryThreadsIdleMax();
		gopConf.maxWait = 5000L;
		gopConf.testOnReturn = true;
		gopConf.whenExhaustedAction = 1;
		this.deliveryThreadObjectFactory = new GenericPoolableDeliveryThreadFactory(this, configuration);
		this.deliveryThreadObjectPool = new GenericObjectPool(this.deliveryThreadObjectFactory, gopConf);
		this.deliveryThreadObjectFactory.init(new ThreadGroup("DeliveryThreadGroup"), this.deliveryThreadObjectPool);
		this.queueStore = queueStore;
		queueStore.init();
		this.deliveryHandlers.put(SendMessage.class.getCanonicalName(), new SendMessage(configuration, mailStore));
		this.deliveryHandlers.put(ResolveHost.class.getCanonicalName(), new ResolveHost());
		configuration.addListener(this);
	}

	public String add(MimeMessage mimeMessage) throws MessagingException {
		String mailid = this.helper.getMailID(mimeMessage);
		long expiry = this.helper.getExpiry(mimeMessage);
		Collection<InternetAddress> recipients = AspirinInternal.extractRecipients(mimeMessage);
		synchronized(this.mailingLock) {
			this.mailStore.set(mailid, mimeMessage);
			this.queueStore.add(mailid, expiry, recipients);
			return mailid;
		}
	}

	public MimeMessage get(QueueInfo qi) {
		if (qi == null) {
			throw new RuntimeException("queue info object is null");
		} else {
			return this.mailStore.get(qi.getMailid());
		}
	}

	public void remove(String messageName) {
		synchronized(this.mailingLock) {
			this.mailStore.remove(messageName);
			this.queueStore.remove(messageName);
		}
	}

	public void run() {
		this.running = true;
		log.info("DeliveryManager started.");

		while(this.running) {
			QueueInfo qi = null;

			try {
				qi = this.queueStore.next();
				if (qi != null) {
					DeliveryContext dCtx = (new DeliveryContext()).setQueueInfo(qi);
					log.trace("DeliveryManager.run(): Pool state. A{}/I{}", new Object[]{this.deliveryThreadObjectPool.getNumActive(), this.deliveryThreadObjectPool.getNumIdle()});

					try {
						log.debug("DeliveryManager.run(): Start delivery. qi={}", qi);
						DeliveryThread dThread = (DeliveryThread)this.deliveryThreadObjectPool.borrowObject();
						log.trace("DeliveryManager.run(): Borrow DeliveryThread object. dt={}: state '{}/{}'", new Object[]{dThread.getName(), dThread.getState().name(), dThread.isAlive()});
						dThread.setContext(dCtx);
						if (!dThread.isAlive()) {
							dThread.start();
						}
					} catch (IllegalStateException var7) {
						this.release(qi);
					} catch (NoSuchElementException var8) {
						log.debug("DeliveryManager.run(): No idle DeliveryThread is available: {}", var8.getMessage());
						qi.setResultInfo("No delivery available, will try again");
						this.release(qi);
					} catch (Exception var9) {
						log.error("DeliveryManager.run(): Failed borrow delivery thread object.", var9);
						this.release(qi);
					}
				} else {
					synchronized(this) {
						try {
							this.wait(6000L);
						} catch (InterruptedException var5) {
							this.running = false;
						}
					}
				}
			} catch (Throwable var10) {
				log.error("Exception polling for messages", var10);
				System.out.println("----");
				if (qi != null) {
					this.release(qi);
				}
			}
		}

		log.info("DeliveryManager terminated.");
	}

	public boolean isRunning() {
		return this.running;
	}

	public void terminate() {
		this.running = false;
	}

	public void release(QueueInfo qi) {
		if (qi.hasState(new DeliveryState[]{DeliveryState.IN_PROGRESS})) {
			if (qi.isInTimeBounds()) {
				qi.setState(DeliveryState.QUEUED);
				log.trace("DeliveryManager.release(): Releasing: QUEUED. qi={}", qi);
			} else {
				qi.setState(DeliveryState.FAILED);
				log.trace("DeliveryManager.release(): Releasing: FAILED. qi={}", qi);
			}
		}

		this.queueStore.setSendingResult(qi);
		if (this.queueStore.isCompleted(qi.getMailid())) {
			this.queueStore.remove(qi.getMailid());
		}

		log.trace("DeliveryManager.release(): Release item '{}' with state: '{}' after {} attempts.", new Object[]{qi.getMailid(), qi.getState().name(), qi.getAttemptCount()});
	}

	public boolean isCompleted(QueueInfo qi) {
		return this.queueStore.isCompleted(qi.getMailid());
	}

	public void configChanged(String parameterName) {
	}

	public DeliveryHandler getDeliveryHandler(String handlerName) {
		return (DeliveryHandler)this.deliveryHandlers.get(handlerName);
	}

	public void shutdown() {
		this.running = false;

		try {
			this.deliveryThreadObjectPool.close();
			this.deliveryThreadObjectPool.clear();
		} catch (Exception var2) {
			log.error("DeliveryManager.shutdown() failed.", var2);
		}

	}
}
