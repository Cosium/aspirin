//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.masukomi.aspirin.core.delivery;

import java.lang.Thread.State;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;
import org.masukomi.aspirin.core.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericPoolableDeliveryThreadFactory extends BasePoolableObjectFactory {
	private static final Logger log = LoggerFactory.getLogger(GenericPoolableDeliveryThreadFactory.class);
	private ThreadGroup deliveryThreadGroup = null;
	private ObjectPool myParentPool = null;
	private Integer rdCount = 0;
	private Object rdLock = new Object();
	private final DeliveryManager deliveryManager;
	private final Configuration configuration;

	public GenericPoolableDeliveryThreadFactory(DeliveryManager deliveryManager, Configuration configuration) {
		this.deliveryManager = deliveryManager;
		this.configuration = configuration;
	}

	public void init(ThreadGroup deliveryThreadGroup, ObjectPool pool) {
		this.deliveryThreadGroup = deliveryThreadGroup;
		this.myParentPool = pool;
	}

	public Object makeObject() throws Exception {
		if (this.myParentPool == null) {
			throw new RuntimeException("Please set the parent pool for right working.");
		} else {
			DeliveryThread dThread = new DeliveryThread(this.deliveryThreadGroup, this.deliveryManager, this.configuration);
			synchronized(this.rdLock) {
				Integer var3 = this.rdCount;
				Integer var4 = this.rdCount = this.rdCount + 1;
				dThread.setName(DeliveryThread.class.getSimpleName() + "-" + this.rdCount);
			}

			dThread.setParentObjectPool(this.myParentPool);
			log.trace("GenericPoolableDeliveryThreadFactory.makeObject(): New DeliveryThread object created: {}.", dThread.getName());
			return dThread;
		}
	}

	public void destroyObject(Object obj) throws Exception {
		if (obj instanceof DeliveryThread) {
			DeliveryThread dThread = (DeliveryThread)obj;
			log.trace(this.getClass().getSimpleName() + ".destroyObject(): destroy thread {}.", dThread.getName());
			dThread.shutdown();
		}

	}

	public boolean validateObject(Object obj) {
		if (!(obj instanceof DeliveryThread)) {
			return false;
		} else {
			DeliveryThread dThread = (DeliveryThread)obj;
			return dThread.isAlive() && (dThread.getState().equals(State.NEW) || dThread.getState().equals(State.RUNNABLE) || dThread.getState().equals(State.TIMED_WAITING) || dThread.getState().equals(State.WAITING));
		}
	}
}
