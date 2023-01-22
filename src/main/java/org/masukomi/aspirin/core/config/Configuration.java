//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.masukomi.aspirin.core.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.ParseException;
import org.masukomi.aspirin.core.store.mail.MailStore;
import org.masukomi.aspirin.core.store.mail.SimpleMailStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration implements ConfigurationMBean {
	private static final Logger log = LoggerFactory.getLogger(Configuration.class);
	private Map<String, Object> configParameters = new HashMap();
	private MailStore mailStore = null;
	protected InternetAddress postmaster = null;
	private Session mailSession = null;
	private List<ConfigurationChangeListener> listeners;
	private Object listenerLock = new Object();
	private static final String MAIL_MIME_CHARSET = "mail.mime.charset";
	private static final String MAIL_SMTP_CONNECTIONTIMEOUT = "mail.smtp.connectiontimeout";
	private static final String MAIL_SMTP_HOST = "mail.smtp.host";
	private static final String MAIL_SMTP_LOCALHOST = "mail.smtp.localhost";
	private static final String MAIL_SMTP_TIMEOUT = "mail.smtp.timeout";

	public void init(Properties props) {
		List<Parameter> parameterList = new ArrayList();
		parameterList.add(new Parameter("aspirin.delivery.attempt.count", 3, 1));
		parameterList.add(new Parameter("aspirin.delivery.attempt.delay", 300000, 1));
		parameterList.add(new Parameter("aspirin.delivery.bounce-on-failure", true, 3));
		parameterList.add(new Parameter("aspirin.delivery.debug", false, 3));
		parameterList.add(new Parameter("aspirin.delivery.expiry", -1L, 2));
		parameterList.add(new Parameter("aspirin.delivery.threads.active.max", 3, 1));
		parameterList.add(new Parameter("aspirin.delivery.threads.idle.max", 3, 1));
		parameterList.add(new Parameter("aspirin.delivery.timeout", 30000, 1));
		parameterList.add(new Parameter("aspirin.encoding", "UTF-8", 0));
		parameterList.add(new Parameter("aspirin.hostname", "localhost", 0));
		parameterList.add(new Parameter("aspirin.logger.name", "Aspirin", 0));
		parameterList.add(new Parameter("aspirin.logger.prefix", "Aspirin ", 0));
		parameterList.add(new Parameter("aspirin.mailstore.class", SimpleMailStore.class.getCanonicalName(), 0));
		parameterList.add(new Parameter("aspirin.postmaster.email", (Object)null, 0));
		Iterator var3 = parameterList.iterator();

		while(var3.hasNext()) {
			Parameter param = (Parameter)var3.next();
			Object o = param.extractValue(props);
			if (o != null) {
				this.configParameters.put(param.getName(), o);
			}
		}

		this.setPostmasterEmail((String)this.configParameters.get("aspirin.postmaster.email"));
		this.updateMailSession();
	}

	public Configuration() {
		this.init(new Properties());
	}

	public Configuration(Properties props) {
		this.init(props);
	}

	public InternetAddress getPostmaster() {
		return this.postmaster;
	}

	public String getHostname() {
		return (String)this.configParameters.get("aspirin.hostname");
	}

	public void setHostname(String hostname) {
		this.configParameters.put("aspirin.hostname", hostname);
		this.updateMailSession();
		this.notifyListeners("aspirin.hostname");
	}

	public String getEncoding() {
		return (String)this.configParameters.get("aspirin.encoding");
	}

	public void setEncoding(String encoding) {
		this.configParameters.put("aspirin.encoding", encoding);
		this.updateMailSession();
		this.notifyListeners("aspirin.encoding");
	}

	public int getDeliveryAttemptCount() {
		return (Integer)this.configParameters.get("aspirin.delivery.attempt.count");
	}

	public int getDeliveryAttemptDelay() {
		return (Integer)this.configParameters.get("aspirin.delivery.attempt.delay");
	}

	public int getDeliveryThreadsActiveMax() {
		return (Integer)this.configParameters.get("aspirin.delivery.threads.active.max");
	}

	public int getDeliveryThreadsIdleMax() {
		return (Integer)this.configParameters.get("aspirin.delivery.threads.idle.max");
	}

	public int getDeliveryTimeout() {
		return (Integer)this.configParameters.get("aspirin.delivery.timeout");
	}

	public long getExpiry() {
		return (Long)this.configParameters.get("aspirin.delivery.expiry");
	}

	public String getLoggerName() {
		return (String)this.configParameters.get("aspirin.logger.name");
	}

	public String getLoggerPrefix() {
		return (String)this.configParameters.get("aspirin.logger.prefix");
	}

	public MailStore getMailStore() {
		if (this.mailStore == null) {
			String mailStoreClassName = (String)this.configParameters.get("aspirin.mailstore.class");

			try {
				Class<?> storeClass = Class.forName(mailStoreClassName);
				if (storeClass.getInterfaces()[0].equals(MailStore.class)) {
					this.mailStore = (MailStore)storeClass.newInstance();
				}
			} catch (Exception var3) {
				log.error(this.getClass().getSimpleName() + " Mail store class could not be instantiated. Class=" + mailStoreClassName, var3);
				this.mailStore = new SimpleMailStore();
			}
		}

		return this.mailStore;
	}

	public String getPostmasterEmail() {
		return this.postmaster.toString();
	}

	public boolean isDeliveryBounceOnFailure() {
		return (Boolean)this.configParameters.get("aspirin.delivery.bounce-on-failure");
	}

	public boolean isDeliveryDebug() {
		return (Boolean)this.configParameters.get("aspirin.delivery.debug");
	}

	public void setDeliveryAttemptCount(int attemptCount) {
		this.configParameters.put("aspirin.delivery.attempt.count", attemptCount);
		this.notifyListeners("aspirin.delivery.attempt.count");
	}

	public void setDeliveryAttemptDelay(int delay) {
		this.configParameters.put("aspirin.delivery.attempt.delay", delay);
		this.notifyListeners("aspirin.delivery.attempt.delay");
	}

	public void setDeliveryBounceOnFailure(boolean bounce) {
		this.configParameters.put("aspirin.delivery.bounce-on-failure", bounce);
		this.notifyListeners("aspirin.delivery.bounce-on-failure");
	}

	public void setDeliveryDebug(boolean debug) {
		this.configParameters.put("aspirin.delivery.debug", debug);
		this.updateMailSession();
		this.notifyListeners("aspirin.delivery.debug");
	}

	public void setDeliveryThreadsActiveMax(int activeThreadsMax) {
		this.configParameters.put("aspirin.delivery.threads.active.max", activeThreadsMax);
		this.notifyListeners("aspirin.delivery.threads.active.max");
	}

	public void setDeliveryThreadsIdleMax(int idleThreadsMax) {
		this.configParameters.put("aspirin.delivery.threads.idle.max", idleThreadsMax);
		this.notifyListeners("aspirin.delivery.threads.idle.max");
	}

	public void setDeliveryTimeout(int timeout) {
		this.configParameters.put("aspirin.delivery.timeout", timeout);
		this.updateMailSession();
		this.notifyListeners("aspirin.delivery.timeout");
	}

	public void setExpiry(long expiry) {
		this.configParameters.put("aspirin.delivery.expiry", expiry);
		this.notifyListeners("aspirin.delivery.expiry");
	}

	public void setMailStore(MailStore mailStore) {
		this.mailStore = mailStore;
		this.notifyListeners("aspirin.mailstore.class");
	}

	public void setPostmasterEmail(String emailAddress) {
		if (emailAddress == null) {
			this.postmaster = null;
		} else {
			try {
				this.postmaster = new InternetAddress(emailAddress);
				this.notifyListeners("aspirin.postmaster.email");
			} catch (ParseException var3) {
				log.error(this.getClass().getSimpleName() + ".setPostmasterEmail(): The email address is unparseable.", var3);
			}

		}
	}

	public void addListener(ConfigurationChangeListener listener) {
		if (this.listeners == null) {
			this.listeners = new ArrayList();
		}

		synchronized(this.listenerLock) {
			this.listeners.add(listener);
		}
	}

	public void removeListener(ConfigurationChangeListener listener) {
		if (this.listeners != null) {
			synchronized(this.listenerLock) {
				this.listeners.remove(listener);
			}
		}

	}

	private void notifyListeners(String changedParameterName) {
		if (this.listeners != null && 0 < this.listeners.size()) {
			if (log.isInfoEnabled()) {
				log.info(this.getClass().getSimpleName() + ".notifyListeners(): Configuration parameter '" + changedParameterName + "' changed.");
			}

			synchronized(this.listenerLock) {
				Iterator var3 = this.listeners.iterator();

				while(var3.hasNext()) {
					ConfigurationChangeListener listener = (ConfigurationChangeListener)var3.next();
					listener.configChanged(changedParameterName);
				}
			}
		}

	}

	public String getMailStoreClassName() {
		return (String)this.configParameters.get("aspirin.mailstore.class");
	}

	public void setMailStoreClassName(String className) {
		this.configParameters.put("aspirin.mailstore.class", className);
		this.mailStore = null;
		this.notifyListeners("aspirin.mailstore.class");
	}

	public Session newMailSession() {
		Properties props = this.mailSession.getProperties();
		props = new Properties(props);
		return Session.getInstance(props);
	}

	public Object getProperty(String name) {
		return this.configParameters.get(name);
	}

	public void setProperty(String name, Object value) {
		this.configParameters.put(name, value);
	}

	private void updateMailSession() {
		Properties mailSessionProps = System.getProperties();
		mailSessionProps.put("mail.smtp.host", this.getHostname());
		mailSessionProps.put("mail.smtp.localhost", this.getHostname());
		mailSessionProps.put("mail.mime.charset", this.getEncoding());
		mailSessionProps.put("mail.smtp.connectiontimeout", this.getDeliveryTimeout());
		mailSessionProps.put("mail.smtp.timeout", this.getDeliveryTimeout());
		mailSessionProps.put("mail.smtp.starttls.enable", "true");
		mailSessionProps.put("mail.smtp.ssl.checkserveridentity", "false");
		mailSessionProps.put("mail.smtp.ssl.trust", "*");
		Session newSession = Session.getInstance(mailSessionProps);
		if (log.isTraceEnabled()) {
			newSession.setDebug(true);
		}

		this.mailSession = newSession;
	}

	private class Parameter {
		public static final int TYPE_STRING = 0;
		public static final int TYPE_INTEGER = 1;
		public static final int TYPE_LONG = 2;
		public static final int TYPE_BOOLEAN = 3;
		private String name;
		private int type;
		private Object defaultValue;

		public Parameter(String name, Object defaultValue, int type) {
			this.name = name;
			this.defaultValue = defaultValue;
			this.type = type;
		}

		public String getName() {
			return this.name;
		}

		Object extractValue(Properties props) {
			String tempString = props.getProperty(this.name);
			if (tempString == null) {
				tempString = System.getProperty(this.name);
			}

			if (tempString != null) {
				switch (this.type) {
					case 1:
						return Integer.valueOf(tempString);
					case 2:
						return Long.valueOf(tempString);
					case 3:
						return "true".equalsIgnoreCase(tempString) ? Boolean.TRUE : Boolean.FALSE;
					default:
						return tempString;
				}
			} else {
				return this.defaultValue;
			}
		}
	}
}
