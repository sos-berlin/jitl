package com.sos.jitl.housekeeping.dequeuemail;

import java.io.File;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;

import sos.net.SOSMail;
import sos.settings.SOSSettings;
import sos.settings.SOSProfileSettings;
import sos.util.SOSFile;

public class DequeueMailExecuter {

    private JobSchedulerDequeueMailJobOptions jobSchedulerDequeueMailJobOptions;
    private static Logger logger = Logger.getLogger(DequeueMailExecuter.class);

    private Vector<File> mailOrders = null;
    private Iterator<File> mailOrderIterator = null;
    private SOSMail sosMail;

    public DequeueMailExecuter(JobSchedulerDequeueMailJobOptions jobSchedulerDequeueMailJobOptions) {
        super();
        this.jobSchedulerDequeueMailJobOptions = jobSchedulerDequeueMailJobOptions;
    }

    public void execute() throws RuntimeException, Exception {
        sosMail = new SOSMail(jobSchedulerDequeueMailJobOptions.smtp_host.Value());
        readMailOrders();
        while (mailOrderIterator.hasNext()) {
            processOneFile(mailOrderIterator.next());
        }
    }

    private void readMailOrders() throws RuntimeException, Exception {
        mailOrders = SOSFile.getFilelist(jobSchedulerDequeueMailJobOptions.queue_directory.Value(), jobSchedulerDequeueMailJobOptions.queue_prefix.Value(), 0);
        mailOrderIterator = mailOrders.iterator();
        if (mailOrders.size() > 0)
            logger.info(mailOrders.size() + " mail files found");
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
        return new File(failedPath, jobSchedulerDequeueMailJobOptions.failed_prefix.Value() + failedName);
    }

    private void sendMessage(File messageFile, int curDeliveryCounter) throws Exception {
        try { // to send
            boolean shouldSend = true;
            File mailFile = null;
            String message = "";

            if (jobSchedulerDequeueMailJobOptions.log_directory.isNotDirty() && jobSchedulerDequeueMailJobOptions.log_directory.Value().length() > 0) {
                // dump message with attachments
                mailFile = this.getMailFile(jobSchedulerDequeueMailJobOptions.log_directory.Value());
                sosMail.dumpMessageToFile(mailFile, true);
            }

            int maxDeliveryCounter = jobSchedulerDequeueMailJobOptions.max_delivery.value();

            boolean sendOk = (jobSchedulerDequeueMailJobOptions.log_only.value() || sosMail.send());

            if (!sendOk | jobSchedulerDequeueMailJobOptions.log_only.value()) {

                String but = "";
                String trials = "";
                if (jobSchedulerDequeueMailJobOptions.log_only.value()) {
                    but = "stored to a file:" + mailFile.getAbsolutePath();
                } else {
                    but = "stored for later dequeueing:" + mailFile.getAbsolutePath();
                }

                if (maxDeliveryCounter > 0) {
                    trials = "(trial " + curDeliveryCounter + " of " + maxDeliveryCounter + ")";
                }

                message = String.format("mail was NOT sent %s but %s", trials, but);
                logger.info(message);

                if (message.length() > 250) {
                    message = message.substring(message.length() - 250);
                }
                message = message.replaceAll("'", "''");
                shouldSend = false;
            }

            try { // to check the delivery counter if mail could not be sent
                if (!shouldSend && curDeliveryCounter > maxDeliveryCounter && maxDeliveryCounter > 0) {
                    throw new Exception("number of trials [" + maxDeliveryCounter + "] exceeded to send mail from file: "
                            + messageFile.getAbsolutePath());
                }
            } catch (Exception e) {
                throw new Exception(e.getMessage());
            }

            if (jobSchedulerDequeueMailJobOptions.log_only.value()) {
                logger.info("mail was processed from file [" + messageFile.getAbsolutePath() + "] to: " + sosMail.getRecipientsAsString() + " into: "
                        + mailFile.getAbsolutePath());
            } else {
                logger.info("mail was sent from file [" + messageFile.getAbsolutePath() + "] to: " + sosMail.getRecipientsAsString());
            }

            logger.debug("mail was sent with headers: " + sosMail.dumpHeaders());
            messageFile.delete();

        } catch (Exception ex) {
            throw new Exception("mail was NOT sent from file [" + messageFile.getAbsolutePath() + "]" + ex.getMessage());
        }
    }

