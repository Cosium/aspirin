//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.masukomi.aspirin.core.delivery;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import org.apache.commons.pool.ObjectPool;
import org.masukomi.aspirin.core.config.Configuration;
import org.masukomi.aspirin.core.dns.ResolveHost;
import org.masukomi.aspirin.core.store.queue.DeliveryState;
import org.masukomi.aspirin.core.store.queue.QueueInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeliveryThread extends Thread {
	private static final Logger log = LoggerFactory.getLogger(DeliveryThread.class);
	private final DeliveryManager deliveryManager;
	private final Configuration configuration;
	private boolean running = true;
	private ObjectPool parentObjectPool = null;
	private DeliveryContext dCtx = null;

	public DeliveryThread(ThreadGroup parentThreadGroup, DeliveryManager deliveryManager, Configuration configuration) {
		super(parentThreadGroup, DeliveryThread.class.getSimpleName());
		this.deliveryManager = deliveryManager;
		this.configuration = configuration;
	}

	public ObjectPool getParentObjectPool() {
		return this.parentObjectPool;
	}

	public void setParentObjectPool(ObjectPool parentObjectPool) {
		this.parentObjectPool = parentObjectPool;
	}

	public void shutdown() {
		log.debug("DeliveryThread ({}).shutdown(): Called.", this.getName());
		this.running = false;
		synchronized(this) {
			this.notify();
		}
	}

	public void run() {
		while(this.running) {
			synchronized(this) {
				if (this.dCtx == null) {
					try {
						if (this.running) {
							log.trace("DeliveryThread ({}).run(): Wait for next sendable item.", this.getName());
							this.wait(60000L);
							continue;
						}
					} catch (InterruptedException var16) {
						if (this.dCtx != null) {
							log.trace("DeliveryThread ({}).run(): Release item after interruption. qi={}", new Object[]{this.getName(), this.dCtx});
							this.deliveryManager.release(this.dCtx.getQueueInfo());
							this.dCtx = null;
						}

						this.running = false;

						try {
							log.trace("DeliveryThread ({}).run(): Invalidate DeliveryThread object in the pool.", this.getName());
							this.parentObjectPool.invalidateObject(this);
						} catch (Exception var14) {
							throw new RuntimeException("The object could not be invalidated in the pool.", var14);
						}
					}
				}
			}

			try {
				if (this.dCtx != null) {
					log.trace("DeliveryThread ({}).run(): Call delivering... dCtx={}", new Object[]{this.getName(), this.dCtx});
					this.deliver(this.dCtx, this.configuration.newMailSession());
					this.deliveryManager.release(this.dCtx.getQueueInfo());
					this.dCtx = null;
				}
			} catch (Exception var13) {
				log.error("DeliveryThread (" + this.getName() + ").run(): Could not deliver message. dCtx={" + this.dCtx + "}", var13);
			} finally {
				if (this.dCtx != null && !this.dCtx.getQueueInfo().isSendable()) {
					this.deliveryManager.release(this.dCtx.getQueueInfo());
					this.dCtx = null;
				}

			}

			if (this.dCtx == null) {
				try {
					log.trace("DeliveryThread ({}).run(): Try to give back DeliveryThread object into the pool.", this.getName());
					this.parentObjectPool.returnObject(this);
				} catch (Exception var12) {
					log.error("DeliveryThread (" + this.getName() + ").run(): The object could not be returned into the pool.", var12);
					this.shutdown();
				}
			}
		}

	}

	public void setContext(DeliveryContext dCtx) throws MessagingException {
		synchronized(this) {
			if (this.dCtx != null) {
				if (this.dCtx.getQueueInfo().hasState(new DeliveryState[]{DeliveryState.IN_PROGRESS})) {
					this.notify();
				}

				throw new MessagingException("The previous QuedItem was not removed from this thread.");
			} else {
				this.dCtx = dCtx;
				log.trace("DeliveryThread ({}).setQuedItem(): Item was set. qi={}", new Object[]{this.getName(), dCtx});
				this.notify();
			}
		}
	}

	private void deliver(DeliveryContext dCtx, Session session) {
		log.info("DeliveryThread ({}).deliver(): Starting mail delivery. qi={}", new Object[]{this.getName(), dCtx});
		String[] handlerList = new String[]{ResolveHost.class.getCanonicalName(), SendMessage.class.getCanonicalName()};
		QueueInfo qInfo = dCtx.getQueueInfo();
		String[] var5 = handlerList;
		int var6 = handlerList.length;

		for(int var7 = 0; var7 < var6; ++var7) {
			String handlerName = var5[var7];

			try {
				DeliveryHandler handler = this.deliveryManager.getDeliveryHandler(handlerName);
				log.info("deliver using: " + handler.getClass());
				handler.handle(dCtx);
			} catch (DeliveryException var10) {
				qInfo.setResultInfo(var10.getMessage());
				log.info("DeliveryThread ({}).deliver(): Mail delivery failed: {}. qi={}", new Object[]{this.getName(), qInfo.getResultInfo(), dCtx});
				if (var10.isPermanent()) {
					qInfo.setState(DeliveryState.FAILED);
				} else {
					qInfo.setState(DeliveryState.QUEUED);
				}

				return;
			}
		}

		if (qInfo.hasState(new DeliveryState[]{DeliveryState.IN_PROGRESS})) {
			if (qInfo.getResultInfo() == null) {
				qInfo.setResultInfo("250 OK");
			}

			log.info("DeliveryThread ({}).deliver(): Mail delivery success: {}. qi={}", new Object[]{this.getName(), qInfo.getResultInfo(), dCtx});
			qInfo.setState(DeliveryState.SENT);
		}

	}
}
