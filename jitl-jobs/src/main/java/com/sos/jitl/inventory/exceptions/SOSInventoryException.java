package com.sos.jitl.inventory.exceptions;

import com.sos.exception.SOSException;


public class SOSInventoryException extends SOSException {

    private static final long serialVersionUID = 1L;
    
    public SOSInventoryException() {
        super();
    }

    public SOSInventoryException(String message) {
        super(message);
    }
    
    public SOSInventoryException(Throwable cause) {
        super(cause);
    }
    
    public SOSInventoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public SOSInventoryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