    private File getMailFile(String path) throws Exception {

        Date d = new Date();
        StringBuffer bb = new StringBuffer();
        SimpleDateFormat s = new SimpleDateFormat(jobSchedulerDequeueMailJobOptions.queue_pattern.Value());

        if (!path.endsWith("/") && !path.endsWith("\\")) {
            path += "/";
        }

        FieldPosition fp = new FieldPosition(0);
        StringBuffer b = s.format(d, bb, fp);
        String lastGeneratedFilename = path + jobSchedulerDequeueMailJobOptions.queue_prefix.Value() + b + ".email";

        File f = new File(lastGeneratedFilename);
        while (f.exists()) {
            b = s.format(d, bb, fp);
            lastGeneratedFilename = path + jobSchedulerDequeueMailJobOptions.queue_prefix.Value() + b + ".email";
            f = new File(lastGeneratedFilename);
        }
        return f;
    }

    private void processOneFile(File listFile) throws Exception {
        File workFile = getWorkFile(listFile);
        logger.info("processing mail file: " + workFile.getAbsolutePath());

        File failedFile = getFailedPath(workFile);

        File messageFile = new File(workFile.getAbsolutePath() + "~");

        if (messageFile.exists()) {
            messageFile.delete();
        }

        workFile.renameTo(messageFile);

        sosMail.setQueueDir(jobSchedulerDequeueMailJobOptions.queue_directory.Value());

        // set queue prefix "sos" to enable dequeueing by
        // JobSchedulerMailDequeueJob
        sosMail.setQueuePraefix(jobSchedulerDequeueMailJobOptions.queue_prefix.Value());

        SOSSettings smtpSettings = new SOSProfileSettings(jobSchedulerDequeueMailJobOptions.ini_path.Value());
        Properties smtpProperties = smtpSettings.getSection("smtp");

        if (!smtpProperties.isEmpty()) {
            if (smtpProperties.getProperty("mail.smtp.user") != null && smtpProperties.getProperty("mail.smtp.user").length() > 0) {
                sosMail.setUser(smtpProperties.getProperty("mail.smtp.user"));
            }
            if (smtpProperties.getProperty("mail.smtp.password") != null && smtpProperties.getProperty("mail.smtp.password").length() > 0) {
                sosMail.setPassword(smtpProperties.getProperty("mail.smtp.password"));
            }
            if (smtpProperties.getProperty("mail.smtp.port") != null && smtpProperties.getProperty("mail.smtp.port").length() > 0) {
                sosMail.setPort(smtpProperties.getProperty("mail.smtp.port"));
            }
        }

        try { // to load mail and check delivery counter
            sosMail.loadFile(messageFile);
        } catch (Exception e) {
            throw new Exception("mail file [" + workFile.getAbsolutePath() + "]: " + e.getMessage());
        }

        int curDeliveryCounter = 0;
        int maxDeliveryCounter = jobSchedulerDequeueMailJobOptions.max_delivery.value();
        try { // to check the delivery counter
            if (sosMail.getMessage().getHeader("X-SOSMail-delivery-counter") != null
                    && sosMail.getMessage().getHeader("X-SOSMail-delivery-counter").length > 0) {
                try {
                    curDeliveryCounter = Integer.parseInt(sosMail.getMessage().getHeader("X-SOSMail-delivery-counter")[0].toString().trim());
                } catch (Exception ex) {
                    throw new Exception("illegal header value for X-SOSMail-delivery-counter: "
                            + sosMail.getMessage().getHeader("X-SOSMail-delivery-counter")[0].toString());
                }
                if (++curDeliveryCounter > maxDeliveryCounter && maxDeliveryCounter > 0) {
                    logger.debug("mail file [" + workFile.getAbsolutePath() + "] exceeds number of trials [" + maxDeliveryCounter
                            + "] to send mail and will not be dequeued");
                    sosMail.setQueueDir("");
                }
            }
            sosMail.getMessage().setHeader("X-SOSMail-delivery-counter", String.valueOf(curDeliveryCounter));
            sosMail.getMessage().saveChanges();
        } catch (Exception e) {
            throw new Exception("mail file [" + workFile.getAbsolutePath() + "]: " + e.getMessage());
        }

        sendMessage(messageFile, curDeliveryCounter);

        try {
            // this file should only exist in case of errors, we rename it with
            // a prefix to prevent further processing
            if (messageFile.exists()) {
                logger.info("mail file is renamed to exclude it from further processing: " + failedFile.getAbsolutePath());
                messageFile.renameTo(failedFile);
            }
        } catch (Exception ex) {
        } // gracefully ignore this error to preserve the original exception

    }
}
