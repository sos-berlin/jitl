package com.sos.jitl.mail.smtp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import sos.net.SOSMail;
import sos.scheduler.command.SOSSchedulerCommand;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.objects.Spooler;

public class JSSmtpMailClient extends JSJobUtilitiesClass<JSSmtpMailOptions> {

    private static final Logger LOGGER = Logger.getLogger(JSSmtpMailClient.class);
    private static final String MESSAGE_FILE_PATH = "com_sos_scheduler_messages";

    public JSSmtpMailClient() {
        super(new JSSmtpMailOptions());
        this.setMessageResource(MESSAGE_FILE_PATH);
    }

    @Override
    public JSSmtpMailOptions getOptions() {
        if (objOptions == null) {
            objOptions = new JSSmtpMailOptions();
        }
        return objOptions;
    }

    public JSSmtpMailClient Execute() throws Exception {
        JSSmtpMailOptions objO = objOptions;
        Execute(objO);
        return this;
    }

    public JSSmtpMailClient Execute(final JSSmtpMailOptions pobjOptions) throws Exception {
        if (pobjOptions != null && pobjOptions.FileNotificationTo.isDirty()) {
            try {
                boolean useCurrentTaskLog = !pobjOptions.job_name.isDirty() && !pobjOptions.job_id.isDirty();
                if (pobjOptions.tasklog_to_body.value()) {
                    if (useCurrentTaskLog) {
                        pobjOptions.job_name.setValue(pobjOptions.getCurrentJobFolder(), pobjOptions.getCurrentJobName());
                        pobjOptions.job_id.value(pobjOptions.getCurrentJobId());
                    }
                    Object objSp = getSpoolerObject();
                    if (isNotNull(objSp)) {
                        sos.spooler.Spooler objSpooler = (sos.spooler.Spooler) objSp;
                        if (!pobjOptions.scheduler_host.isDirty()) {
                            pobjOptions.scheduler_host.setValue(objSpooler.hostname());
                        }
                        if (!pobjOptions.scheduler_port.isDirty()) {
                            pobjOptions.scheduler_port.value(SOSSchedulerCommand.getHTTPPortFromScheduler(objSpooler));
                        }
                    }
                    pobjOptions.job_name.isMandatory(true);
                    pobjOptions.job_id.isMandatory(true);
                    pobjOptions.scheduler_host.isMandatory(true);
                    pobjOptions.scheduler_port.isMandatory(true);
                }
                pobjOptions.checkMandatory();
                String log = "";
                if (pobjOptions.tasklog_to_body.value()) {
                    log =
                            getTaskLog(pobjOptions.job_name.getValue(), pobjOptions.job_id.value(), pobjOptions.scheduler_host.getValue(),
                                    pobjOptions.scheduler_port.value(), useCurrentTaskLog);
                }
                if (!pobjOptions.subject.isDirty()) {
                    String strT = "SOSJobScheduler: ${JobName} - ${JobTitle} - CC ${CC} ";
                    pobjOptions.subject.setValue(strT);
                }
                String strM = pobjOptions.subject.getValue();
                pobjOptions.subject.setValue(objJSJobUtilities.replaceSchedulerVars(strM));
                strM = pobjOptions.body.getValue();
                strM = pobjOptions.replaceVars(strM);
                Pattern pattern = Pattern.compile("[?%]log[?%]|[$%]\\{log\\}", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(strM);
                if (matcher.find()) {
                    strM = matcher.replaceAll(log);
                } else {
                    strM += "\n" + log;
                }
                pobjOptions.body.setValue(strM);
                if (!pobjOptions.from.isDirty()) {
                    pobjOptions.from.setValue("JobScheduler@sos-berlin.com");
                }
                SOSMail objMail = new SOSMail(pobjOptions.host.getValue());
                LOGGER.debug(pobjOptions.dirtyString());
                objMail.sendMail(pobjOptions);
            } catch (Exception e) {
                throw new JobSchedulerException(e.getMessage(), e);
            }
        }
        return this;
    }

    private String getTaskLog(final String strJobName, final int intTaskId, final String strJSHost, final int intJSPort,
            final boolean bUseCurrentTaskLog) {
        String log = null;
        try {
            SchedulerObjectFactory objSchedulerObjectFactory = new SchedulerObjectFactory(strJSHost, intJSPort);
            objSchedulerObjectFactory.initMarshaller(Spooler.class);
            log = objSchedulerObjectFactory.getTaskLog(strJobName, intTaskId, bUseCurrentTaskLog);
        } catch (Exception e) {
            LOGGER.error(Messages.getMsg("JSJ_W_0001", strJobName, intTaskId, strJSHost, intJSPort), e);
            log = "";
        }
        if (log == null) {
            LOGGER.error(Messages.getMsg("JSJ_W_0001", strJobName, intTaskId, strJSHost, intJSPort));
            log = "";
        }
        return log;
    }

}