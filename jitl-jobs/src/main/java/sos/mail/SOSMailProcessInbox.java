package sos.mail;

import java.io.File;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.search.SubjectTerm;

import org.apache.log4j.Logger;

import sos.mail.options.SOSMailProcessInboxOptions;
import sos.net.SOSMailAttachment;
import sos.net.SOSMailReceiver;
import sos.net.SOSMimeMessage;
import sos.scheduler.command.SOSSchedulerCommand;
import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Job_chain;
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.util.SOSSchedulerLogger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class SOSMailProcessInbox extends JobSchedulerJobAdapter {

    private static final Logger LOGGER = Logger.getLogger(SOSMailProcessInbox.class);
    private SOSMailReceiver objMailReader;
    private SOSMailProcessInboxOptions objO = null;
    private boolean isLocalScheduler = true;
    private boolean flgCheckdate;
    private long lngMessagesSkipped = 0;
    private Date dteMinAge;

    public boolean spooler_process() throws Exception {
        long lngProcessCount = 0;
        try {
            super.spooler_process();
            objO = new SOSMailProcessInboxOptions();
            objO.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));
            spooler_job.set_state_text("*** running ***");
            if (!objO.mailSchedulerHost.isDirty()) {
                objO.mailSchedulerHost.setValue(spooler.hostname());
                if (!objO.mailSchedulerPort.isDirty()) {
                    objO.mailSchedulerPort.value(spooler.tcp_port());
                }
            }
            if (objO.mailUseSeen.value()) {
                objO.mailSetSeen.value(true);
            }
            objO.checkMandatory();
            isLocalScheduler =
                    objO.mailSchedulerHost.getValue().equalsIgnoreCase(spooler.hostname()) && objO.mailSchedulerPort.value() == spooler.tcp_port();
            dteMinAge = null;
            flgCheckdate = false;
            if (objO.minAge.isDirty()) {
                String strT = objO.minAge.getValue();
                if (!strT.startsWith("-")) {
                    objO.minAge.setValue("-" + strT);
                }
                dteMinAge = objO.minAge.getEndFromNow();
                LOGGER.info(String.format("Min Age defined: %1$s", dteMinAge.toLocaleString()));
                flgCheckdate = true;
            }
            String strMailHost = objO.mailHost.getValue();
            int intMailPort = objO.mailPort.value();
            String strServerType = objO.mailServerType.getValue();
            LOGGER.debug(String.format("Connecting to Mailserver %1$s:%2$d (%3$s)...", strMailHost, intMailPort, strServerType));
            objMailReader = new SOSMailReceiver(objO.mailHost.getValue(), objO.mailPort.getValue(), objO.mailUser.getValue(), objO.mailPassword.getValue());
            objMailReader.setLogger(new SOSSchedulerLogger(spooler_log));
            if (objO.mailServerTimeout.value() > 0) {
                objMailReader.setTimeout(objO.mailServerTimeout.value());
            }
            objMailReader.connect(objO.mailServerType.getValue());
            for (String strMailFolderName : objO.mailMessageFolder.getValue().split("[,|;]")) {
                spooler_job.set_state_text(String.format("processing folder %1$s", strMailFolderName));
                performMessagesInFolder(strMailFolderName.trim());
            }
        } catch (Exception e) {
            String stateText = e.toString();
            spooler_job.set_state_text(stateText);
            LOGGER.info("Job " + spooler_job.name() + " terminated with errors.");
            LOGGER.error("an error occurred while processing: " + stateText);
            throw new JobSchedulerException("", e);
        } finally {
            if (objMailReader != null) {
                objMailReader.closeFolder(true);
                objMailReader.disconnect();
                objMailReader = null;
            }
        }
        LOGGER.info(String.format("%1$d objects processed \n%2$d objects skipped", lngProcessCount, lngMessagesSkipped));
        spooler_job.set_state_text("*** completed ***");
        return signalSuccess();
    }

    private boolean isPerformMessage(SOSMimeMessage sosMimeMessage) throws Exception {
        Date messageDate = sosMimeMessage.getSentDate();
        boolean result = true;
        if (messageDate != null) {
            LOGGER.info(sosMimeMessage.getSubject() + " " + messageDate.toLocaleString());
        }
        if (flgCheckdate && messageDate != null && dteMinAge.before(messageDate)) {
            LOGGER.debug("message skipped due to date constraint: \n" + sosMimeMessage.getSubject() + " " + messageDate);
            lngMessagesSkipped++;
            result = false;
        }
        return result;
    }

    private void executeMessage(SOSMimeMessage sosMimeMessage) throws Exception {
        if (isPerformMessage(sosMimeMessage)) {
            performAction(sosMimeMessage);
        }
    }

    private void performAction(final SOSMimeMessage message) throws Exception {
        String action = "";
        StringTokenizer t = new StringTokenizer(objO.mailAction.getValue(), ",");
        while (t.hasMoreTokens()) {
            action = t.nextToken();
            if ("dump".equalsIgnoreCase(action)) {
                dumpMessage(message);
            } else if ("order".equalsIgnoreCase(action)) {
                startOrder(message);
            } else if ("command".equalsIgnoreCase(action)) {
                executeCommand(message);
            } else if ("delete".equalsIgnoreCase(action)) {
                deleteMessage(message);
            }
            if (objO.mailSetSeen.value()) {
                message.setFlag(Flags.Flag.SEEN, true);
            }
        }
    }

    private void performMessagesInFolder(final String pstrMessageFolder) throws Exception {
        try {
            LOGGER.debug("reading " + pstrMessageFolder);
            Folder folder = objMailReader.openFolder(pstrMessageFolder, objMailReader.READ_WRITE);
            int intMaxObjectsToProcess = folder.getMessageCount();
            SubjectTerm term = null;
            Message[] msgs = null;
            Message[] msgs2 = null;
            term = new SubjectTerm(objO.mailSubjectFilter.getValue());
            int intBufferSize = objO.maxMailsToProcess.value();
            if (intMaxObjectsToProcess > intBufferSize) {
                intMaxObjectsToProcess = intBufferSize;
            }
            msgs = folder.getMessages(1, intMaxObjectsToProcess);
            if (objO.mailSubjectFilter.isNotEmpty()) {
                LOGGER.debug(String.format("looking for %1$s", objO.mailSubjectFilter.getValue()));
                msgs2 = folder.search(term, msgs);
                LOGGER.debug(String.format("%1$s messages found with %2$s", msgs2.length, objO.mailSubjectFilter.getValue()));
            } else {
                msgs2 = msgs;
                LOGGER.debug(msgs2.length + " messages found, folder = " + pstrMessageFolder);
            }
            if (msgs2.length > 0) {
                for (Message objMessageElement : msgs2) {
                    if (objO.mailUseSeen.value() && objMessageElement.isSet(Flags.Flag.SEEN)) {
                        LOGGER.info("message skipped, already seen: " + objMessageElement.getSubject());
                        lngMessagesSkipped++;
                        continue;
                    }
                    try {
                        SOSMimeMessage objSOSMailItem = new SOSMimeMessage(objMessageElement, new SOSSchedulerLogger(spooler_log));
                        if (objO.mailSubjectPattern.isNotEmpty()) {
                            objO.mailSubjectPattern.setRegExpFlags(0);
                            Matcher subjectMatcher = objO.mailSubjectPattern.getPattern().matcher(objSOSMailItem.getSubject());
                            if (!subjectMatcher.find()) {
                                LOGGER.info(String.format("message skipped, subject does not match [%1$s]: %2$s", objO.mailSubjectPattern.getValue(),
                                        objSOSMailItem.getSubject()));
                                lngMessagesSkipped++;
                                continue;
                            }
                        }
                        if (objO.mailBodyPattern.isNotEmpty()) {
                            objO.mailBodyPattern.setRegExpFlags(0);
                            Matcher bodyMatcher = objO.mailBodyPattern.getPattern().matcher(objSOSMailItem.getPlainTextBody());
                            if (!bodyMatcher.find()) {
                                LOGGER.info(String.format("message skipped, body does not match [%1$s]: %2$s", objO.mailBodyPattern.getValue(),
                                        objSOSMailItem.getPlainTextBody()));
                                lngMessagesSkipped++;
                                continue;
                            }
                        }
                        executeMessage(objSOSMailItem);
                    } catch (Exception e) {
                        LOGGER.info("message skipped, exception occured: " + objMessageElement.getSubject());
                        lngMessagesSkipped++;
                        continue;
                    }
                }
            }
        } catch (Exception e) {
            if (e instanceof JobSchedulerException) {
                throw e;
            }
            throw new JobSchedulerException("Error occured querying mail server. " + e, e);
        }
    }

    private void executeCommand(final SOSMimeMessage message) throws Exception {
        String strText = message.getPlainTextBody();
        if (strText != null && !strText.isEmpty()) {
            if (isLocalScheduler) {
                LOGGER.debug("...host/port is this host and port. Using API");
                spooler.execute_xml(strText);
            } else {
                executeXml(strText);
            }
        }
    }

    private void dumpMessage(final SOSMimeMessage message) throws Exception {
        if (objO.mailDumpDir.IsEmpty()) {
            throw new JobSchedulerException("No output directory [parameter mail_dump_dir] specified.");
        }
        File messageFile = new File(objO.mailDumpDir.getValue(), message.getMessageId());
        LOGGER.debug("saving message to file: " + messageFile.getAbsolutePath());
        message.dumpMessageToFile(messageFile, true, false);
    }

    private void deleteMessage(final SOSMimeMessage message) throws Exception {
        LOGGER.debug("deleting message : " + message.getSubject() + " " + message.getSentDateAsString());
        message.deleteMessage();
    }

    private void startOrder(final SOSMimeMessage message) throws Exception {
        String jobchain = objO.mailJobchain.getValue();
        Variable_set objReturnParams = spooler.create_variable_set();
        LOGGER.debug("....merge");
        objReturnParams.merge(spooler_task.params());
        objReturnParams.set_var("mail_from", message.getFrom());
        if (message.getFromName() != null) {
            objReturnParams.set_var("mail_from_name", message.getFromName());
        } else {
            objReturnParams.set_var("mail_from_name", "");
        }
        objReturnParams.set_var("mail_message_id", message.getMessageId());
        objReturnParams.set_var("mail_subject", message.getSubject());
        objReturnParams.set_var("mail_body", message.getPlainTextBody());
        objReturnParams.set_var("mail_send_at", message.getSentDateAsString());
        Vector<SOSMailAttachment> lstAttachments = message.getSosMailAttachments();
        if (isLocalScheduler) {
            LOGGER.debug("...host/port is this host and port. Using API");
            Job_chain objJobChain = spooler.job_chain(jobchain);
            LOGGER.debug("...jobchain " + jobchain + " object created.");
            Order objNewOrder = spooler.create_order();
            objNewOrder.params().merge(objReturnParams);
            if (objO.mailOrderState.isNotEmpty()) {
                objNewOrder.set_state(objO.mailOrderState.getValue());
            }
            if (objO.mailOrderTitle.isNotEmpty()) {
                objNewOrder.set_title(objO.mailOrderTitle.getValue());
            }
            objJobChain.add_order(objNewOrder);
            LOGGER.debug("...order added to " + jobchain);
        } else {
            startOrderXML(objReturnParams);
        }
    }

    private void startOrderXML(final Variable_set params_) throws Exception {
        String id = objO.mailOrderId.getValue();
        String state = objO.mailOrderState.getValue();
        String title = objO.mailOrderTitle.getValue();
        String jobchain = objO.mailJobchain.getValue();
        LOGGER.debug("Starting order " + id + " at " + jobchain + " with xml-command");
        if (objO.mailSchedulerHost.IsEmpty()) {
            throw new Exception("Missing host while starting order.");
        }
        if (objO.mailOrderId.isNotEmpty()) {
            id = " id=\"" + id + "\"";
        }
        if (objO.mailOrderState.isNotEmpty()) {
            state = " state=\"" + state + "\"";
        }
        if (objO.mailOrderTitle.isNotEmpty()) {
            title = " title=\"" + title + "\"";
        }
        String xml = "<add_order replace=\"yes\"" + id + title + state + " job_chain=\"" + jobchain + "\"><params>";
        if (params_ != null && params_.xml() != null && !params_.xml().isEmpty()) {
            String pparamsXml = params_.xml();
            int begin = pparamsXml.indexOf("<sos.spooler.variable_set>") + 26;
            int end = pparamsXml.lastIndexOf("</sos.spooler.variable_set>");
            if (begin >= 26 && end >= 26) {
                xml += pparamsXml.substring(begin, end).replaceAll("variable", "param");
            }
        }
        xml += "</params></add_order>";
        executeXml(xml);
    }

    private void executeXml(final String xml) throws Exception {
        SOSSchedulerCommand command;
        command = new SOSSchedulerCommand(objO.mailSchedulerHost.getValue(), objO.mailSchedulerPort.value());
        command.setProtocol("udp");
        LOGGER.debug("Trying connection to " + objO.mailSchedulerHost.getValue() + ":" + objO.mailSchedulerPort.getValue());
        command.connect();
        LOGGER.debug("...connected");
        LOGGER.debug("Sending add_order command:\n" + xml);
        command.sendRequest(xml);
    }

}