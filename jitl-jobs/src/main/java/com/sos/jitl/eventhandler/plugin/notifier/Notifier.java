package com.sos.jitl.eventhandler.plugin.notifier;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.util.SOSDate;

public class Notifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(Notifier.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private Mailer mailer;
    private String caller;
    private Long lastNotifier;
    private Throwable lastException;
    private boolean notifyFirstIntervalErrorAsWarning;
    private long counter;

    public Notifier(Mailer pluginMailer, String className) {
        mailer = pluginMailer;
        caller = className;
    }

    public boolean sendOnError(int notifyInterval, String msg, Throwable e) {
        if (lastException != null && e != null) {
            if (!lastException.getClass().equals(e.getClass())) {
                reset();
            }
        }
        counter++;
        if (lastNotifier == null) {
            lastNotifier = new Long(0);
        }
        Long current = SOSDate.getMinutes(new Date());
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][notifyInterval=%sm]current=%sm, lastNotifier=%sm", caller, notifyInterval, current, lastNotifier));
        }
        if ((current - lastNotifier) >= notifyInterval) {
            if (notifyFirstIntervalErrorAsWarning && counter == 1) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][sendOnWarning]%s", caller, e.toString()));
                }
                mailer.sendOnWarning(caller, msg, e);
            } else {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][sendOnError]%s", caller, e.toString()));
                }
                mailer.sendOnError(caller, msg, e);
            }
            lastNotifier = current;
            lastException = e;
            return true;
        }
        return false;
    }

    public boolean sendOnError(String callerMethod, Throwable e) {
        return sendOnError(callerMethod, null, e);
    }

    public boolean sendOnError(String callerMethod, String bodyPart, Throwable e) {
        counter++;
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][%s][%s][sendOnError]%s", caller, callerMethod, bodyPart, e.toString()));
        }
        mailer.sendOnError(caller, callerMethod, bodyPart, e);

        lastNotifier = SOSDate.getMinutes(new Date());
        lastException = e;
        return true;
    }

    public boolean sendOnRecovery(String msg) {
        if (lastNotifier != null) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][sendOnRecovery]%s", caller, lastException == null ? "lastException is null" : lastException
                        .toString()));
            }
            mailer.sendOnRecovery(caller, msg, lastException);
            reset();
            return true;
        }
        return false;
    }

    public void setNotifyFirstIntervalErrorAsWarning(boolean val) {
        notifyFirstIntervalErrorAsWarning = val;
    }

    public boolean setNotifyFirstIntervalErrorAsWarning() {
        return notifyFirstIntervalErrorAsWarning;
    }

    public long getCounter() {
        return counter;
    }

    private void reset() {
        counter = 0;
        lastNotifier = null;
        lastException = null;
    }
}
