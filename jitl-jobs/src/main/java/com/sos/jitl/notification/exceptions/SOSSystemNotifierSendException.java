package com.sos.jitl.notification.exceptions;

import com.sos.exception.SOSException;

public class SOSSystemNotifierSendException extends SOSException {

    private static final long serialVersionUID = 1L;
    private String message = null;

    public SOSSystemNotifierSendException(String msg, Throwable cause) {
        message = msg;
        initCause(cause);
    }

    @Override
    public String getMessage() {
        return message;
    }
}
