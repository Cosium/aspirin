//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.masukomi.aspirin.core.store.queue;

public enum DeliveryState {
	SENT(0),
	FAILED(1),
	QUEUED(2),
	IN_PROGRESS(3);

	private int stateId = 0;

	private DeliveryState(int stateId) {
		this.stateId = stateId;
	}

	public int getStateId() {
		return this.stateId;
	}
}
