//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.masukomi.aspirin.core.store.mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.masukomi.aspirin.core.Helper;
import org.masukomi.aspirin.core.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileMailStore implements MailStore {
	private static final Logger log = LoggerFactory.getLogger(FileMailStore.class);
	private final Helper helper;
	private File rootDir;
	private int subDirCount = 3;
	private Random rand = new Random();
	private Map<String, WeakReference<MimeMessage>> messageMap = new HashMap();
	private Map<String, String> messagePathMap = new HashMap();

	public FileMailStore(Configuration configuration) {
		this.helper = new Helper(configuration);
	}

	public MimeMessage get(String mailid) {
		WeakReference<MimeMessage> msgRef = (WeakReference)this.messageMap.get(mailid);
		MimeMessage msg = null;
		if (msgRef != null) {
			msg = (MimeMessage)msgRef.get();
			if (msg == null) {
				try {
					msg = new MimeMessage(Session.getDefaultInstance(System.getProperties()), new FileInputStream(new File((String)this.messagePathMap.get(mailid))));
					new WeakReference(msg);
				} catch (FileNotFoundException var5) {
					log.error(this.getClass().getSimpleName() + " No file representation found for name " + mailid, var5);
				} catch (MessagingException var6) {
					log.error(this.getClass().getSimpleName() + " There is a messaging exception with name " + mailid, var6);
				}
			}
		}

		return msg;
	}

	public List<String> getMailIds() {
		return new ArrayList(this.messageMap.keySet());
	}

	public void init() {
		if (this.rootDir.exists()) {
			File[] subdirs = this.rootDir.listFiles();
			if (subdirs != null) {
				File[] var2 = subdirs;
				int var3 = subdirs.length;

				for(int var4 = 0; var4 < var3; ++var4) {
					File subDir = var2[var4];
					if (subDir.isDirectory()) {
						File[] subdirFiles = subDir.listFiles();
						if (subdirFiles != null) {
							File[] var7 = subdirFiles;
							int var8 = subdirFiles.length;

							for(int var9 = 0; var9 < var8; ++var9) {
								File msgFile = var7[var9];

								try {
									MimeMessage msg = new MimeMessage(Session.getDefaultInstance(System.getProperties()), new FileInputStream(msgFile));
									String mailid = this.helper.getMailID(msg);
									synchronized(this.messageMap) {
										this.messageMap.put(mailid, new WeakReference(msg));
										this.messagePathMap.put(mailid, msgFile.getAbsolutePath());
									}
								} catch (FileNotFoundException var16) {
									log.error(this.getClass().getSimpleName() + " No file representation found with name " + msgFile.getAbsolutePath(), var16);
								} catch (MessagingException var17) {
									log.error(this.getClass().getSimpleName() + " There is a messaging exception in file " + msgFile.getAbsolutePath(), var17);
								}
							}
						}
					}
				}

			}
		}
	}

	public void remove(String mailid) {
		synchronized(this.messageMap) {
			this.messageMap.remove(mailid);
			synchronized(this.messagePathMap) {
				File f = new File((String)this.messagePathMap.get(mailid));
				f.delete();
				this.messagePathMap.remove(mailid);
			}

		}
	}

	public void set(String mailid, MimeMessage msg) {
		if (this.rootDir == null) {
			throw new RuntimeException(this.getClass().getSimpleName() + " Please set up root directory.");
		} else {
			String subDirName = String.valueOf(this.rand.nextInt(this.subDirCount));
			File dir = new File(this.rootDir, subDirName);
			if (!dir.exists()) {
				dir.mkdirs();
			}

			String filepath = (new File(dir, mailid + ".msg")).getAbsolutePath();

			try {
				File msgFile = new File(filepath);
				if (msgFile.exists()) {
					msgFile.delete();
				}

				if (!msgFile.exists()) {
					msgFile.createNewFile();
				}

				msg.writeTo(new FileOutputStream(msgFile));
				synchronized(this.messageMap) {
					this.messageMap.put(mailid, new WeakReference(msg));
					this.messagePathMap.put(mailid, filepath);
				}
			} catch (FileNotFoundException var10) {
				log.error(this.getClass().getSimpleName() + " No file representation found for name " + mailid, var10);
			} catch (IOException var11) {
				log.error(this.getClass().getSimpleName() + " Could not write file for name " + mailid, var11);
			} catch (MessagingException var12) {
				log.error(this.getClass().getSimpleName() + " There is a messaging exception with name " + mailid, var12);
			}

		}
	}

	public void setRootDir(File rootDir) {
		this.rootDir = rootDir;
	}

	public File getRootDir() {
		return this.rootDir;
	}

	public void setSubDirCount(int subDirCount) {
		this.subDirCount = subDirCount;
	}

	public int getSubDirCount() {
		return this.subDirCount;
	}
}
