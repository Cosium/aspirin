//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.masukomi.aspirin.core.store.queue;

import org.masukomi.aspirin.core.config.Configuration;
import org.masukomi.aspirin.core.listener.ListenerManager;

public class QueueInfo {
	private final Configuration configuration;
	private final ListenerManager listenerManager;
	private String mailid;
	private String recipient;
	private String resultInfo;
	private long attempt = 0L;
	private int attemptCount = 0;
	private long expiry = -1L;
	private DeliveryState state;
	private transient boolean notifiedAlready;
	private transient String complexId;
	private transient String qiToString;

	public QueueInfo(Configuration configuration, ListenerManager listenerManager) {
		this.state = DeliveryState.QUEUED;
		this.notifiedAlready = false;
		this.complexId = null;
		this.qiToString = null;
		this.configuration = configuration;
		this.listenerManager = listenerManager;
	}

	public String getComplexId() {
		if (this.complexId == null) {
			this.complexId = this.mailid + "-" + this.recipient;
		}

		return this.complexId;
	}

	public String getMailid() {
		return this.mailid;
	}

	public void setMailid(String mailid) {
		this.mailid = mailid;
	}

	public String getRecipient() {
		return this.recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	public String getResultInfo() {
		return this.resultInfo;
	}

	public void setResultInfo(String resultInfo) {
		this.resultInfo = resultInfo;
	}

	public long getAttempt() {
		return this.attempt;
	}

	public void setAttempt(long attempt) {
		this.attempt = attempt;
	}

	public int getAttemptCount() {
		return this.attemptCount;
	}

	public void incAttemptCount() {
		++this.attemptCount;
	}

	public void setAttemptCount(int attemptCount) {
		this.attemptCount = attemptCount;
	}

	public long getExpiry() {
		return this.expiry;
	}

	public void setExpiry(long expiry) {
		this.expiry = expiry;
	}

	public DeliveryState getState() {
		return this.state;
	}

	public void setState(DeliveryState state) {
		this.state = state;
		if (this.listenerManager != null && !this.notifiedAlready && !this.hasState(DeliveryState.QUEUED, DeliveryState.IN_PROGRESS)) {
			this.listenerManager.notifyListeners(this);
			this.notifiedAlready = true;
		}

	}

	public boolean hasState(DeliveryState... states) {
		DeliveryState[] var2 = states;
		int var3 = states.length;

		for(int var4 = 0; var4 < var3; ++var4) {
			DeliveryState st = var2[var4];
			if (st.equals(this.state)) {
				return true;
			}
		}

		return false;
	}

	public boolean isSendable() {
		return this.hasState(DeliveryState.QUEUED) && this.getAttempt() < System.currentTimeMillis();
	}

	public boolean isInTimeBounds() {
		return (this.getExpiry() == -1L || System.currentTimeMillis() < this.getExpiry()) && this.getAttemptCount() < this.configuration.getDeliveryAttemptCount();
	}

	public String toString() {
		if (this.qiToString == null) {
			StringBuilder sb = new StringBuilder();
			sb.append("Mail: [id=").append(this.mailid).append("; recipient=").append(this.recipient).append("];");
			this.qiToString = sb.toString();
		}

		return this.qiToString;
	}
}
