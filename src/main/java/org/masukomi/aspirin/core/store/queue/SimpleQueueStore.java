//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.masukomi.aspirin.core.store.queue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import org.masukomi.aspirin.core.config.Configuration;
import org.masukomi.aspirin.core.listener.ListenerManager;

public class SimpleQueueStore implements QueueStore {
	private final Configuration configuration;
	private final ListenerManager listenerManager;
	private List<QueueInfo> queueInfoList = new LinkedList();
	private Map<String, QueueInfo> queueInfoByMailidAndRecipient = new HashMap();
	private Map<String, List<QueueInfo>> queueInfoByMailid = new HashMap();
	private Map<String, List<QueueInfo>> queueInfoByRecipient = new HashMap();
	private Object lock = new Object();
	private Comparator<QueueInfo> queueInfoComparator = new Comparator<QueueInfo>() {
		public int compare(QueueInfo o1, QueueInfo o2) {
			return (int)(o2.getAttempt() - o1.getAttempt());
		}
	};

	public SimpleQueueStore(Configuration configuration, ListenerManager listenerManager) {
		this.configuration = configuration;
		this.listenerManager = listenerManager;
	}

	public void add(String mailid, long expiry, Collection<InternetAddress> recipients) throws MessagingException {
		try {
			Iterator var5 = recipients.iterator();

			while(var5.hasNext()) {
				InternetAddress recipient = (InternetAddress)var5.next();
				QueueInfo queueInfo = new QueueInfo(this.configuration, this.listenerManager);
				queueInfo.setExpiry(expiry);
				queueInfo.setMailid(mailid);
				queueInfo.setRecipient(recipient.getAddress());
				synchronized(this.lock) {
					this.queueInfoList.add(queueInfo);
					this.queueInfoByMailidAndRecipient.put(this.createSearchKey(queueInfo.getMailid(), queueInfo.getRecipient()), queueInfo);
					if (!this.queueInfoByMailid.containsKey(queueInfo.getMailid())) {
						this.queueInfoByMailid.put(queueInfo.getMailid(), new ArrayList());
					}

					((List)this.queueInfoByMailid.get(queueInfo.getMailid())).add(queueInfo);
					if (!this.queueInfoByRecipient.containsKey(queueInfo.getRecipient())) {
						this.queueInfoByRecipient.put(queueInfo.getRecipient(), new ArrayList());
					}

					((List)this.queueInfoByRecipient.get(queueInfo.getRecipient())).add(queueInfo);
				}
			}

		} catch (Exception var11) {
			throw new MessagingException("Message queueing failed: " + mailid, var11);
		}
	}

	public List<String> clean() {
		List<String> mailidList = null;
		synchronized(this.lock) {
			mailidList = new ArrayList(this.queueInfoByMailid.keySet());
		}

		Iterator<String> mailidIt = mailidList.iterator();

		while(mailidIt.hasNext()) {
			String mailid = (String)mailidIt.next();
			if (this.isCompleted(mailid)) {
				this.remove(mailid);
				mailidIt.remove();
			}
		}

		return mailidList;
	}

	public QueueInfo createQueueInfo() {
		return new QueueInfo(this.configuration, this.listenerManager);
	}

	public long getNextAttempt(String mailid, String recipient) {
		QueueInfo qInfo = (QueueInfo)this.queueInfoByMailidAndRecipient.get(this.createSearchKey(mailid, recipient));
		return qInfo != null && qInfo.hasState(new DeliveryState[]{DeliveryState.QUEUED}) ? qInfo.getAttempt() : -1L;
	}

	public boolean hasBeenRecipientHandled(String mailid, String recipient) {
		QueueInfo qInfo = (QueueInfo)this.queueInfoByMailidAndRecipient.get(this.createSearchKey(mailid, recipient));
		return qInfo != null && qInfo.hasState(new DeliveryState[]{DeliveryState.FAILED, DeliveryState.SENT});
	}

	public void init() {
	}

	public boolean isCompleted(String mailid) {
		List<QueueInfo> qibmList = (List)this.queueInfoByMailid.get(mailid);
		if (qibmList != null) {
			Iterator var3 = qibmList.iterator();

			while(var3.hasNext()) {
				QueueInfo sqi = (QueueInfo)var3.next();
				if (sqi.hasState(new DeliveryState[]{DeliveryState.IN_PROGRESS, DeliveryState.QUEUED})) {
					return false;
				}
			}
		}

		return true;
	}

	public QueueInfo next() {
		Collections.sort(this.queueInfoList, this.queueInfoComparator);
		if (!this.queueInfoList.isEmpty()) {
			synchronized(this.lock) {
				ListIterator<QueueInfo> queueInfoIt = this.queueInfoList.listIterator();

				while(true) {
					QueueInfo qi;
					do {
						if (!queueInfoIt.hasNext()) {
							return null;
						}

						qi = (QueueInfo)queueInfoIt.next();
					} while(!qi.isSendable());

					if (qi.isInTimeBounds()) {
						qi.setState(DeliveryState.IN_PROGRESS);
						return qi;
					}

					if (qi.getResultInfo() == null || qi.getResultInfo().isEmpty()) {
						qi.setResultInfo("Delivery is out of time or attempt.");
					}

					qi.setState(DeliveryState.FAILED);
					this.setSendingResult(qi);
				}
			}
		} else {
			return null;
		}
	}

	public void remove(String mailid) {
		synchronized(this.lock) {
			List<QueueInfo> removeableQueueInfos = (List)this.queueInfoByMailid.remove(mailid);
			if (removeableQueueInfos != null) {
				Iterator var4 = removeableQueueInfos.iterator();

				while(var4.hasNext()) {
					QueueInfo sqi = (QueueInfo)var4.next();
					this.queueInfoByMailidAndRecipient.remove(this.createSearchKey(sqi.getMailid(), sqi.getRecipient()));
					((List)this.queueInfoByRecipient.get(sqi.getRecipient())).remove(sqi);
				}
			}

		}
	}

	public void removeRecipient(String recipient) {
		synchronized(this.lock) {
			List<QueueInfo> removeableQueueInfos = (List)this.queueInfoByRecipient.remove(recipient);
			if (removeableQueueInfos != null) {
				Iterator var4 = removeableQueueInfos.iterator();

				while(var4.hasNext()) {
					QueueInfo sqi = (QueueInfo)var4.next();
					this.queueInfoByMailidAndRecipient.remove(this.createSearchKey(sqi.getMailid(), sqi.getRecipient()));
					((List)this.queueInfoByMailid.get(sqi.getMailid())).remove(sqi);
				}
			}

		}
	}

	public void setSendingResult(QueueInfo qi) {
		synchronized(this.lock) {
			QueueInfo uniqueQueueInfo = (QueueInfo)this.queueInfoByMailidAndRecipient.get(this.createSearchKey(qi.getMailid(), qi.getRecipient()));
			if (uniqueQueueInfo != null) {
				uniqueQueueInfo.setAttempt(System.currentTimeMillis() + (long)this.configuration.getDeliveryAttemptDelay());
				uniqueQueueInfo.incAttemptCount();
				uniqueQueueInfo.setState(qi.getState());
			}

		}
	}

	public int size() {
		return this.queueInfoByMailid.size();
	}

	private String createSearchKey(String mailid, String recipient) {
		return mailid + "-" + recipient;
	}
}
