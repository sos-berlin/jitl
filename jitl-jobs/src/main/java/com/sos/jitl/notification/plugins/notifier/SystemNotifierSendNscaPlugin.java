package com.sos.jitl.notification.plugins.notifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.spooler.Spooler;
import sos.util.SOSString;

import com.googlecode.jsendnsca.Level;
import com.googlecode.jsendnsca.MessagePayload;
import com.googlecode.jsendnsca.NagiosPassiveCheckSender;
import com.googlecode.jsendnsca.NagiosSettings;
import com.googlecode.jsendnsca.builders.MessagePayloadBuilder;
import com.googlecode.jsendnsca.builders.NagiosSettingsBuilder;
import com.googlecode.jsendnsca.encryption.Encryption;
import com.sos.jitl.notification.db.DBItemSchedulerMonChecks;
import com.sos.jitl.notification.db.DBItemSchedulerMonNotifications;
import com.sos.jitl.notification.db.DBItemSchedulerMonSystemNotifications;
import com.sos.jitl.notification.db.DBLayerSchedulerMon;
import com.sos.jitl.notification.helper.ElementNotificationMonitor;
import com.sos.jitl.notification.helper.ElementNotificationMonitorInterface;
import com.sos.jitl.notification.helper.EServiceMessagePrefix;
import com.sos.jitl.notification.helper.EServiceStatus;
import com.sos.jitl.notification.jobs.notifier.SystemNotifierJobOptions;

/** com.googlecode.jsendnsca.encryption.Encryption supports only 3 encryptions : NONE, XOR, TRIPLE_DES
 * 
 * "send_nsca.cfg" Note:
 * 
 * The encryption method you specify here must match the decryption method the nsca daemon uses (as specified in the nsca.cfg file)!!
 * 
 * Values:
 * 
 * 0=None (Do NOT use this option) <- Encryption.NONE
 * 
 * 1=Simple XOR (No security, just obfuscation, but very fast) <- Encryption.XOR
 * 
 * 2=DES,
 * 
 * 3=3DES (Triple DES) <- Encryption.TRIPLE_DES
 * 
 * 4=CAST-128, 5=CAST-256, 6=xTEA, 7=3WAY, 8=BLOWFISH, 9=TWOFISH
 * 
 * 10=LOKI97, 11=RC2, 12=ARCFOUR, 14=RIJNDAEL-128, 15=RIJNDAEL-192, 16=RIJNDAEL-256, 19=WAKE
 * 
 * 20=SERPENT, 22=ENIGMA (Unix crypt), 23=GOST, 24=SAFER64, 25=SAFER128, 26=SAFER+ */
