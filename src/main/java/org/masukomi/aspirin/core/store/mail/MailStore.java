//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.masukomi.aspirin.core.store.mail;

import java.util.List;
import jakarta.mail.internet.MimeMessage;

public interface MailStore {
	MimeMessage get(String var1);

	List<String> getMailIds();

	void init();

	void remove(String var1);

	void set(String var1, MimeMessage var2);
}
