package com.sos.jitl.housekeeping.dequeuemail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.notification.helper.settings.InternalNotificationSettings;
import com.sos.jitl.notification.helper.settings.MailSettings;
import com.sos.jitl.notification.model.internal.ExecutorModel;
import com.sos.jitl.notification.model.internal.ExecutorModel.InternalType;

import sos.net.SOSMail;
import sos.settings.SOSProfileSettings;
import sos.settings.SOSSettings;
import sos.util.SOSFile;
import sos.util.SOSFileOperations;
import sos.util.SOSString;

public class DequeueMailExecuter {

    private static final String MSG_CODE_LONGER = "SCHEDULER-712";
    private static final String MSG_CODE_SHORTER = "SCHEDULER-711";
    private JobSchedulerDequeueMailJobOptions jobSchedulerDequeueMailJobOptions;
    private static final Logger LOGGER = LoggerFactory.getLogger(DequeueMailExecuter.class);

    private Vector<File> mailOrders = null;
    private Iterator<File> mailOrderIterator = null;
    private SOSMail sosMail;
    private boolean isFileOrder;
    private String hibernateConfiurationFile;
    private String configDir;
    private boolean notification;
    private String schedulerId;

    public DequeueMailExecuter(JobSchedulerDequeueMailJobOptions jobSchedulerDequeueMailJobOptions) {
        super();
        this.jobSchedulerDequeueMailJobOptions = jobSchedulerDequeueMailJobOptions;
    }

    public void execute() throws RuntimeException, Exception {
        sosMail = new SOSMail(jobSchedulerDequeueMailJobOptions.smtpHost.getValue());
        sosMail.setQueueMailOnError(false);
        isFileOrder = jobSchedulerDequeueMailJobOptions.fileWatching.value();
        readMailOrders();
        while (mailOrderIterator.hasNext()) {
            processOneFile(mailOrderIterator.next());
        }
    }

    private void readMailOrders() throws RuntimeException, Exception {
        if (isFileOrder) {
            mailOrders = new Vector<File>();
            mailOrders.add(new File(jobSchedulerDequeueMailJobOptions.emailFileName.getValue()));
        } else {
            mailOrders = SOSFile.getFilelist(jobSchedulerDequeueMailJobOptions.queueDirectory.getValue(),
                    jobSchedulerDequeueMailJobOptions.queuePrefix.getValue(), 0);
        }
        mailOrderIterator = mailOrders.iterator();
        if (!mailOrders.isEmpty()) {
            if (!isFileOrder) {
                LOGGER.info(mailOrders.size() + " mail files found");
            }
        }
    }

    private File getWorkFile(File listFile) throws Exception {
        File workFile = new File(listFile.getAbsolutePath());
        if (!workFile.exists()) {
            throw new Exception("mail file [" + workFile.getAbsolutePath() + "] does not exist.");
        } else if (!workFile.canRead()) {
            throw new Exception("cannot read from mail file [" + workFile.getAbsolutePath() + "]");
        }
        return workFile;
    }

    private File getFailedPath(File workFile) {
        String failedPath = workFile.getParent();
        String failedName = workFile.getName();
        if (failedName.endsWith("~")) {
            failedName = failedName.substring(0, failedName.length() - 1);
        }
        return new File(failedPath, jobSchedulerDequeueMailJobOptions.failedPrefix.getValue() + failedName);
    }

    private File getNotifiedPath(File workFile) {
        String notifiedPath = workFile.getParent();
        String notifiedName = workFile.getName();
        if (notifiedName.endsWith("~")) {
            notifiedName = notifiedName.substring(0, notifiedName.length() - 1);
        }
        return new File(notifiedPath, "notified." + notifiedName);
    }

