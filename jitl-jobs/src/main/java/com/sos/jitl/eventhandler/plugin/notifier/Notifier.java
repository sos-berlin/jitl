package com.sos.jitl.eventhandler.plugin.notifier;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.util.SOSDate;
import sos.util.SOSString;

public class Notifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(Notifier.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

    private final Mailer mailer;
    private final String caller;
    private boolean notifyFirstErrorAsWarning;
    private ErrorNotifier errorNotifier = new ErrorNotifier();
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

    public boolean smartNotifyOnError(Class<?> clazz, Throwable e) {
        return smartNotifyOnError(clazz, null, e);
    }

    public boolean smartNotifyOnError(Class<?> clazz, String bodyPart, Throwable e) {
        return smartNotifyOnError(clazz, bodyPart, e, notifyInterval);
    }

    public boolean smartNotifyOnError(Class<?> clazz, String bodyPart, Throwable e, int notifyInterval) {
        if (errorNotifier.getException() != null && e != null) {
            if (!errorNotifier.getException().getClass().equals(e.getClass())) {
                reset();
            }
        }

        errorNotifier.addCounter();

        if (errorNotifier.calculate()) {
            if (notifyFirstErrorAsWarning && errorNotifier.getCounter() == 1) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][sendOnWarning]%s", caller, e.toString()));
                }
                mailer.sendOnWarning(caller, clazz.getSimpleName(), bodyPart, e);
            } else {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[%s][sendOnError]%s", caller, e.toString()));
                }
                if (errorNotifier.getCounter() > 1) {
                    String msg = "This error has now occurs " + errorNotifier.getCounter() + " times.";
                    if (SOSString.isEmpty(bodyPart)) {
                        bodyPart = msg;
                    } else {
                        StringBuilder sb = new StringBuilder(msg);
                        sb.append(String.format("%s", Mailer.NEW_LINE));
                        sb.append(bodyPart);
                        bodyPart = sb.toString();
                    }
                }
                mailer.sendOnError(caller, clazz.getSimpleName(), bodyPart, e);
            }
            errorNotifier.setException(e);
            errorNotifier.setCaller(clazz);
            return true;
        } else {
            LOGGER.error(String.format("[%s][reoccurred][%s]%s", clazz.getSimpleName(), errorNotifier.getCounter(), e.toString()));
        }
        return false;
    }

    public boolean smartNotifyOnRecovery() {
        if (errorNotifier.getCounter() > 0) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][smartNotifyOnRecovery]%s", caller, errorNotifier.getException() == null ? "lastException is null"
                        : errorNotifier.getException().toString()));
            }
            mailer.sendOnRecovery(caller, errorNotifier.getCaller().getSimpleName(), errorNotifier.getException());
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

    public void setNotifyFirstErrorAsWarning(boolean val) {
        notifyFirstErrorAsWarning = val;
    }

    public boolean geNotifyFirstErrorAsWarning() {
        return notifyFirstErrorAsWarning;
    }

    public void setNotifyInterval(int val) {
        notifyInterval = val;
    }

    public int getNotifyInterval() {
        return notifyInterval;
    }

    private void reset() {
        errorNotifier = new ErrorNotifier();
    }

    private class ErrorNotifier {

        private long counter = 0;
        private Long last = new Long(0);
        private Throwable exception;
        private Class<?> caller;

        public void addCounter() {
            counter++;
        }

        public long getCounter() {
            return counter;
        }

        public boolean calculate() {
            Long current = SOSDate.getMinutes(new Date());
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][notifyInterval=%sm][diff=%sm][current=%sm, last=%sm]", caller, notifyInterval, (current - last),
                        current, last));
            }
            if ((current - last) >= notifyInterval) {
                last = current;
                return true;
            }
            return false;
        }

        public Throwable getException() {
            return exception;
        }

        public void setException(Throwable val) {
            exception = val;
        }

        public void setCaller(Class<?> val) {
            caller = val;
        }

        public Class<?> getCaller() {
            return caller;
        }
    }
}
