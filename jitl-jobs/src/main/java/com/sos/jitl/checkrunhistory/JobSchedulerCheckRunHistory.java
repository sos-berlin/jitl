package com.sos.jitl.checkrunhistory;

import java.util.Locale;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;

import sos.spooler.Mail;
import sos.spooler.Spooler;

import com.sos.JSHelper.Basics.IJSCommands;
import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.jitl.checkrunhistory.JobHistory;
import com.sos.jitl.checkrunhistory.JobHistoryHelper;
import com.sos.localization.Messages;

@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
public class JobSchedulerCheckRunHistory extends JSToolBox implements JSJobUtilities, IJSCommands {

    private final String conClassName = "JobSchedulerCheckRunHistory";						//$NON-NLS-1$
    private static Logger logger = Logger.getLogger(JobSchedulerCheckRunHistory.class);
    protected JobSchedulerCheckRunHistoryOptions objOptions = null;
    private JSJobUtilities objJSJobUtilities = this;
    private IJSCommands objJSCommands = this;
    private String historyObjectName = "";

    public JobSchedulerCheckRunHistory() {
        super();
        Messages = new Messages("com_sos_scheduler_messages", Locale.getDefault());
    }

    public JobSchedulerCheckRunHistoryOptions options() {
        if (objOptions == null) {
            objOptions = new JobSchedulerCheckRunHistoryOptions();
        }
        return objOptions;
    }

    private IJobSchedulerHistory getHistoryObject(Spooler schedulerInstance) {
        if (options().schedulerPort.value() == 0 && options().schedulerHostName.Value().length() == 0) {
            logger.debug("Get answer from JobScheduler instance");
            if (options().getJobChainName().Value().length() == 0) {
                historyObjectName = options().getJobName().Value();
                return new JobHistory(schedulerInstance);
            } else {
                historyObjectName = options().getJobChainName().Value();
                return new JobChainHistory(schedulerInstance);
            }
        } else {
            logger.debug(String.format("Get answer from %s:%s", options().schedulerHostName.Value(), options().schedulerPort.value()));
            if (options().getJobChainName().Value().length() == 0) {
                historyObjectName = options().getJobName().Value();
                return new JobHistory(options().schedulerHostName.Value(), options().schedulerPort.value());
            } else {
                historyObjectName = options().getJobChainName().Value();
                return new JobChainHistory(options().schedulerHostName.Value(), options().schedulerPort.value());
            }
        }
    }

