package com.sos.jitl.inventory.exceptions;


public class SOSInventoryInitialProcessingException extends SOSInventoryException {

    private static final long serialVersionUID = 1L;
    
    public SOSInventoryInitialProcessingException() {
        super();
    }

    public SOSInventoryInitialProcessingException(String message) {
        super(message);
    }
    
    public SOSInventoryInitialProcessingException(Throwable cause) {
        super(cause);
    }
    
    public SOSInventoryInitialProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public SOSInventoryInitialProcessingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
