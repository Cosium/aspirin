//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.masukomi.aspirin.core.store.mail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import jakarta.mail.internet.MimeMessage;

public class SimpleMailStore implements MailStore {
	private HashMap<String, MimeMessage> messageMap = new HashMap();

	public SimpleMailStore() {
	}

	public MimeMessage get(String mailid) {
		return (MimeMessage)this.messageMap.get(mailid);
	}

	public List<String> getMailIds() {
		return new ArrayList(this.messageMap.keySet());
	}

	public void init() {
	}

	public void remove(String mailid) {
		this.messageMap.remove(mailid);
	}

	public void set(String mailid, MimeMessage msg) {
		this.messageMap.put(mailid, msg);
	}
}
