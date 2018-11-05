package com.sos.jitl.checkhistory;

import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.spooler.Mail;
import sos.spooler.Spooler;

import com.sos.JSHelper.Basics.IJSCommands;
import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.jitl.checkhistory.interfaces.IJobSchedulerHistory;
import com.sos.jitl.checkhistory.interfaces.IJobSchedulerHistoryInfo;
import com.sos.jitl.restclient.ApiAccessToken;
import com.sos.jitl.restclient.WebserviceCredentials;
import com.sos.localization.Messages;

@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
public class JobSchedulerCheckHistory extends JSToolBox implements JSJobUtilities, IJSCommands {

    private final String conClassName = "JobSchedulerCheckRunHistory";
    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerCheckHistory.class);
    protected JobSchedulerCheckHistoryOptions objOptions = null;
    private JSJobUtilities objJSJobUtilities = this;
    private IJSCommands objJSCommands = this;
    private String historyObjectName = "";
    private String pathOfJob = "";

    public void setPathOfJob(String _pathOfJob) {
        this.pathOfJob = _pathOfJob;
    }

    public JobSchedulerCheckHistory() {
        super();
        Messages = new Messages("com_sos_scheduler_messages");
    }

    public JobSchedulerCheckHistoryOptions options() {
        if (objOptions == null) {
            objOptions = new JobSchedulerCheckHistoryOptions();
        }
        return objOptions;
    }

    private IJobSchedulerHistory getHistoryObject(Spooler schedulerInstance) throws Exception {
        String xAccessToken = null;
        String jocUrl = null;
        String schedulerInstanceId = "";

        if (schedulerInstance != null) {
            xAccessToken = schedulerInstance.variables().value("X-Access-Token");
            jocUrl = schedulerInstance.variables().value("joc_url");
            schedulerInstanceId = schedulerInstance.id();
        } 
         

        if (jocUrl == null) {
            jocUrl = "";
        }

        if (options().jocUrl.isDirty()) {
            jocUrl = options().jocUrl.getValue();
        }

        WebserviceCredentials webserviceCredentials = new WebserviceCredentials();
        webserviceCredentials.setPassword(options().password.getValue());
        webserviceCredentials.setUser(options().user.getValue());
        webserviceCredentials.setSchedulerId(schedulerInstanceId);

        ApiAccessToken apiAccessToken = new ApiAccessToken(jocUrl);

        if (schedulerInstance != null) {
            jocUrl = schedulerInstance.variables().value("joc_url");
            apiAccessToken.setJocUrl(jocUrl);
            xAccessToken = schedulerInstance.variables().value("X-Access-Token");
        }
        
        if (xAccessToken == null) {
            xAccessToken = "";
        }

        if (!apiAccessToken.isValidAccessToken(xAccessToken)) {
            throw new Exception("no valid access token found");
        }

        if (options().user.isNotDirty() || options().password.isNotDirty()) {
            webserviceCredentials.setAccessToken(xAccessToken);
        }

        IJobSchedulerHistory jobSchedulerHistory;
        LOGGER.debug("Get answer from JOC instance:" + jocUrl);
        if (options().getJobChainName().getValue().isEmpty()) {
            historyObjectName = options().getJobName().getValue();
            jobSchedulerHistory = new JobHistory(jocUrl, webserviceCredentials);
        } else {
            historyObjectName = options().getJobChainName().getValue();
            jobSchedulerHistory = new JobChainHistory(jocUrl, webserviceCredentials);

        }

        jobSchedulerHistory.setRelativePath(pathOfJob);
        return jobSchedulerHistory;
    }

    public JobSchedulerCheckHistory Execute() throws Exception {
        final String conMethodName = conClassName + "::Execute";
        LOGGER.debug(String.format(Messages.getMsg("JSJ-I-110"), conMethodName));
        boolean result = false;
        options().checkMandatory();
        LOGGER.debug(options().toString());
        try {
            String startTime = "00:00:00";
            String endTime = "00:00:00";
            String query = options().query.getValue();
            String[] queries = query.split("(;|,)");
            HistoryHelper jobHistoryHelper = new HistoryHelper();
            String methodName = jobHistoryHelper.getMethodName(options().query.getValue());
            if (options().start_time.isDirty()) {
                startTime = options().start_time.getValue();
            }
            if (options().end_time.isDirty()) {
                endTime = options().end_time.getValue();
            }
            String message = options().message.getValue();
            String mailTo = options().mailTo.getValue();
            String mailCc = options().mailCC.getValue();
            String mailBcc = options().mail_bcc.getValue();
            Spooler schedulerInstance = (Spooler) objJSCommands.getSpoolerObject();
            IJobSchedulerHistory jobHistory = getHistoryObject(schedulerInstance);
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
            if ("isCompletedBefore".equalsIgnoreCase(methodName) || "isCompletedSuccessfulBefore".equalsIgnoreCase(methodName)
                    || "isCompletedWithErrorBefore".equalsIgnoreCase(methodName)) {
                String time = jobHistoryHelper.getParameter(endTime, query);
                jobHistory.setTimeLimit(time);
            }
            String[] historyObjectNames = historyObjectName.split("(;|,)");
            for (int i = 0; i < historyObjectNames.length; i++) {
                String actHistoryObjectName = historyObjectNames[i];
                actHistoryObjectName = Matcher.quoteReplacement(actHistoryObjectName);
                String strTemp = message.replaceAll("(?im)\\[?JOB_NAME\\]?", actHistoryObjectName);
                message = Messages.getMsg("JCH_T_0001", actHistoryObjectName, strTemp);
                IJobSchedulerHistoryInfo jobSchedulerHistoryInfo = jobHistory.getJobSchedulerHistoryInfo(actHistoryObjectName);
                actHistoryObjectName = jobHistory.getActHistoryObjectName();
                jobSchedulerHistoryInfo.setEndTime(endTime);
                jobSchedulerHistoryInfo.setStartTime(startTime);
                String actQuery = "";
                if (queries.length == 1) {
                    actQuery = query;
                } else if (queries.length > i) {
                    actQuery = queries[i];
                }
                result = jobSchedulerHistoryInfo.queryHistory(actQuery);
                LOGGER.debug("--->" + actQuery + "(" + actHistoryObjectName + ")=" + result);
                options().result.value(result);
                if (jobSchedulerHistoryInfo.getLastCompleted().error == 0) {
                    if (jobSchedulerHistoryInfo.getLastCompleted().found) {
                        LOGGER.info(Messages.getMsg("JCH_I_0001", actHistoryObjectName, jobSchedulerHistoryInfo.getLastCompleted().end, ""));
                    }
                    if (jobSchedulerHistoryInfo.getLastCompletedWithError().found) {
                        LOGGER.info(Messages.getMsg("JCH_I_0003", actHistoryObjectName, jobSchedulerHistoryInfo.getLastCompletedWithError().end,
                                jobSchedulerHistoryInfo.getLastCompletedWithError().errorMessage));
                    } else {
                        LOGGER.info(Messages.getMsg("JCH_I_0006", actHistoryObjectName));
                    }
                } else {
                    if (jobSchedulerHistoryInfo.getLastCompleted().found) {
                        LOGGER.info(Messages.getMsg("JCH_I_0002", actHistoryObjectName, jobSchedulerHistoryInfo.getLastCompleted().end,
                                jobSchedulerHistoryInfo.getLastCompleted().errorMessage));
                    }
                    if (jobSchedulerHistoryInfo.getLastCompletedSuccessful().found) {
                        LOGGER.info(Messages.getMsg("JCH_I_0004", actHistoryObjectName, jobSchedulerHistoryInfo.getLastCompletedSuccessful().end));
                    } else {
                        LOGGER.info(Messages.getMsg("JCH_I_0005", actHistoryObjectName));
                    }
                }
                if (!result) {
                    message = message + " " + methodName + "=false";
                    if ("true".equals(options().failOnQueryResultFalse.getValue())) {
                        LOGGER.error(message);
                        throw new JobSchedulerException(message);
                    } else {
                        LOGGER.info(message);
                    }
                } else {
                    message = message + " " + methodName + "=true";
                    if (options().failOnQueryResultTrue.value()) {
                        LOGGER.error(message);
                        throw new JobSchedulerException(message);
                    } else {
                        LOGGER.info(message);
                    }
                }
            }
        } catch (JobSchedulerException ee) {
            LOGGER.error(Messages.getMsg("JSJ-F-107", conMethodName) + ":" + ee.getMessage());
            throw ee;
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(Messages.getMsg("JSJ-F-107", conMethodName) + ":" + e.toString(), e);
            throw e;
        }

        return this;
    }

    @Override
    public String replaceSchedulerVars(final String pstrString2Modify) {
        LOGGER.debug("replaceSchedulerVars as Dummy-call executed. No Instance of JobUtilites specified.");
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
        LOGGER.debug("objJSJobUtilities = " + objJSJobUtilities.getClass().getName());
    }

    public void setJSCommands(final IJSCommands pobjJSCommands) {
        if (pobjJSCommands == null) {
            objJSCommands = this;
        } else {
            objJSCommands = pobjJSCommands;
            LOGGER.debug("pobjJSCommands = " + pobjJSCommands.getClass().getName());
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

}