    private void sendMessage(File messageFile, int curDeliveryCounter) throws Exception {
        try {
            boolean shouldSend = true;
            File mailFile = null;
            String message = "";
            if (jobSchedulerDequeueMailJobOptions.logDirectory.isDirty() && !jobSchedulerDequeueMailJobOptions.logDirectory.getValue().isEmpty()) {
                mailFile = this.getMailFile(jobSchedulerDequeueMailJobOptions.logDirectory.getValue());
                sosMail.dumpMessageToFile(mailFile, true);
            }
            int maxDeliveryCounter = jobSchedulerDequeueMailJobOptions.maxDelivery.value();
            boolean sendOk = jobSchedulerDequeueMailJobOptions.logOnly.value() || sosMail.send();
            if (!sendOk || jobSchedulerDequeueMailJobOptions.logOnly.value()) {
                String but = "";
                String trials = "";
                if (jobSchedulerDequeueMailJobOptions.logOnly.value()) {
                    but = "stored to a file:" + mailFile.getAbsolutePath();
                } else {
                    but = "stored for later dequeueing:" + mailFile.getAbsolutePath();
                }
                if (maxDeliveryCounter > 0) {
                    trials = "(trial " + curDeliveryCounter + " of " + maxDeliveryCounter + ")";
                }
                message = String.format("mail was NOT sent %s but %s", trials, but);
                LOGGER.info(message);
                if (message.length() > 250) {
                    message = message.substring(message.length() - 250);
                }
                message = message.replaceAll("'", "''");
                shouldSend = false;
            }
            try {
                if (!shouldSend && curDeliveryCounter > maxDeliveryCounter && maxDeliveryCounter > 0) {
                    throw new Exception("number of trials [" + maxDeliveryCounter + "] exceeded to send mail from file: " + messageFile
                            .getAbsolutePath());
                }
            } catch (Exception e) {
                throw new Exception(e.getMessage());
            }
            if (jobSchedulerDequeueMailJobOptions.logOnly.value()) {
                LOGGER.info("mail was processed from file [" + messageFile.getAbsolutePath() + "] to: " + sosMail.getRecipientsAsString() + " into: "
                        + mailFile.getAbsolutePath());
            } else {
                LOGGER.info("mail was sent from file [" + messageFile.getAbsolutePath() + "] to: " + sosMail.getRecipientsAsString());
            }
            LOGGER.debug("mail was sent with headers: " + sosMail.dumpHeaders());
            messageFile.delete();
        } catch (Exception ex) {
            throw new Exception("mail was NOT sent from file [" + messageFile.getAbsolutePath() + "]" + ex.getMessage());
        }
    }

    private File getMailFile(String path) throws Exception {
        Date d = new Date();
        StringBuffer bb = new StringBuffer();
        SimpleDateFormat s = new SimpleDateFormat(jobSchedulerDequeueMailJobOptions.queuePattern.getValue());
        if (!path.endsWith("/") && !path.endsWith("\\")) {
            path += "/";
        }
        FieldPosition fp = new FieldPosition(0);
        StringBuffer b = s.format(d, bb, fp);
        String lastGeneratedFilename = path + jobSchedulerDequeueMailJobOptions.queuePrefix.getValue() + b + ".email";
        File f = new File(lastGeneratedFilename);
        while (f.exists()) {
            b = s.format(d, bb, fp);
            lastGeneratedFilename = path + jobSchedulerDequeueMailJobOptions.queuePrefix.getValue() + b + ".email";
            f = new File(lastGeneratedFilename);
        }
        return f;
    }

