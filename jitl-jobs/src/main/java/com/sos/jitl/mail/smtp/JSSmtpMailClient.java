package com.sos.jitl.mail.smtp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import sos.net.SOSMail;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.objects.Spooler;

@SuppressWarnings("deprecation")
public class JSSmtpMailClient extends JSJobUtilitiesClass<JSSmtpMailOptions> {

    private final String conClassName = "JSMailClient";
    private static Logger logger = Logger.getLogger(JSSmtpMailClient.class);
    @SuppressWarnings("unused")
    private final String conSVNVersion = "$Id: JSMailClient.java 18220 2012-10-18 07:46:10Z kb $";
    private final String conMessageFilePath = "com_sos_scheduler_messages";

    /** \brief JSMailClient
     *
     * \details */
    public JSSmtpMailClient() {
        super();
        this.getOptions();
        this.setMessageResource(conMessageFilePath);
    }

    /** \brief Options - returns the JSMailClientOptionClass
     *
     * \details The JSMailClientOptionClass is used as a Container for all
     * Options (Settings) which are needed.
     *
     * \return JSMailClientOptions */
    @Override
    public JSSmtpMailOptions getOptions() {

        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::Options"; //$NON-NLS-1$

        if (objOptions == null) {
            objOptions = new JSSmtpMailOptions();
        }
        return objOptions;
    }

    /** \brief Execute - Start the Execution of JSMailClient
     *
     * \details
     *
     * For more details see
     *
     * \see JobSchedulerAdapterClass \see JSMailClientMain
     *
     * \return JSMailClient
     *
     * @return */
    public JSSmtpMailClient Execute() throws Exception {

        JSSmtpMailOptions objO = objOptions;
        Execute(objO);

        return this;
    }

    public JSSmtpMailClient Execute(final JSSmtpMailOptions pobjOptions) throws Exception {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::Execute";

        if (pobjOptions != null && pobjOptions.FileNotificationTo.isDirty() == true) {
            try {
                // String strA = "";
                // if (objOptions.log_filename.isDirty() == true) {
                // String strF = objOptions.log_filename.getHtmlLogFileName();
                // if (strF.length() > 0) {
                // strA += strF;
                // }
                //
                // strF = objOptions.log_filename.Value();
                // if (strF.length() > 0) {
                // if (strA.length() > 0) {
                // strA += ";";
                // }
                // strA += strF;
                // }
                // if (strA.length() > 0) {
                // objOptions.attachment.Value(strA);
                // }
                // }

                // TODO useCurrentTaskLog besser option statt implizit
                boolean useCurrentTaskLog = pobjOptions.job_name.isDirty() == false && pobjOptions.job_id.isDirty() == false;
                if (pobjOptions.tasklog_to_body.value() == true) {
                    if (useCurrentTaskLog == true) {
                        pobjOptions.job_name.Value(pobjOptions.CurrentJobFolder(), pobjOptions.CurrentJobName());
                        pobjOptions.job_id.value(pobjOptions.CurrentJobId());
                    }
                    Object objSp = getSpoolerObject();
                    if (isNotNull(objSp)) {
                        sos.spooler.Spooler objSpooler = (sos.spooler.Spooler) objSp;
                        if (pobjOptions.scheduler_host.isDirty() == false) {
                            // TODO Maybe 'localhost' instead of
                            // objSpooler.hostname()
                            pobjOptions.scheduler_host.Value(objSpooler.hostname());
                        }
                        if (pobjOptions.scheduler_port.isDirty() == false) {
                            pobjOptions.scheduler_port.value(objSpooler.tcp_port());
                        }
                    }
                    pobjOptions.job_name.isMandatory(true);
                    pobjOptions.job_id.isMandatory(true);
                    pobjOptions.scheduler_host.isMandatory(true);
                    pobjOptions.scheduler_port.isMandatory(true);
                }

                pobjOptions.CheckMandatory();

                String log = "";
                if (pobjOptions.tasklog_to_body.value() == true) {
                    log =
                            getTaskLog(pobjOptions.job_name.Value(), pobjOptions.job_id.value(), pobjOptions.scheduler_host.Value(),
                                    pobjOptions.scheduler_port.value(), useCurrentTaskLog);
                }

                if (pobjOptions.subject.isDirty() == false) {
                    String strT = "SOSJobScheduler: ${JobName} - ${JobTitle} - CC ${CC} ";
                    pobjOptions.subject.Value(strT);
                }

                String strM = pobjOptions.subject.Value();
                pobjOptions.subject.Value(pobjOptions.replaceVars(strM));

                strM = pobjOptions.body.Value();
                strM = pobjOptions.replaceVars(strM);

                Pattern pattern = Pattern.compile("[?%]log[?%]|[$%]\\{log\\}", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(strM);
                if (matcher.find() == true) {
                    strM = matcher.replaceAll(log);
                } else {
                    strM += "\n" + log;
                }
                pobjOptions.body.Value(strM);
                if (pobjOptions.from.isDirty() == false) {
                    pobjOptions.from.Value("JobScheduler@sos-berlin.com");
                }

                SOSMail objMail = new SOSMail(pobjOptions.host.Value());
                logger.debug(pobjOptions.dirtyString());
                objMail.sendMail(pobjOptions);
            } catch (Exception e) {
                throw new JobSchedulerException(e.getLocalizedMessage(), e);
            }
        }

        return this;
    }

    public void init() {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::init";
        doInitialize();
    }

    private void doInitialize() {
    } // doInitialize

    /** \brief getTaskLog
     *
     * \details
     *
     * \return log content from given TaskId
     *
     * @param strJobName
     * @param strTaskId */
    private String getTaskLog(final String strJobName, final int intTaskId, final String strJSHost, final int intJSPort,
            final boolean bUseCurrentTaskLog) {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::getTaskLog";

        String log = null;
        try {
            SchedulerObjectFactory objSchedulerObjectFactory = new SchedulerObjectFactory(strJSHost, intJSPort);
            objSchedulerObjectFactory.initMarshaller(Spooler.class);

            log = objSchedulerObjectFactory.getTaskLog(strJobName, intTaskId, bUseCurrentTaskLog);
        } catch (Exception e) {
            logger.error(Messages.getMsg("JSJ_W_0001", strJobName, intTaskId, strJSHost, intJSPort), e);
            log = "";
        }

        if (log == null) {
            logger.error(Messages.getMsg("JSJ_W_0001", strJobName, intTaskId, strJSHost, intJSPort));
            log = "";
        }

        return log;

    } // getTaskLog

} // class JSMailClient