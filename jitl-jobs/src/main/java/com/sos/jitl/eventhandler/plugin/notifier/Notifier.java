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
    private int notifyInterval = 5; // in minutes for the reoccurred exceptions

    public Notifier(Mailer pluginMailer, Class<?> clazz) {
        mailer = pluginMailer;
        caller = clazz.getSimpleName();
    }

    private Notifier(Mailer pluginMailer, String clazzName) {
        mailer = pluginMailer;
        caller = clazzName;
    }

    public Notifier newInstance() {
        return new Notifier(mailer, caller);
    }

    public boolean smartNotifyOnError(String callerMethod, Throwable e) {
        return smartNotifyOnError(notifyInterval, callerMethod, null, e);
    }

    public boolean smartNotifyOnError(String callerMethod, String bodyPart, Throwable e) {
        return smartNotifyOnError(notifyInterval, callerMethod, bodyPart, e);
    }

    public boolean smartNotifyOnError(int notifyInterval, String callerMethod, String bodyPart, Throwable e) {
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
            LOGGER.debug(String.format("[%s][notifyInterval=%sm][diff=%sm][current=%sm, lastNotifier=%sm]", caller, notifyInterval, (current
                    - lastNotifier), current, lastNotifier));
        }
        if ((current - lastNotifier) >= notifyInterval) {
            if (notifyFirstIntervalErrorAsWarning && counter == 1) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][sendOnWarning]%s", caller, e.toString()));
                }
                mailer.sendOnWarning(caller, callerMethod, bodyPart, e);
            } else {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][sendOnError]%s", caller, e.toString()));
                }
                mailer.sendOnError(caller, callerMethod, bodyPart, e);
            }
            lastNotifier = current;
            lastException = e;
            return true;
        } else {
            LOGGER.error(String.format("[%s][reoccurred][%s]%s", callerMethod, counter, e.toString()));
        }
        return false;
    }

    public boolean smartNotifyOnRecovery(String msg) {
        if (lastNotifier != null) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][smartNotifyOnRecovery]%s", caller, lastException == null ? "lastException is null" : lastException
                        .toString()));
            }
            mailer.sendOnRecovery(caller, msg, lastException);
            reset();
            return true;
        }
        return false;
    }

    public void notifyOnError(String callerMethod, Throwable e) {
        notifyOnError(callerMethod, null, e);
    }

    public void notifyOnError(String callerMethod, String bodyPart, Throwable e) {
        mailer.sendOnError(caller, callerMethod, bodyPart, e);
    }

    public void setNotifyFirstIntervalErrorAsWarning(boolean val) {
        notifyFirstIntervalErrorAsWarning = val;
    }

    public boolean getNotifyFirstIntervalErrorAsWarning() {
        return notifyFirstIntervalErrorAsWarning;
    }

    public void setNotifyInterval(int val) {
        notifyInterval = val;
    }

    public int getNotifyInterval() {
        return notifyInterval;
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
