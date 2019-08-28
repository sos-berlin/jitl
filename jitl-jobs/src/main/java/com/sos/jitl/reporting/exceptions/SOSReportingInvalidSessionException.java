package com.sos.jitl.reporting.exceptions;

import com.sos.exception.SOSException;

public class SOSReportingInvalidSessionException extends SOSException {

    private static final long serialVersionUID = 1L;

    public SOSReportingInvalidSessionException(Exception cause) {
        super(cause);
    }
}
