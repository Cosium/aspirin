//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.masukomi.aspirin.core.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.masukomi.aspirin.core.delivery.DeliveryManager;
import org.masukomi.aspirin.core.store.queue.DeliveryState;
import org.masukomi.aspirin.core.store.queue.QueueInfo;

public class ListenerManager {
	private final List<AspirinListener> listenerList = new ArrayList();
	private DeliveryManager deliveryManager;

	public ListenerManager() {
	}

	public void setDeliveryManager(DeliveryManager deliveryManager) {
		this.deliveryManager = deliveryManager;
	}

	public DeliveryManager getDeliveryManager() {
		return this.deliveryManager;
	}

	public void add(AspirinListener listener) {
		synchronized(this.listenerList) {
			this.listenerList.add(listener);
		}
	}

	public void remove(AspirinListener listener) {
		synchronized(this.listenerList) {
			this.listenerList.remove(listener);
		}
	}

	public void notifyListeners(QueueInfo qi) {
		List<AspirinListener> listeners = null;
		synchronized(this.listenerList) {
			listeners = Collections.unmodifiableList(this.listenerList);
		}

		if (listeners != null && !listeners.isEmpty()) {
			Iterator var3 = listeners.iterator();

			while(var3.hasNext()) {
				AspirinListener listener = (AspirinListener)var3.next();
				if (qi.hasState(new DeliveryState[]{DeliveryState.FAILED})) {
					listener.delivered(qi.getMailid(), qi.getRecipient(), ResultState.FAILED, qi.getResultInfo());
				} else if (qi.hasState(new DeliveryState[]{DeliveryState.SENT})) {
					listener.delivered(qi.getMailid(), qi.getRecipient(), ResultState.SENT, qi.getResultInfo());
				}

				if (this.deliveryManager.isCompleted(qi)) {
					listener.delivered(qi.getMailid(), qi.getRecipient(), ResultState.FINISHED, qi.getResultInfo());
				}
			}
		}

	}
}
