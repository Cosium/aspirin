//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.masukomi.aspirin.core.delivery;

import java.util.HashMap;
import java.util.Map;
import jakarta.mail.Session;
import org.masukomi.aspirin.core.store.queue.QueueInfo;

public class DeliveryContext {
	private QueueInfo queueInfo;
	private Session mailSession;
	private Map<String, Object> contextVariables = new HashMap();
	private transient String ctxToString;

	public DeliveryContext() {
	}

	public QueueInfo getQueueInfo() {
		return this.queueInfo;
	}

	public DeliveryContext setQueueInfo(QueueInfo queueInfo) {
		this.queueInfo = queueInfo;
		return this;
	}

	public Session getMailSession() {
		return this.mailSession;
	}

	public DeliveryContext setMailSession(Session mailSession) {
		this.mailSession = mailSession;
		return this;
	}

	public Map<String, Object> getContextVariables() {
		return this.contextVariables;
	}

	public void addContextVariable(String name, Object variable) {
		this.contextVariables.put(name, variable);
	}

	public <T> T getContextVariable(String name) {
		return this.contextVariables.containsKey(name) ? (T) this.contextVariables.get(name) : null;
	}

	public String toString() {
		if (this.ctxToString == null) {
			StringBuilder sb = new StringBuilder();
			sb.append(this.getClass().getSimpleName()).append(" [");
			sb.append("qi=").append(this.queueInfo);
			sb.append("]; ");
			this.ctxToString = sb.toString();
		}

		return this.ctxToString;
	}
}
