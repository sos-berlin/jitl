package com.sos.jitl.inventory.exceptions;


public class SOSInventoryEventProcessingException extends SOSInventoryException {

    private static final long serialVersionUID = 1L;
    
    public SOSInventoryEventProcessingException() {
        super();
    }

    public SOSInventoryEventProcessingException(String message) {
        super(message);
    }
    
    public SOSInventoryEventProcessingException(Throwable cause) {
        super(cause);
    }
    
    public SOSInventoryEventProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public SOSInventoryEventProcessingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
