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
            if (!objO.mail_scheduler_host.isDirty()) {
                objO.mail_scheduler_host.Value(spooler.hostname());
                if (!objO.mail_scheduler_port.isDirty()) {
                    objO.mail_scheduler_port.value(spooler.tcp_port());
                }
            }
            if (objO.mail_use_seen.value()) {
                objO.mail_set_seen.value(true);
            }
            objO.CheckMandatory();

            isLocalScheduler =
                    objO.mail_scheduler_host.Value().equalsIgnoreCase(spooler.hostname()) && objO.mail_scheduler_port.value() == spooler.tcp_port();
            dteMinAge = null;
            flgCheckdate = false;
            if (objO.MinAge.isDirty()) {
                String strT = objO.MinAge.Value();
                if (!strT.startsWith("-")) {
                    objO.MinAge.Value("-" + strT);
                }
                dteMinAge = objO.MinAge.getEndFromNow();
                LOGGER.info(String.format("Min Age defined: %1$s", dteMinAge.toLocaleString()));
                flgCheckdate = true;
            }
            String strMailHost = objO.mail_host.Value();
            int intMailPort = objO.mail_port.value();
            String strServerType = objO.mail_server_type.Value();
            LOGGER.debug(String.format("Connecting to Mailserver %1$s:%2$d (%3$s)...", strMailHost, intMailPort, strServerType));
            objMailReader = new SOSMailReceiver(objO.mail_host.Value(), objO.mail_port.Value(), objO.mail_user.Value(), objO.mailPassword.Value());
            objMailReader.setLogger(new SOSSchedulerLogger(spooler_log));
            if (objO.mail_server_timeout.value() > 0) {
                objMailReader.setTimeout(objO.mail_server_timeout.value());
            }
            objMailReader.connect(objO.mail_server_type.Value());
            for (String strMailFolderName : objO.mail_message_folder.Value().split("[,|;]")) {
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
        if (flgCheckdate && messageDate != null) {
            if (dteMinAge.before(messageDate)) {
                LOGGER.debug("message skipped due to date constraint: \n" + sosMimeMessage.getSubject() + " " + messageDate);
                lngMessagesSkipped++;
                result = false;
            }
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
        StringTokenizer t = new StringTokenizer(objO.mail_action.Value(), ",");
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
            if (objO.mail_set_seen.value()) {
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
            term = new SubjectTerm(objO.mail_subject_filter.Value());
            int intBufferSize = objO.max_mails_to_process.value();
            if (intMaxObjectsToProcess > intBufferSize) {
                intMaxObjectsToProcess = intBufferSize;
            }
            msgs = folder.getMessages(1, intMaxObjectsToProcess);
            if (objO.mail_subject_filter.IsNotEmpty()) {
                LOGGER.debug(String.format("looking for %1$s", objO.mail_subject_filter.Value()));
                msgs2 = folder.search(term, msgs);
                LOGGER.debug(String.format("%1$s messages found with %2$s", msgs2.length, objO.mail_subject_filter.Value()));
            } else {
                msgs2 = msgs;
                LOGGER.debug(msgs2.length + " messages found, folder = " + pstrMessageFolder);
            }
            if (msgs2.length > 0) {
                for (Message objMessageElement : msgs2) {
                    if (objO.mail_use_seen.value() && objMessageElement.isSet(Flags.Flag.SEEN)) {
                        LOGGER.info("message skipped, already seen: " + objMessageElement.getSubject());
                        lngMessagesSkipped++;
                        continue;
                    }
                    try {
                        SOSMimeMessage objSOSMailItem = new SOSMimeMessage(objMessageElement, new SOSSchedulerLogger(spooler_log));
                        if (objO.mail_subject_pattern.IsNotEmpty()) {
                            objO.mail_subject_pattern.setRegExpFlags(0);
                            Matcher subjectMatcher = objO.mail_subject_pattern.getPattern().matcher(objSOSMailItem.getSubject());
                            if (!subjectMatcher.find()) {
                                LOGGER.info(String.format("message skipped, subject does not match [%1$s]: %2$s", objO.mail_subject_pattern.Value(),
                                        objSOSMailItem.getSubject()));
                                lngMessagesSkipped++;
                                continue;
                            }
                        }
                        if (objO.mail_body_pattern.IsNotEmpty()) {
                            objO.mail_body_pattern.setRegExpFlags(0);
                            Matcher bodyMatcher = objO.mail_body_pattern.getPattern().matcher(objSOSMailItem.getPlainTextBody());
                            if (!bodyMatcher.find()) {
                                LOGGER.info(String.format("message skipped, body does not match [%1$s]: %2$s", objO.mail_body_pattern.Value(),
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
        if (objO.mail_dump_dir.IsEmpty()) {
            throw new JobSchedulerException("No output directory [parameter mail_dump_dir] specified.");
        }
        File messageFile = new File(objO.mail_dump_dir.Value(), message.getMessageId());
        LOGGER.debug("saving message to file: " + messageFile.getAbsolutePath());
        message.dumpMessageToFile(messageFile, true, false);
    }

    private void deleteMessage(final SOSMimeMessage message) throws Exception {
        LOGGER.debug("deleting message : " + message.getSubject() + " " + message.getSentDateAsString());
        message.deleteMessage();
    }

    private void startOrder(final SOSMimeMessage message) throws Exception {
        String jobchain = objO.mail_jobchain.Value();
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
            if (objO.mail_order_state.IsNotEmpty()) {
                objNewOrder.set_state(objO.mail_order_state.Value());
            }
            if (objO.mail_order_title.IsNotEmpty()) {
                objNewOrder.set_title(objO.mail_order_title.Value());
            }
            objJobChain.add_order(objNewOrder);
            LOGGER.debug("...order added to " + jobchain);
        } else {
            startOrderXML(objReturnParams);
        }
    }

    private void startOrderXML(final Variable_set params_) throws Exception {
        String id = objO.mail_order_id.Value();
        String state = objO.mail_order_state.Value();
        String title = objO.mail_order_title.Value();
        String jobchain = objO.mail_jobchain.Value();
        LOGGER.debug("Starting order " + id + " at " + jobchain + " with xml-command");
        if (objO.mail_scheduler_host.IsEmpty()) {
            throw new Exception("Missing host while starting order.");
        }
        if (objO.mail_order_id.IsNotEmpty()) {
            id = " id=\"" + id + "\"";
        }
        if (objO.mail_order_state.IsNotEmpty()) {
            state = " state=\"" + state + "\"";
        }
        if (objO.mail_order_title.IsNotEmpty()) {
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
        command = new SOSSchedulerCommand(objO.mail_scheduler_host.Value(), objO.mail_scheduler_port.value());
        command.setProtocol("udp");
        LOGGER.debug("Trying connection to " + objO.mail_scheduler_host.Value() + ":" + objO.mail_scheduler_port.Value());
        command.connect();
        LOGGER.debug("...connected");
        LOGGER.debug("Sending add_order command:\n" + xml);
        command.sendRequest(xml);
    }

}
