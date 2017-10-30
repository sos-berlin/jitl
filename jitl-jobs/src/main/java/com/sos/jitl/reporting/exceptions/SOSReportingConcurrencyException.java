package com.sos.jitl.reporting.exceptions;

import com.sos.exception.SOSException;

public class SOSReportingConcurrencyException extends SOSException {

    private static final long serialVersionUID = 1L;

    public SOSReportingConcurrencyException(Exception cause) {
        super(cause);
    }
}
