/*
 * JobSchedulerManagedMailJob.java Created on 02.08.2007
 */
package sos.scheduler.managed;

import java.io.File;
import java.util.HashMap;
import java.util.Properties;

import sos.net.SOSMail;
import sos.net.SOSMailAttachment;
import sos.settings.SOSProfileSettings;
import sos.settings.SOSSettings;
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.util.SOSFile;

public class JobSchedulerManagedMailJob extends JobSchedulerManagedJob {

	@Override
	public boolean spooler_process() {

		Order order = null;
		orderPayload = null;
		String orderId = "(none)";
		String host = spooler_log.mail().smtp();
		boolean hostChanged = false;
		int port = 25;
		boolean portChanged = false;
		String queueDir = spooler_log.mail().queue_dir();
		boolean queueDirChanged = false;
		String from = spooler_log.mail().from();
		boolean fromChanged = false;
		boolean queueMailOnError = true;
		String fromName = "";
		String replyTo = "";
		String to = "";
		String cc = "";
		String bcc = "";
		String subject = "";
		String body = "";
		String contentType = "";
		String encoding = "";
		String attachmentCharset = "";
		String attachmentContentType = "";
		String attachmentEncoding = "";
		boolean cleanupAttachment = false;
		String[] attachments = {};
		String smtpUser = "";
		String smtpPass = "";
		String securityProtocol = "";

		try {
			try {
				super.prepareParams();
			} catch (Exception e) {
				throw new Exception("error occurred preparing order: " + e.getMessage());
			}
			if (doSendMail()) {
				try {
					if (this.getParameters().get("to") != null && !this.getParameters().get("to").isEmpty()) {
						to = this.getParameters().get("to");
					} else {
						throw new Exception("no value was specified for mandatory parameter [to]");
					}
					if (this.getParameters().get("subject") != null && !this.getParameters().get("subject").isEmpty()) {
						subject = this.getParameters().get("subject");
					} else {
						throw new Exception("no value was specified for mandatory parameter [subject]");
					}
					if (this.getParameters().get("host") != null && !this.getParameters().get("host").isEmpty()) {
						host = this.getParameters().get("host");
						hostChanged = true;
					}

					if (this.getParameters().get("port") != null && !this.getParameters().get("port").isEmpty()) {
						try {
							port = Integer.parseInt(this.getParameters().get("port"));
							portChanged = true;
						} catch (Exception e) {
							throw new Exception("illegal, non-numeric value [" + this.getParameters().get("port")
									+ "] for parameter [port]: " + e.getMessage());
						}
					}
					if (this.getParameters().get("smtp_user") != null
							&& !this.getParameters().get("smtp_user").isEmpty()) {
						smtpUser = this.getParameters().get("smtp_user");
					}
					if (this.getParameters().get("smtp_password") != null
							&& !this.getParameters().get("smtp_password").isEmpty()) {
						smtpPass = this.getParameters().get("smtp_password");
					}
					if (this.getParameters().get("queue_directory") != null
							&& !this.getParameters().get("queue_directory").isEmpty()) {
						queueDir = this.getParameters().get("queue_directory");
						queueDirChanged = true;
					}
					if (this.getParameters().get("from") != null && !this.getParameters().get("from").isEmpty()) {
						from = this.getParameters().get("from");
						fromChanged = true;
					}
					if (this.getParameters().get("cc") != null && !this.getParameters().get("cc").isEmpty()) {
						cc = this.getParameters().get("cc");
					}
					if (this.getParameters().get("bcc") != null && !this.getParameters().get("bcc").isEmpty()) {
						bcc = this.getParameters().get("bcc");
					}
					if (this.getParameters().get("from_name") != null
							&& !this.getParameters().get("from_name").isEmpty()) {
						fromName = this.getParameters().get("from_name");
					}
					if (this.getParameters().get("reply_to") != null
							&& !this.getParameters().get("reply_to").isEmpty()) {
						replyTo = this.getParameters().get("reply_to");
					}
					if (this.getParameters().get("body") != null && !this.getParameters().get("body").isEmpty()) {
						body = this.getParameters().get("body");
					}
					if (this.getParameters().get("content_type") != null
							&& !this.getParameters().get("content_type").isEmpty()) {
						contentType = this.getParameters().get("content_type");
					}
					if (this.getParameters().get("encoding") != null
							&& !this.getParameters().get("encoding").isEmpty()) {
						encoding = this.getParameters().get("encoding");
					}
					if (this.getParameters().get("attachment_charset") != null
							&& !this.getParameters().get("attachment_charset").isEmpty()) {
						attachmentCharset = this.getParameters().get("attachment_charset");
					}
					if (this.getParameters().get("attachment_content_type") != null
							&& !this.getParameters().get("attachment_content_type").isEmpty()) {
						attachmentContentType = this.getParameters().get("attachment_content_type");
					}
					if (this.getParameters().get("attachment_encoding") != null
							&& !this.getParameters().get("attachment_encoding").isEmpty()) {
						attachmentEncoding = this.getParameters().get("attachment_encoding");
					}
					if (this.getParameters().get("attachment") != null
							&& !this.getParameters().get("attachment").isEmpty()) {
						attachments = this.getParameters().get("attachment").split(";");
					}
					if (this.getParameters().get("security_protocol") != null
							&& !this.getParameters().get("security_protocol").isEmpty()) {
						securityProtocol = this.getParameters().get("security_protocol");
					}
					if (this.getParameters().get("queue_mail_on_error") != null
							&& !this.getParameters().get("queue_mail_on_error").isEmpty()) {
						queueMailOnError = !"false".equalsIgnoreCase(this.getParameters().get("queue_mail_on_error"));
					}
					if (this.getParameters().get("cleanup_attachment") != null
							&& !this.getParameters().get("cleanup_attachment").isEmpty()
							&& ("1".equals(this.getParameters().get("cleanup_attachment"))
									|| "true".equalsIgnoreCase(this.getParameters().get("cleanup_attachment"))
									|| "yes".equalsIgnoreCase(this.getParameters().get("cleanup_attachment")))) {
						cleanupAttachment = true;
					}
				} catch (Exception e) {
					throw new Exception("error occurred checking parameters: " + e.getMessage());
				}
				try {
					SOSMail sosMail;
					Properties mailSection = null;
					if (this.getConnectionSettings() != null) {
						try {
							mailSection = this.getConnectionSettings().getSection("email", "mail_server");
							if (mailSection.isEmpty()) {
								mailSection = null;
							}
						} catch (Exception e) {
							getLogger().debug6("No database settings found, using defaults from factory.ini");
						}
					}
					if (mailSection != null) {
						sosMail = new SOSMail(getConnectionSettings());
						if (hostChanged) {
							sosMail.setHost(host);
						}
						if (queueDirChanged) {
							sosMail.setQueueDir(queueDir);
						}
						if (fromChanged) {
							sosMail.setFrom(from);
						}
					} else {
						 sosMail = new SOSMail(host);
                         sosMail.setQueueDir(queueDir);
                         sosMail.setFrom(from);
                         try {
                             SOSSettings smtpSettings = new SOSProfileSettings(spooler.ini_path());
                             Properties smtpProperties = smtpSettings.getSection("smtp");
                             sosMail.setProperties(smtpProperties);
                             
                         	if (smtpProperties != null && smtpProperties.get("mail.smtp.timeout") != null) {
								try {
									sosMail.setTimeout(
											Integer.parseInt(smtpProperties.get("mail.smtp.timeout").toString()));
								} catch (NumberFormatException e) {

								}
							}

                             if (!smtpProperties.isEmpty()) {
                                 if (smtpProperties.getProperty("mail.smtp.user") != null && !smtpProperties.getProperty("mail.smtp.user").isEmpty()) {
                                     sosMail.setUser(smtpProperties.getProperty("mail.smtp.user"));
                                 }
                                 if (smtpProperties.getProperty("mail.smtp.password") != null && !smtpProperties.getProperty("mail.smtp.password").isEmpty()) {
                                     sosMail.setPassword(smtpProperties.getProperty("mail.smtp.password"));
                                 }
                                 if (smtpProperties.getProperty("mail.smtp.port") != null && !smtpProperties.getProperty("mail.smtp.port").isEmpty()) {
                                     sosMail.setPort(smtpProperties.getProperty("mail.smtp.port"));
                                 }
                             }
						} catch (Exception e) {
							// The job is running on an Universal Agent that
							// does not suppor .ini_path()
						}
					}
					if (portChanged) {
						sosMail.setPort(Integer.toString(port));
					}
					if (!smtpUser.isEmpty()) {
						sosMail.setUser(smtpUser);
					}
					if (!smtpPass.isEmpty()) {
						sosMail.setPassword(smtpPass);
					}
					// set values only if these are set by params, else use
					// defaults from SOSMail
					if (!contentType.isEmpty()) {
						sosMail.setContentType(contentType);
					}
					if (!encoding.isEmpty()) {
						sosMail.setEncoding(encoding);
					}
					if (!attachmentCharset.isEmpty()) {
						sosMail.setAttachmentCharset(attachmentCharset);
					}
					if (!attachmentEncoding.isEmpty()) {
						sosMail.setAttachmentEncoding(attachmentEncoding);
					}
					if (!attachmentContentType.isEmpty()) {
						sosMail.setAttachmentContentType(attachmentContentType);
					}
					if (!fromName.isEmpty()) {
						sosMail.setFromName(fromName);
					}
					sosMail.setSecurityProtocol(securityProtocol);
					String[] recipientsTo = to.split(";|,");
					for (int i = 0; i < recipientsTo.length; i++) {
						if (i == 0) {
							sosMail.setReplyTo(recipientsTo[i].trim());
						}
						sosMail.addRecipient(recipientsTo[i].trim());
					}
					if (!replyTo.isEmpty()) {
						sosMail.setReplyTo(replyTo);
					}
					sosMail.addCC(cc);
					sosMail.addBCC(bcc);
					sosMail.setSubject(subject);
					sosMail.setBody(body);
					for (String attachment2 : attachments) {
						File attachmentFile = new File(attachment2);
						SOSMailAttachment attachment = new SOSMailAttachment(sosMail, attachmentFile);
						attachment.setCharset(sosMail.getAttachmentCharset());
						attachment.setEncoding(sosMail.getAttachmentEncoding());
						attachment.setContentType(sosMail.getAttachmentContentType());
						sosMail.addAttachment(attachment);
					}
					this.getLogger().info("sending mail: \n" + sosMail.dumpMessageAsString());
					sosMail.setQueueMailOnError(queueMailOnError);
					if (!sosMail.send()) {
						this.getLogger()
								.warn("mail server is unavailable, mail for recipient [" + to
										+ "] is queued in local directory [" + sosMail.getQueueDir() + "]:"
										+ sosMail.getLastError());
					}
					if (cleanupAttachment) {
						for (String attachment : attachments) {
							File attachmentFile = new File(attachment);
							if (attachmentFile.exists() && attachmentFile.canWrite()) {
								SOSFile.deleteFile(attachmentFile);
							}
						}
					}
					sosMail.clearRecipients();
				} catch (Exception e) {
					throw new Exception(e.getMessage());
				}
			}
			return spooler_task.job().order_queue() != null;
		} catch (Exception e) {
			if (queueMailOnError) {
				spooler_log.warn("error occurred processing order [" + orderId + "]: " + e.getMessage());
			} else {
				spooler_log.error("error occurred processing order [" + orderId + "]: " + e.getMessage());
			}
			spooler_task.end();
			return false;
		}
	}

	protected boolean doSendMail() {
		return true;
	}

	@Override
	public final HashMap<String, String> getParameters() {
		return convertVariableSet2HashMap(orderPayload);
	}

}