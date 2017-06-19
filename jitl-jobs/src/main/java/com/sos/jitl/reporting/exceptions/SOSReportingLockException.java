package com.sos.jitl.reporting.exceptions;

import com.sos.exception.SOSException;

public class SOSReportingLockException extends SOSException {

    private static final long serialVersionUID = 1L;

    public SOSReportingLockException(Exception cause) {
        super(cause);
    }
}