    public JobSchedulerCheckRunHistory Execute() throws Exception {
        final String conMethodName = conClassName + "::Execute"; //$NON-NLS-1$
        logger.debug(String.format(Messages.getMsg("JSJ-I-110"), conMethodName));
        boolean result = false;

        options().CheckMandatory();
        logger.debug(options().toString());

        try {

            String startTime = "00:00:00";
            String endTime = "00:00:00";

            String query = options().query.Value();

            JobHistoryHelper jobHistoryHelper = new JobHistoryHelper();
            String methodName = jobHistoryHelper.getMethodName(options().query.Value());

            if (options().start_time.isDirty()) {
                startTime = options().start_time.Value();
            }

            if (options().end_time.isDirty()) {
                endTime = options().end_time.Value();
            }

            String message = options().message.Value();
            String mailTo = options().mail_to.Value();
            String mailCc = options().mail_cc.Value();
            String mailBcc = options().mail_bcc.Value();
            
            historyObjectName = Matcher.quoteReplacement(historyObjectName);
            String strTemp = message.replaceAll("(?im)\\[?JOB_NAME\\]?", historyObjectName);


            message = Messages.getMsg("JCH_T_0001", historyObjectName, strTemp);

            Spooler schedulerInstance = (Spooler) objJSCommands.getSpoolerObject();

            if (schedulerInstance != null) {
                Mail mail = schedulerInstance.log().mail();
                if (isNotEmpty(mailTo)) {
                    mail.set_to(mailTo);
                }
                if (isNotEmpty(mailCc)) {
                    mail.set_cc(mailCc);
                }
                if (isNotEmpty(mailBcc)) {
                    mail.set_bcc(mailBcc);
                }
                if (isNotEmpty(message)) {
                    mail.set_subject(message);
                }
            }

            IJobSchedulerHistory jobHistory = getHistoryObject(schedulerInstance);

            if (methodName.equalsIgnoreCase("isCompletedBefore") || methodName.equalsIgnoreCase("isCompletedSuccessfulBefore")
                    || methodName.equalsIgnoreCase("isCompletedWithErrorBefore")) {
                String time = jobHistoryHelper.getTime(endTime, query);
                jobHistory.setTimeLimit(time);
            }

            IJobSchedulerHistoryInfo jobSchedulerHistoryInfo = jobHistory.getJobSchedulerHistoryInfo(historyObjectName);

            jobSchedulerHistoryInfo.setEndTime(endTime);
            jobSchedulerHistoryInfo.setStartTime(startTime);

            result = jobSchedulerHistoryInfo.queryHistory(query);

            options().result.value(result);
            options().numberOfCompleted.value(jobHistory.getNumberOfCompleted());
            options().numberOfCompletedSuccessful.value(jobHistory.getNumberOfCompletedSuccessful());
            options().numberOfCompletedWithError.value(jobHistory.getNumberOfCompletedWithError());
            options().numberOfStarts.value(jobHistory.getCount());

            if (jobSchedulerHistoryInfo.getLastCompleted().error == 0) {
                logger.info(Messages.getMsg("JCH_I_0001", historyObjectName, jobSchedulerHistoryInfo.getLastCompleted().end, ""));

                if (jobSchedulerHistoryInfo.getLastCompletedWithError().found) {
                    logger.info(Messages.getMsg("JCH_I_0003", historyObjectName, jobSchedulerHistoryInfo.getLastCompletedSuccessful().end, jobSchedulerHistoryInfo.getLastCompletedWithError().errorMessage));
                } else {
                    logger.info(Messages.getMsg("JCH_I_0006", historyObjectName));
                }
            } else {
                logger.info(Messages.getMsg("JCH_I_0002", historyObjectName, jobSchedulerHistoryInfo.getLastCompleted().end, jobSchedulerHistoryInfo.getLastCompleted().errorMessage));

                if (jobSchedulerHistoryInfo.getLastCompletedSuccessful().found) {
                    logger.info(Messages.getMsg("JCH_I_0004", historyObjectName, jobSchedulerHistoryInfo.getLastCompletedSuccessful().end));
                } else {
                    logger.info(Messages.getMsg("JCH_I_0005", historyObjectName));
                }
            }

            if (!result) {
                message = message + " " + methodName + "=false";
                if (options().failOnQueryResultFalse.Value().equals("true")) {
                    logger.error(message);
                    throw new JobSchedulerException(message);
                } else {
                    logger.info(message);
                }
            } else {
                message = message + " " + methodName + "=true";
                if (options().failOnQueryResultTrue.value()) {
                    logger.error(message);
                    throw new JobSchedulerException(message);
                } else {
                    logger.info(message);
                }

            }

        } catch (Exception e) {
            logger.error(Messages.getMsg("JSJ-F-107", conMethodName), e);
            throw e;
        }
        return this;
    }

 
    @Override
    public String replaceSchedulerVars(final boolean isWindows, final String pstrString2Modify) {
        logger.debug("replaceSchedulerVars as Dummy-call executed. No Instance of JobUtilites specified.");
        return pstrString2Modify;
    }

    @Override
    public void setJSParam(final String pstrKey, final String pstrValue) {
    }

    @Override
    public void setJSParam(final String pstrKey, final StringBuffer pstrValue) {
    }

    @Override
    public void setJSJobUtilites(final JSJobUtilities pobjJSJobUtilities) {
        if (pobjJSJobUtilities == null) {
            objJSJobUtilities = this;
        } else {
            objJSJobUtilities = pobjJSJobUtilities;
        }
        logger.debug("objJSJobUtilities = " + objJSJobUtilities.getClass().getName());
    }

    public void setJSCommands(final IJSCommands pobjJSCommands) {
        if (pobjJSCommands == null) {
            objJSCommands = this;
        } else {
            objJSCommands = pobjJSCommands;
            logger.debug("pobjJSCommands = " + pobjJSCommands.getClass().getName());
        }
    }

    @Override
    public String getCurrentNodeName() {
        return null;
    }

    @Override
    public Object getSpoolerObject() {
        return null;
    }

    @Override
    public String executeXML(final String pstrJSXmlCommand) {
        return null;
    }

    @Override
    public void setStateText(final String pstrStateText) {
    }

    @Override
    public void setCC(final int pintCC) {
    }

    @Override
    public void setNextNodeState(final String pstrNodeName) {
    }

} // class JobSchedulerCheckRunHistory