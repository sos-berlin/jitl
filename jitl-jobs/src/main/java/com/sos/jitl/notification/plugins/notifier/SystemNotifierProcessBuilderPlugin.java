package com.sos.jitl.notification.plugins.notifier;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.notification.db.DBItemSchedulerMonChecks;
import com.sos.jitl.notification.db.DBItemSchedulerMonNotifications;
import com.sos.jitl.notification.db.DBItemSchedulerMonSystemNotifications;
import com.sos.jitl.notification.db.DBLayerSchedulerMon;
import com.sos.jitl.notification.exceptions.SOSSystemNotifierSendException;
import com.sos.jitl.notification.helper.EServiceMessagePrefix;
import com.sos.jitl.notification.helper.EServiceStatus;
import com.sos.jitl.notification.helper.ElementNotificationMonitor;
import com.sos.jitl.notification.helper.ElementNotificationMonitorCommand;
import com.sos.jitl.notification.jobs.notifier.SystemNotifierJobOptions;

import sos.spooler.Spooler;
import sos.util.SOSString;

public class SystemNotifierProcessBuilderPlugin extends SystemNotifierPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemNotifierProcessBuilderPlugin.class);
    private ElementNotificationMonitorCommand config = null;

    @Override
    public void init(ElementNotificationMonitor monitor, SystemNotifierJobOptions opt) throws Exception {
        super.init(monitor, opt);
        setReplaceBackslashes(true);
        config = (ElementNotificationMonitorCommand) getNotificationMonitor().getMonitorInterface();
        if (config == null) {
            throw new Exception(String.format("[init]%s element is missing (not configured)", ElementNotificationMonitor.NOTIFICATION_COMMAND));
        }
    }

    /** @TODO not implemented yet */
    @Override
    public int notifySystemReset(String serviceName, EServiceStatus status, EServiceMessagePrefix prefix, String message) throws Exception {
        return 0;
    }

    /** @TODO calculate elapsed etc. */
    @Override
    public int notifySystem(Spooler spooler, SystemNotifierJobOptions options, DBLayerSchedulerMon dbLayer,
            DBItemSchedulerMonNotifications notification, DBItemSchedulerMonSystemNotifications systemNotification, DBItemSchedulerMonChecks check,
            EServiceStatus status, EServiceMessagePrefix prefix) throws SOSSystemNotifierSendException {

        String method = "notifySystem";
        Process p = null;
        int exitCode = 0;
        try {
            setCommand(config.getCommand());

            String serviceStatus = getServiceStatusValue(status);
            String servicePrefix = prefix == null ? "" : prefix.name();

            setTableFields(notification, systemNotification, check);
            resolveCommandAllTableFieldVars();
            resolveCommandServiceNameVar(systemNotification.getServiceName());
            resolveCommandServiceStatusVar(serviceStatus);
            resolveCommandServiceMessagePrefixVar(servicePrefix);

            ProcessBuilder pb = new ProcessBuilder();
            pb.command(createProcessBuilderCommand(getCommand()));

            // Process ENV Variables setzen
            Map<String, String> env = pb.environment();
            env.put(VARIABLE_ENV_PREFIX + "_SERVICE_STATUS", serviceStatus);
            env.put(VARIABLE_ENV_PREFIX + "_SERVICE_NAME", systemNotification.getServiceName());
            env.put(VARIABLE_ENV_PREFIX + "_SERVICE_MESSAGE_PREFIX", servicePrefix.trim());
            env.put(VARIABLE_ENV_PREFIX + "_SERVICE_COMMAND", getCommand());
            if (getTableFields() != null) {
                for (Entry<String, String> entry : getTableFields().entrySet()) {
                    env.put(VARIABLE_ENV_PREFIX_TABLE_FIELD + "_" + entry.getKey().toUpperCase(), normalizeVarValue(entry.getValue()));
                }
            }

            LOGGER.info(String.format("[%s-%s][command][preview]%s", serviceStatus, servicePrefix, resolveEnvVars(getCommand(), env)));
            LOGGER.info(String.format("[%s-%s][command][execute]%s", serviceStatus, servicePrefix, pb.command()));

            p = pb.start();
            if (p.waitFor() != 0) {
                exitCode = p.exitValue();
            }

            if (exitCode > 0) {
                StringBuffer inputStream = new StringBuffer();
                StringBuffer errorStream = new StringBuffer();

                Scanner s = null;
                try {
                    s = new Scanner(p.getInputStream());
                    while (s.hasNext()) {
                        String m = s.next();
                        if (m.trim().length() > 0) {
                            inputStream.append(m.trim());
                            inputStream.append(" ");
                        }
                    }
                } catch (Exception ex) {
                    LOGGER.warn(String.format("error reading process input stream = %s", ex.toString()));
                } finally {
                    if (s != null) {
                        try {
                            s.close();
                        } catch (Exception ex) {
                        }
                    }
                }
                try {
                    s = new Scanner(p.getErrorStream());
                    while (s.hasNext()) {
                        String m = s.next();
                        if (m.trim().length() > 0) {
                            errorStream.append(m.trim());
                            errorStream.append(" ");
                        }
                    }
                } catch (Exception ex) {
                    LOGGER.warn(String.format("error reading process error stream = %s", ex.toString()));
                } finally {
                    if (s != null) {
                        try {
                            s.close();
                        } catch (Exception ex) {
                        }
                    }
                }

                if (inputStream.length() > 0 || errorStream.length() > 0) {
                    throw new Exception(String.format("[command executed][exitCode=%s][input stream=%s][error stream=%s]", exitCode, inputStream
                            .toString(), errorStream.toString()));
                }
            }

            LOGGER.info(String.format("[%s-%s][command][executed]exitCode=%s", serviceStatus, servicePrefix, exitCode));

            return exitCode;
        } catch (Throwable ex) {
            throw new SOSSystemNotifierSendException(String.format("[%s]%s", method, ex.toString()), ex);
        } finally {
            try {
                p.destroy();
            } catch (Exception e) {
            }
        }
    }

    @Override
    public String onResolveAllTableFieldVars(String key, String value) {
        if (key.equals(VARIABLE_TABLE_PREFIX_NOTIFICATIONS + "_ERROR_TEXT")) {
            if (isWindows()) {
                return mask4Windows(value);
            } else {
                return mask4Unix(value);
            }
        }
        return value;
    }

    private String[] createProcessBuilderCommand(String command) {
        String[] c = new String[3];
        if (this.isWindows()) {
            String executable = System.getenv("comspec");
            if (SOSString.isEmpty(executable)) {
                executable = "cmd.exe";
            }
            c[0] = executable;
            c[1] = "/C";
            c[2] = command;
        } else {
            String executable = System.getenv("SHELL");
            if (SOSString.isEmpty(executable)) {
                executable = "/bin/sh";
            }
            c[0] = executable;
            c[1] = "-c";
            c[2] = command;
        }

        return c;
    }

    private String mask4Windows(String s) {
        return s.replaceAll("<", "^<").replaceAll(">", "^>").replaceAll("%", "^%").replaceAll("&", "^&");
    }

    private String mask4Unix(String s) {
        return s.replaceAll("<", "\\<").replaceAll(">", "\\>").replaceAll("%", "\\%").replaceAll("&", "\\&");
    }
}