    private String getBodyFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        String result = "";
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result = result + "\n" + bodyPart.getContent();
                break; // without break same text appears twice in my tests
            } else if (bodyPart.isMimeType("text/html")) {
                result = result + "\n" + bodyPart.getContent();
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result = result + getBodyFromMimeMultipart((MimeMultipart) bodyPart.getContent());
            }
        }
        return result;
    }

    private String getBodyFromMessage(MimeMessage message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getBodyFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    private void processOneFile(File listFile) throws Exception {
        boolean send = true;
        File workFile = getWorkFile(listFile);
        LOGGER.info("processing mail file: " + workFile.getAbsolutePath());
        File failedFile = getFailedPath(workFile);
        File notifiedFile = getNotifiedPath(workFile);
        File messageFile = new File(workFile.getAbsolutePath() + "~");
        if (messageFile.exists()) {
            messageFile.delete();
        }
        workFile.renameTo(messageFile);

        sosMail.setQueueDir(jobSchedulerDequeueMailJobOptions.queueDirectory.getValue());
        sosMail.setQueuePraefix(jobSchedulerDequeueMailJobOptions.queuePrefix.getValue());
        SOSSettings smtpSettings = new SOSProfileSettings(jobSchedulerDequeueMailJobOptions.iniPath.getValue());
        Properties smtpProperties = smtpSettings.getSection("smtp");

        sosMail.setProperties(smtpProperties);

        try {
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
                if (smtpProperties.getProperty("mail.smtp.security_protocol") != null && !smtpProperties.getProperty("mail.smtp.security_protocol")
                        .isEmpty()) {
                    sosMail.setSecurityProtocol(smtpProperties.getProperty("mail.smtp.security_protocol"));
                }

            }
            try {
                sosMail.loadFile(messageFile);

                if (notification) {
                    String body = getBodyFromMessage(sosMail.getMessage());
                    LOGGER.debug("Body: " + body);
                    boolean considerShort = getConsider(body, "[warning].*Task.*runs shorter than the expected duration", ".*" + MSG_CODE_SHORTER
                            + ".*");
                    boolean considerLong = getConsider(body, "[warning].*Task.*runs longer than the expected duration", ".*" + MSG_CODE_LONGER
                            + ".*");
                    boolean considerMasterMessage = !getConsider(body, "([warning]|[error]).*Task", null);
                    boolean considerTaskWarningMessage = getConsider(body, "[warning].*Task.*terminated with warnings", null);

                    if (considerTaskWarningMessage) {
                        String varTitle = sosMail.getMessage().getSubject();
                        send = !executeNotification(InternalType.TASK_WARNING, varTitle, body);
                    } else {
                        if (considerShort) {
                            String varTitle = sosMail.getMessage().getSubject();
                            // String varText = MSG_CODE_SHORTER + ": " + getSubString(body, MSG_CODE_SHORTER + "(.*?)$");
                            send = !executeNotification(InternalType.TASK_IF_SHORTER_THAN, varTitle, body);
                        } else {

                            if (considerLong) {
                                String varTitle = sosMail.getMessage().getSubject();
                                send = !executeNotification(InternalType.TASK_IF_LONGER_THAN, varTitle, body);
                            } else {
                                if (considerMasterMessage) {
                                    String varTitle = sosMail.getMessage().getSubject();
                                    send = !executeNotification(InternalType.MASTER_MESSAGE, varTitle, body);
                                }
                            }
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("mail file [" + workFile.getAbsolutePath() + "]: " + e.getMessage());
            }
            int curDeliveryCounter = 0;
            int maxDeliveryCounter = jobSchedulerDequeueMailJobOptions.maxDelivery.value();
            try {
                if (sosMail.getMessage().getHeader("X-SOSMail-delivery-counter") != null && sosMail.getMessage().getHeader(
                        "X-SOSMail-delivery-counter").length > 0) {
                    try {
                        curDeliveryCounter = Integer.parseInt(sosMail.getMessage().getHeader("X-SOSMail-delivery-counter")[0].toString().trim());
                    } catch (Exception ex) {
                        throw new Exception("illegal header value for X-SOSMail-delivery-counter: " + sosMail.getMessage().getHeader(
                                "X-SOSMail-delivery-counter")[0].toString());
                    }
                    if (++curDeliveryCounter > maxDeliveryCounter && maxDeliveryCounter > 0) {
                        LOGGER.debug("mail file [" + workFile.getAbsolutePath() + "] exceeds number of trials [" + maxDeliveryCounter
                                + "] to send mail and will not be dequeued");
                        sosMail.setQueueDir("");
                    }
                }
                sosMail.getMessage().setHeader("X-SOSMail-delivery-counter", String.valueOf(curDeliveryCounter));
                sosMail.getMessage().saveChanges();
            } catch (Exception e) {
                throw new Exception("mail file [" + workFile.getAbsolutePath() + "]: " + e.getMessage());
            }
            if (send) {
                sendMessage(messageFile, curDeliveryCounter);
            } else {
                messageFile.delete();
            }
        } finally {

            try {
                if (messageFile.exists()) {
                    LOGGER.info("mail file is renamed to exclude it from further processing: " + failedFile.getAbsolutePath());
                    messageFile.renameTo(failedFile);
                }
            } catch (Exception ex) {
                // gracefully ignore this error to preserve the original exception
            }
        }
    }

    private String getSubString(String searchString, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(searchString);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }

    private boolean getConsider(String body, String regexSubject, String regexBody) throws MessagingException, IOException {
        Matcher regExJobMatcher = null;
        regExJobMatcher = Pattern.compile(regexSubject).matcher("");
        boolean consider = regExJobMatcher.reset(sosMail.getMessage().getSubject()).find();
        if (regexBody != null) {
            regExJobMatcher = Pattern.compile(regexBody).matcher("");
            consider = consider || regExJobMatcher.reset(body).find();
        }
        return consider;
    }

    private boolean executeNotification(InternalType internalType, String varTitle, String body) throws MessagingException, IOException {
        boolean notify = true;
        String msgCode = "";
        if (internalType.equals(InternalType.TASK_IF_SHORTER_THAN)) {
            msgCode = MSG_CODE_SHORTER;
        } 
        else if (internalType.equals(InternalType.TASK_IF_LONGER_THAN)) {
            msgCode = MSG_CODE_LONGER;
        }
        else if (internalType.equals(InternalType.TASK_WARNING)) {
            String regex = ".*(SCHEDULER-(.*?)\\s)";
            String code = getSubString(body, regex);
            if(SOSString.isEmpty(code)) {
                code = "WARN";
            }
            msgCode = code;
        }
        else {
            try {
                String regex = ".*((SCHEDULER|ERRNO|WSWIN)-(.*?)\\s)";
                String code = getSubString(varTitle, regex);
                if(SOSString.isEmpty(code)) {
                    regex = ".*(Z-JAVA-(.*?)\\s)";
                    code = getSubString(body, regex);
                }
                if(!SOSString.isEmpty(code)) {
                    msgCode = code;
                }
            }
            catch(Exception ex) {}
        }
        
        String schedulerId = getSubString(body, ".*JobScheduler -id=(.*?)host");
        if (schedulerId.isEmpty()) {
            schedulerId = this.schedulerId;
        }
        String taskId = getSubString(body, ".*Task:.*ID:(.*?)\\s");
        if (taskId.isEmpty()) {
            taskId = getSubString(body, ".*Task.*with ID (.*?)\\s");
        }
        String jobPath = getSubString(body, ".*Task:.(.*?)ID:");
        LOGGER.debug("InternalType:" + internalType);
        LOGGER.debug("msgCode:" + msgCode);
        LOGGER.debug("vartitle=" + varTitle);
        LOGGER.debug("vartext=" + body);
        LOGGER.debug("schedulerId=" + schedulerId);
        LOGGER.debug("jobPath=" + jobPath);
        LOGGER.debug("taskId=" + taskId);
        LOGGER.debug("configuration Directory=" + configDir);
        LOGGER.debug("Hibernate cfg=" + this.hibernateConfiurationFile);
        if (!configDir.isEmpty()) {
            MailSettings mailSettings = new MailSettings();

            mailSettings.setIniPath(jobSchedulerDequeueMailJobOptions.iniPath.getValue());
            LOGGER.debug("iniPath:" + jobSchedulerDequeueMailJobOptions.iniPath.getValue());
            mailSettings.setSmtp(sosMail.getHost());
            LOGGER.debug("smtp:" + sosMail.getHost());
            mailSettings.setQueueDir(sosMail.getQueueDir());
            LOGGER.debug("queueDir:" + sosMail.getQueueDir());
            String from = "JobScheduler";
            if (sosMail.getMessage().getHeader("From") != null && sosMail.getMessage().getHeader("From").length > 0) {
                from = sosMail.getMessage().getHeader("From")[0].toString().trim();
            }
            mailSettings.setFrom(from);
            LOGGER.debug("from:" + from);
            mailSettings.setTo(sosMail.getRecipientsAsString());
            LOGGER.debug("to:" + sosMail.getRecipientsAsString());
            mailSettings.setCc(sosMail.getCCsAsString());
            LOGGER.debug("cc:" + sosMail.getCCsAsString());
            mailSettings.setBcc(sosMail.getBCCsAsString());
            LOGGER.debug("bcc:" + sosMail.getBCCsAsString());

            ExecutorModel model = new ExecutorModel(Paths.get(configDir), Paths.get(this.hibernateConfiurationFile), mailSettings);

            InternalNotificationSettings settings = new InternalNotificationSettings();
            settings.setSchedulerId(schedulerId);
            settings.setTaskId(taskId);
            settings.setMessage(body);
            settings.setMessageTitle(varTitle);
            settings.setMessageCode(msgCode);

            notify = model.process(internalType, settings);
        } else {
            notify = false;
        }

        return notify;
    }

    @SuppressWarnings("deprecation")
    public void resendFailedMails() throws Exception {
        String prefix = jobSchedulerDequeueMailJobOptions.failedPrefix.getValue();
        String source = jobSchedulerDequeueMailJobOptions.queueDirectory.getValue();
        String fileSpec = prefix + ".*$";
        String replacing = prefix;
        String replacement = "";

        SOSFileOperations.renameFileCnt(source, null, fileSpec, 0, 0, replacing, replacement, null, null, null, null, 0, 0);
    }

    public void setHibernateConfigurationFile(String hibernateConfigurationFile) {
        this.hibernateConfiurationFile = hibernateConfigurationFile;
    }

    public void setConfigDir(String configDir) {
        this.configDir = configDir;
    }

    public void setNotification(boolean notification) {
        this.notification = notification;
    }

    public String getSchedulerId() {
        return schedulerId;
    }

    public void setSchedulerId(String schedulerId) {
        this.schedulerId = schedulerId;
    }

}