public class SystemNotifierSendNscaPlugin extends SystemNotifierPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemNotifierSendNscaPlugin.class);
    private ElementNotificationMonitorInterface config = null;
    private NagiosSettings settings = null;

    @Override
    public void init(ElementNotificationMonitor monitor, SystemNotifierJobOptions opt) throws Exception {
        super.init(monitor, opt);

        config = (ElementNotificationMonitorInterface) getNotificationMonitor().getMonitorInterface();
        if (config == null) {
            throw new Exception(String.format("%s: %s element is missing (not configured)", getClass().getSimpleName(),
                    ElementNotificationMonitor.NOTIFICATION_INTERFACE));
        }

        NagiosSettingsBuilder nb = new NagiosSettingsBuilder().withNagiosHost(config.getMonitorHost());

        if (config.getMonitorPort() > -1) {
            nb.withPort(config.getMonitorPort());
        }
        if (config.getMonitorConnectionTimeout() > -1) {
            nb.withConnectionTimeout(config.getMonitorConnectionTimeout());
        }
        if (config.getMonitorResponseTimeout() > -1) {
            nb.withResponseTimeout(config.getMonitorResponseTimeout());
        }
        if (config.getMonitorPort() > -1) {
            nb.withPort(config.getMonitorPort());
        }
        if (!SOSString.isEmpty(config.getMonitorEncryption())) {
            nb.withEncryption(Encryption.valueOf(config.getMonitorEncryption()));
        }
        if (!SOSString.isEmpty(config.getMonitorPassword())) {
            nb.withPassword(config.getMonitorPassword());
        }
        settings = nb.create();
    }

    private Level resolveServiceStatus(String status) {
        Level l = null;

        if (status != null) {
            if (status.equals("0")) {
                l = Level.OK;
            } else if (status.equals("1")) {
                l = Level.WARNING;
            } else if (status.equals("2")) {
                l = Level.CRITICAL;
            } else if (status.equals("3")) {
                l = Level.UNKNOWN;
            }
        }
        return l;
    }

    @Override
    public int notifySystem(Spooler spooler, SystemNotifierJobOptions options, DBLayerSchedulerMon dbLayer,
            DBItemSchedulerMonNotifications notification, DBItemSchedulerMonSystemNotifications systemNotification, DBItemSchedulerMonChecks check,
            EServiceStatus status, EServiceMessagePrefix prefix) throws Exception {

        setCommand(config.getCommand());

        setTableFields(notification, systemNotification, check);
        resolveCommandAllTableFieldVars();
        resolveCommandServiceNameVar(systemNotification.getServiceName());
        resolveCommandServiceStatusVar(getServiceStatusValue(status));
        resolveCommandServiceMessagePrefixVar(getServiceMessagePrefixValue(prefix));
        resolveCommandAllEnvVars();
        setCommandPrefix(prefix);

        MessagePayload payload = new MessagePayloadBuilder().withHostname(config.getServiceHost()).withLevel(getLevel(status)).withServiceName(
                systemNotification.getServiceName()).withMessage(getCommand()).create();

        LOGGER.info(String.format("send to host= %s:%s service host= %s, service name = %s, level = %s, message = %s", settings.getNagiosHost(),
                settings.getPort(), payload.getHostname(), payload.getServiceName(), payload.getLevel(), payload.getMessage()));

        NagiosPassiveCheckSender sender = new NagiosPassiveCheckSender(settings);
        sender.send(payload);

        return 0;
    }

    @Override
    public int notifySystemReset(String serviceName, EServiceStatus status, EServiceMessagePrefix prefix, String message) throws Exception {

        Level level = status.equals(EServiceStatus.OK) ? Level.OK : Level.CRITICAL;

        MessagePayload payload = new MessagePayloadBuilder().withHostname(config.getServiceHost()).withLevel(level).withServiceName(serviceName)
                .withMessage(message).create();

        LOGGER.info(String.format("send to host= %s:%s service host= %s, service name = %s, level = %s, message = %s", settings.getNagiosHost(),
                settings.getPort(), payload.getHostname(), payload.getServiceName(), payload.getLevel(), payload.getMessage()));

        NagiosPassiveCheckSender sender = new NagiosPassiveCheckSender(settings);
        sender.send(payload);

        return 0;
    }

    private Level getLevel(EServiceStatus status) {
        Level level = null;
        if (status.equals(EServiceStatus.OK)) {
            level = resolveServiceStatus(getNotificationMonitor().getServiceStatusOnSuccess());
            if (level == null) {
                level = Level.OK;
            }
        } else {
            level = resolveServiceStatus(getNotificationMonitor().getServiceStatusOnError());
            if (level == null) {
                level = Level.CRITICAL;
            }
        }
        return level;
    }

    private void setCommandPrefix(EServiceMessagePrefix prefix) {
        if (getCommand() == null) {
            return;
        }
        if (prefix == null) {
            return;
        }

        if (!prefix.equals(EServiceMessagePrefix.NONE)) {
            String command = getCommand().trim().toLowerCase();
            String prefixName = prefix.name().trim().toLowerCase();
            if (!command.startsWith(prefixName)) {
                setCommand(prefix.name() + " " + getCommand());
            }
        }
    }

}
