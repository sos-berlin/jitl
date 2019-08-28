package com.sos.jitl.reporting.plugin;

import java.nio.file.Path;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.classes.plugin.PluginMailer;
import com.sos.jitl.notification.db.DBLayer;
import com.sos.jitl.notification.db.DBLayerSchedulerMon;
import com.sos.jitl.notification.helper.NotificationReportExecution;
import com.sos.jitl.notification.jobs.history.CheckHistoryJobOptions;
import com.sos.jitl.notification.model.history.CheckHistoryModel;
import com.sos.jitl.reporting.db.DBItemReportExecution;
import com.sos.jitl.reporting.db.DBItemReportTask;
import com.sos.jitl.reporting.db.DBItemReportTrigger;
import com.sos.jitl.reporting.db.DBLayerReporting;
import com.sos.jitl.reporting.helper.ReportUtil;

import sos.util.SOSString;

public class FactNotificationPlugin {

    private static Logger LOGGER = LoggerFactory.getLogger(FactNotificationPlugin.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private final String className = FactNotificationPlugin.class.getSimpleName();
    private static final String SCHEMA_PATH = "notification/SystemMonitorNotification_v1.0.xsd";
    private CheckHistoryModel model;
    private PluginMailer mailer = null;
    private SOSHibernateSession session = null;
    private CheckHistoryJobOptions options = null;
    // private boolean hasModelInitError = false;
    private boolean skipExecuteChecks = false;

    public void init(SOSHibernateSession sess, PluginMailer pluginMailer, Path configDir) {
        CheckHistoryJobOptions opt = new CheckHistoryJobOptions();
        opt.schema_configuration_file.setValue(configDir.resolve(SCHEMA_PATH).toString());
        mailer = pluginMailer;
        session = sess;
        options = opt;
    }

    public void process(NotificationReportExecution item, boolean checkJobChains, boolean checkJobs) {
        String method = "process";
        if (skipExecuteChecks) {
            return;
        }

        if (item.getJobName() != null && item.getJobName().equals(DBLayerReporting.TRIGGER_RESULT_IGNORED_JOB_BASENAME)) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[skip][%s]%s", DBLayerReporting.TRIGGER_RESULT_IGNORED_JOB_BASENAME, SOSString.toString(item)));
            }
            return;
        }

        if (model == null) {
            try {
                skipExecuteChecks = false;
                model = new CheckHistoryModel(session, options);
                model.init();
            } catch (Exception e) {
                skipExecuteChecks = true;
                Exception ex = new Exception(String.format("skip notification processing due errors: %s", e.toString()), e);
                LOGGER.error(String.format("%s.%s %s", className, method, ex.toString()), e);
                mailer.sendOnError(className, method, ex);
                return;
            }
        }

        if (!model.executeChecks()) {
            skipExecuteChecks = true;
            return;
        }

        try {
            model.process(item, checkJobChains, checkJobs);
        } catch (Exception e) {
            LOGGER.error(String.format("[%s]%s", method, e.toString()), e);
            mailer.sendOnError(className, method, e);
        }
    }

    public int setOrderEndTime(String schedulerId, Long orderHistoryId, Date orderEndTime) {
        String method = "setOrderEndTime";

        int result = -1;
        if (skipExecuteChecks || orderEndTime == null) {
            return result;
        }

        DBLayerSchedulerMon dbLayer = model == null ? new DBLayerSchedulerMon(session) : model.getDbLayer();
        try {
            result = dbLayer.setNotificationsOrderEndTime(schedulerId, orderHistoryId, orderEndTime);
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][%s][%s][%s]updated=%s", method, schedulerId, orderHistoryId, ReportUtil.getDateAsString(
                        orderEndTime), result));
            }
        } catch (Exception ex) {
            LOGGER.error(String.format("[%s]%s", method, ex.toString()), ex);
            mailer.sendOnError(className, method, ex);
        }

        return result;
    }

    public NotificationReportExecution convert2OrderExecution(DBItemReportTrigger rt, DBItemReportExecution re) {
        NotificationReportExecution item = new NotificationReportExecution();
        // unique
        item.setSchedulerId(re.getSchedulerId());
        item.setStandalone(false);
        item.setTaskId(re.getHistoryId());
        item.setStep(re.getStep());
        item.setOrderHistoryId(rt.getHistoryId());
        // others
        item.setJobChainName(CheckHistoryModel.normalizePath(rt.getParentName()));
        item.setJobChainTitle(rt.getParentTitle());
        item.setOrderId(rt.getName());
        item.setOrderTitle(rt.getTitle());
        item.setOrderStartTime(rt.getStartTime());
        item.setOrderEndTime(rt.getEndTime());
        item.setOrderStepState(re.getState());
        item.setOrderStepStartTime(re.getStartTime());
        item.setOrderStepEndTime(re.getEndTime());
        item.setJobName(CheckHistoryModel.normalizePath(re.getName()));
        item.setJobTitle(re.getTitle());
        item.setTaskStartTime(re.getTaskStartTime());
        item.setTaskEndTime(re.getTaskEndTime());
        item.setReturnCode(re.getExitCode() == null ? null : new Long(re.getExitCode().intValue()));
        item.setAgentUrl(re.getAgentUrl());
        item.setClusterMemberId(re.getClusterMemberId());
        item.setError(re.getError());
        item.setErrorCode(re.getErrorCode());
        item.setErrorText(re.getErrorText());
        return item;
    }

    public NotificationReportExecution convert2StandaloneExecution(DBItemReportTask task) {
        NotificationReportExecution item = new NotificationReportExecution();
        // unique
        item.setSchedulerId(task.getSchedulerId());
        item.setStandalone(true);
        item.setTaskId(task.getHistoryId());
        item.setStep(new Long(1));
        item.setOrderHistoryId(new Long(0));
        // others
        item.setJobChainName(DBLayer.DEFAULT_EMPTY_NAME);
        item.setJobChainTitle(null);
        item.setOrderId(DBLayer.DEFAULT_EMPTY_NAME);
        item.setOrderTitle(null);
        item.setOrderStartTime(null);
        item.setOrderEndTime(null);
        item.setOrderStepState(DBLayer.DEFAULT_EMPTY_NAME);
        item.setOrderStepStartTime(null);
        item.setOrderStepEndTime(null);
        item.setJobName(CheckHistoryModel.normalizePath(task.getName()));
        item.setJobTitle(task.getTitle());
        item.setTaskStartTime(task.getStartTime());
        item.setTaskEndTime(task.getEndTime());
        item.setReturnCode(task.getExitCode() == null ? null : new Long(task.getExitCode().intValue()));
        item.setAgentUrl(task.getAgentUrl());
        item.setClusterMemberId(task.getClusterMemberId());
        item.setError(task.getError());
        item.setErrorCode(task.getErrorCode());
        item.setErrorText(task.getErrorText());
        return item;
    }

    public boolean skipExecuteChecks() {
        return skipExecuteChecks;
    }
}
