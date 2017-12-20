package com.sos.jitl.notification.plugins.notifier;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import javax.persistence.Column;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.spooler.Spooler;
import sos.util.SOSString;

import com.sos.hibernate.classes.DbItem;
import com.sos.jitl.notification.db.DBItemSchedulerMonChecks;
import com.sos.jitl.notification.db.DBItemSchedulerMonNotifications;
import com.sos.jitl.notification.db.DBItemSchedulerMonSystemNotifications;
import com.sos.jitl.notification.db.DBLayer;
import com.sos.jitl.notification.db.DBLayerSchedulerMon;
import com.sos.jitl.notification.helper.EServiceMessagePrefix;
import com.sos.jitl.notification.helper.EServiceStatus;
import com.sos.jitl.notification.helper.ElementNotificationMonitor;
import com.sos.jitl.notification.jobs.notifier.SystemNotifierJobOptions;

public class SystemNotifierPlugin implements ISystemNotifierPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemNotifierPlugin.class);

    private ElementNotificationMonitor notificationMonitor = null;
    private SystemNotifierJobOptions options;
    private String command;
    private Map<String, String> tableFields = null;
    private boolean hasErrorOnInit = false;
    private String initError = null;
    private boolean replaceBackslashes = false;

    public static final String VARIABLE_TABLE_PREFIX_NOTIFICATIONS = "MON_N";
    public static final String VARIABLE_TABLE_PREFIX_SYSNOTIFICATIONS = "MON_SN";
    public static final String VARIABLE_TABLE_PREFIX_CHECKS = "MON_C";

    public static final String VARIABLE_ENV_PREFIX = "SCHEDULER_MON";
    public static final String VARIABLE_ENV_PREFIX_TABLE_FIELD = VARIABLE_ENV_PREFIX + "_TABLE";

    public static final String VARIABLE_SERVICE_NAME = "SERVICE_NAME";
    public static final String VARIABLE_SERVICE_MESSAGE_PREFIX = "SERVICE_MESSAGE_PREFIX";
    public static final String VARIABLE_SERVICE_STATUS = "SERVICE_STATUS";
    public static final String VARIABLE_JOC_HREF_JOB_CHAIN = "JOC_HREF_JOB_CHAIN";
    public static final String VARIABLE_JOC_HREF_ORDER = "JOC_HREF_ORDER";
    public static final String VARIABLE_JOC_HREF_JOB = "JOC_HREF_JOB";

    @Override
    public void init(ElementNotificationMonitor monitor, SystemNotifierJobOptions opt) throws Exception {
        notificationMonitor = monitor;
        options = opt;
        resetInitError();
    }

    @Override
    public int notifySystemReset(String serviceName, EServiceStatus status, EServiceMessagePrefix prefix, String command) throws Exception {
        return 0;
    }

    @Override
    public int notifySystem(Spooler spooler, SystemNotifierJobOptions options, DBLayerSchedulerMon dbLayer,
            DBItemSchedulerMonNotifications notification, DBItemSchedulerMonSystemNotifications systemNotification, DBItemSchedulerMonChecks check,
            EServiceStatus status, EServiceMessagePrefix prefix) throws Exception {
        return 0;
    }

    @Override
    public void close() {
    }

    @Override
    public boolean hasErrorOnInit() {
        return hasErrorOnInit;
    }

    @Override
    public String getInitError() {
        return initError;
    }

    public void setInitError(String err) {
        hasErrorOnInit = true;
        initError = err;
    }

    private void resetInitError() {
        hasErrorOnInit = false;
        initError = null;
    }

    public String getServiceStatusValue(EServiceStatus status) throws Exception {
        String method = "getServiceStatusValue";

        if (getNotificationMonitor() == null) {
            throw new Exception(String.format("%s: this.getNotificationMonitor() is NULL", method));
        }

        /** e.g Nagios 0- OK 1-Warning 2-Critical 3-Unknown */
        String serviceStatus = "0";
        if (status.equals(EServiceStatus.OK)) {
            if (SOSString.isEmpty(getNotificationMonitor().getServiceStatusOnSuccess())) {
                serviceStatus = EServiceStatus.OK.name();
            } else {
                serviceStatus = getNotificationMonitor().getServiceStatusOnSuccess();
            }
        } else {
            if (SOSString.isEmpty(getNotificationMonitor().getServiceStatusOnError())) {
                serviceStatus = EServiceStatus.CRITICAL.name();
            } else {
                serviceStatus = getNotificationMonitor().getServiceStatusOnError();
            }
        }
        LOGGER.debug(String.format("%s:[EServiceStatus=%s]serviceStatus=%s", method, status, serviceStatus));

        return serviceStatus;
    }

    public String getServiceMessagePrefixValue(EServiceMessagePrefix prefix) {
        String method = "getServiceMessagePrefixValue";
        String servicePrefix = "";
        if (prefix != null && !prefix.equals(EServiceMessagePrefix.NONE)) {
            servicePrefix = prefix.name() + " ";
        }
        LOGGER.debug(String.format("%s:[EServiceMessagePrefix=%s]servicePrefix=%s", method, prefix, servicePrefix));

        return servicePrefix;
    }

    protected void resetTableFields() {
        tableFields = null;
    }

    protected void setTableFields(DbItem notification, DbItem systemNotification, DbItem check) throws Exception {
        if (notification == null) {
            throw new Exception("Cannot get table fields. DbItem notification is null");
        }
        if (systemNotification == null) {
            throw new Exception("Cannot get table fields. DbItem systemNotification is null");
        }
        tableFields = new HashMap<String, String>();
        setDbItemTableFields(notification, VARIABLE_TABLE_PREFIX_NOTIFICATIONS);
        setDbItemTableFields(systemNotification, VARIABLE_TABLE_PREFIX_SYSNOTIFICATIONS);
        setDbItemTableFields(check == null ? new DBItemSchedulerMonChecks() : check, VARIABLE_TABLE_PREFIX_CHECKS);

        // NOTIFICATIONS
        setTableFieldElapsed(VARIABLE_TABLE_PREFIX_NOTIFICATIONS + "_ORDER_TIME_ELAPSED", VARIABLE_TABLE_PREFIX_NOTIFICATIONS + "_ORDER_START_TIME",
                VARIABLE_TABLE_PREFIX_NOTIFICATIONS + "_ORDER_END_TIME");
        setTableFieldElapsed(VARIABLE_TABLE_PREFIX_NOTIFICATIONS + "_TASK_TIME_ELAPSED", VARIABLE_TABLE_PREFIX_NOTIFICATIONS + "_TASK_START_TIME",
                VARIABLE_TABLE_PREFIX_NOTIFICATIONS + "_TASK_END_TIME");
        setTableFieldElapsed(VARIABLE_TABLE_PREFIX_NOTIFICATIONS + "_ORDER_STEP_TIME_ELAPSED", VARIABLE_TABLE_PREFIX_NOTIFICATIONS
                + "_ORDER_STEP_START_TIME", VARIABLE_TABLE_PREFIX_NOTIFICATIONS + "_ORDER_STEP_END_TIME");

        // SYSNOTOFICATIONS
        setTableFieldElapsed(VARIABLE_TABLE_PREFIX_SYSNOTIFICATIONS + "_STEP_TIME_ELAPSED", VARIABLE_TABLE_PREFIX_SYSNOTIFICATIONS
                + "_STEP_FROM_START_TIME", VARIABLE_TABLE_PREFIX_SYSNOTIFICATIONS + "_STEP_TO_END_TIME");

        // CHECKS
        setTableFieldElapsed(VARIABLE_TABLE_PREFIX_CHECKS + "_STEP_TIME_ELAPSED", VARIABLE_TABLE_PREFIX_CHECKS + "_STEP_FROM_START_TIME",
                VARIABLE_TABLE_PREFIX_CHECKS + "_STEP_TO_END_TIME");

    }

    private void setTableFieldElapsed(String newField, String startTimeField, String endTimeField) throws Exception {
        tableFields.put(newField, "");

        if (tableFields.containsKey(startTimeField) && tableFields.containsKey(endTimeField)) {
            String vnost = tableFields.get(startTimeField);
            String vnoet = tableFields.get(endTimeField);
            if (!SOSString.isEmpty(vnost) && !SOSString.isEmpty(vnoet)) {
                Date dnost = DBLayer.getDateFromString(vnost);
                Date dnoet = DBLayer.getDateFromString(vnoet);
                Long diffSeconds = dnoet.getTime() / 1000 - dnost.getTime() / 1000;
                tableFields.put(newField, diffSeconds.toString());
            }
        }
    }

    private void setDbItemTableFields(DbItem obj, String prefix) throws Exception {
        Method[] ms = obj.getClass().getDeclaredMethods();
        for (Method m : ms) {
            if (m.getName().startsWith("get")) {
                Column c = m.getAnnotation(Column.class);
                if (c != null) {
                    String name = c.name().replaceAll("`", "");
                    name = prefix + "_" + name;
                    if (!tableFields.containsKey(name)) {
                        Object objVal = m.invoke(obj);
                        String val = "";
                        if (objVal != null) {
                            if (objVal instanceof Timestamp) {
                                val = DBLayer.getDateAsString((Date) objVal);
                            } else if (objVal instanceof Boolean) {
                                val = (Boolean) objVal ? "1" : "0";
                            } else {
                                val = objVal.toString();
                            }
                        }

                        tableFields.put(name, val);
                    }
                }
            }
        }
    }

    public String resolveAllVars(final DBItemSchedulerMonSystemNotifications systemNotification, final DBItemSchedulerMonNotifications notification,
            final DBItemSchedulerMonChecks check, final EServiceStatus status, final EServiceMessagePrefix prefix, final String msg)
            throws Exception {
        if (SOSString.isEmpty(msg)) {
            return msg;
        }
        String txt = msg;
        if (tableFields == null) {
            setTableFields(notification, systemNotification, check);
        }
        txt = resolveAllTableFieldVars(txt);
        txt = resolveVar(txt, VARIABLE_SERVICE_NAME, systemNotification.getServiceName());
        txt = resolveVar(txt, VARIABLE_SERVICE_STATUS, getServiceStatusValue(status));
        txt = resolveVar(txt, VARIABLE_SERVICE_MESSAGE_PREFIX, getServiceMessagePrefixValue(prefix));
        txt = resolveEnvVars(txt, System.getenv());
        return txt;
    }

    protected String resolveJocLinkJobChain(final String val, String href) {
        return resolveVar(val, VARIABLE_JOC_HREF_JOB_CHAIN, href);
    }

    protected String resolveJocLinkOrder(final String val, String href) {
        return resolveVar(val, VARIABLE_JOC_HREF_ORDER, href);
    }

    protected String resolveJocLinkJob(final String val, String href) {
        return resolveVar(val, VARIABLE_JOC_HREF_JOB, href);
    }

    protected void resolveCommandServiceNameVar(String serviceName) {
        command = resolveVar(command, VARIABLE_SERVICE_NAME, serviceName);
    }

    protected void resolveCommandServiceMessagePrefixVar(String prefix) {
        command = resolveVar(command, VARIABLE_SERVICE_MESSAGE_PREFIX, prefix);
    }

    protected void resolveCommandJocLinks(String jocHrefJobChain, String jocHrefJob) {
        command = resolveJocLinkJobChain(command, jocHrefJobChain);
        command = resolveJocLinkJob(command, jocHrefJob);
    }

    protected void resolveCommandServiceStatusVar(String serviceStatus) {
        command = resolveVar(command, VARIABLE_SERVICE_STATUS, serviceStatus);

    }

    protected void resolveCommandAllEnvVars() {
        command = resolveEnvVars(command, System.getenv());
    }

    protected void resolveCommandAllTableFieldVars() throws Exception {
        command = resolveAllTableFieldVars(command);

    }

    protected String normalizeVarValue(String value) {
        // new lines
        value = value.replaceAll("\\r\\n|\\r|\\n", " ");
        if (replaceBackslashes) {
            // for values with paths: e.g.: d:\abc
            value = value.replaceAll("\\\\", "\\\\\\\\");
        }
        return value;
    }

    protected String nl2br(String value) {
        return value.replaceAll("\\r\\n|\\r|\\n", "<br/>");
    }

    protected String resolveAllTableFieldVars(String text) throws Exception {
        if (text == null) {
            return null;
        }
        if (tableFields == null) {
            throw new Exception("tableFields is NULL");
        }
        for (Entry<String, String> entry : tableFields.entrySet()) {
            text = resolveVar(text, entry.getKey(), entry.getValue());
        }
        return text;
    }

    protected String resolveEnvVars(String text, Map<String, String> envs) {
        if (text == null) {
            return null;
        }
        boolean isWindows = isWindows();
        for (Map.Entry<String, String> entry : envs.entrySet()) {
            text = resolveEnvVar(isWindows, text, entry.getKey(), entry.getValue());
        }
        return text;
    }

    private String resolveEnvVar(boolean isWindows, String cmd, String varName, String varValue) {
        if (cmd == null) {
            return null;
        }

        String normalized = varValue == null ? "" : normalizeVarValue(varValue);
        if (isWindows) {
            cmd = cmd.replaceAll("%(?i)" + varName + "%", Matcher.quoteReplacement(normalized));
        } else {
            cmd = cmd.replaceAll("\\$\\{(?i)" + varName + "\\}", Matcher.quoteReplacement(normalized));
            cmd = cmd.replaceAll("\\$(?i)" + varName, Matcher.quoteReplacement(normalized));
        }
        return cmd;
    }

    protected String resolveVar(String text, String varName, String varValue) {
        if (text == null) {
            return null;
        }

        String normalized = varValue == null ? "" : normalizeVarValue(varValue);
        // 2 replacements - compatibility, using of the old {var} and new ${var} syntax
        text = text.replaceAll("\\$\\{(?i)" + varName + "\\}", Matcher.quoteReplacement(normalized));
        return text.replaceAll("\\{(?i)" + varName + "\\}", Matcher.quoteReplacement(normalized));
    }

    protected boolean isWindows() {
        try {
            return System.getProperty("os.name").toLowerCase().contains("windows");
        } catch (Exception x) {
            return false;
        }
    }

    protected void setCommand(String cmd) {
        command = cmd;
    }

    public ElementNotificationMonitor getNotificationMonitor() {
        return notificationMonitor;
    }

    protected String getCommand() {
        return command;
    }

    protected Map<String, String> getTableFields() {
        return tableFields;
    }

    public SystemNotifierJobOptions getOptions() {
        return options;
    }

    public boolean getReplaceBackslashes() {
        return replaceBackslashes;
    }

    public void setReplaceBackslashes(boolean val) {
        replaceBackslashes = val;
    }
}
