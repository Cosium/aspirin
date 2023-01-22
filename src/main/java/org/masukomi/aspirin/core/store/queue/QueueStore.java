//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.masukomi.aspirin.core.store.queue;

import java.util.Collection;
import java.util.List;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;

public interface QueueStore {
	void add(String var1, long var2, Collection<InternetAddress> var4) throws MessagingException;

	List<String> clean();

	QueueInfo createQueueInfo();

	long getNextAttempt(String var1, String var2);

	boolean hasBeenRecipientHandled(String var1, String var2);

	void init();

	boolean isCompleted(String var1);

	QueueInfo next();

	void remove(String var1);

	void removeRecipient(String var1);

	void setSendingResult(QueueInfo var1);

	int size();
}
