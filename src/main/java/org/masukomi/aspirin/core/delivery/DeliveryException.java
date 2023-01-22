//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.masukomi.aspirin.core.delivery;

import jakarta.mail.MessagingException;

public class DeliveryException extends MessagingException {
	private static final long serialVersionUID = -5388667812025531029L;
	private boolean permanent = true;

	public boolean isPermanent() {
		return this.permanent;
	}

	public DeliveryException() {
	}

	public DeliveryException(String s, boolean permanent) {
		super(s);
		this.permanent = permanent;
	}

	public DeliveryException(String s, boolean permanent, Exception e) {
		super(s, e);
		this.permanent = permanent;
	}
}
