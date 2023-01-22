//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.masukomi.aspirin.core.config;

public interface ConfigurationMBean {
	String PARAM_DELIVERY_ATTEMPT_DELAY = "aspirin.delivery.attempt.delay";
	String PARAM_DELIVERY_ATTEMPT_COUNT = "aspirin.delivery.attempt.count";
	String PARAM_DELIVERY_BOUNCE_ON_FAILURE = "aspirin.delivery.bounce-on-failure";
	String PARAM_DELIVERY_DEBUG = "aspirin.delivery.debug";
	String PARAM_DELIVERY_EXPIRY = "aspirin.delivery.expiry";
	String PARAM_DELIVERY_THREADS_ACTIVE_MAX = "aspirin.delivery.threads.active.max";
	String PARAM_DELIVERY_THREADS_IDLE_MAX = "aspirin.delivery.threads.idle.max";
	String PARAM_DELIVERY_TIMEOUT = "aspirin.delivery.timeout";
	String PARAM_ENCODING = "aspirin.encoding";
	String PARAM_HOSTNAME = "aspirin.hostname";
	String PARAM_LOGGER_NAME = "aspirin.logger.name";
	String PARAM_LOGGER_PREFIX = "aspirin.logger.prefix";
	String PARAM_POSTMASTER_EMAIL = "aspirin.postmaster.email";
	String PARAM_MAILSTORE_CLASS = "aspirin.mailstore.class";
	long NEVER_EXPIRES = -1L;

	int getDeliveryAttemptDelay();

	int getDeliveryAttemptCount();

	int getDeliveryThreadsActiveMax();

	int getDeliveryThreadsIdleMax();

	int getDeliveryTimeout();

	String getEncoding();

	long getExpiry();

	String getLoggerName();

	String getLoggerPrefix();

	String getMailStoreClassName();

	String getPostmasterEmail();

	String getHostname();

	boolean isDeliveryBounceOnFailure();

	boolean isDeliveryDebug();

	void setDeliveryAttemptDelay(int var1);

	void setDeliveryAttemptCount(int var1);

	void setDeliveryBounceOnFailure(boolean var1);

	void setDeliveryDebug(boolean var1);

	void setDeliveryThreadsActiveMax(int var1);

	void setDeliveryThreadsIdleMax(int var1);

	void setDeliveryTimeout(int var1);

	void setEncoding(String var1);

	void setExpiry(long var1);

	void setMailStoreClassName(String var1);

	void setPostmasterEmail(String var1);

	void setHostname(String var1);
}
