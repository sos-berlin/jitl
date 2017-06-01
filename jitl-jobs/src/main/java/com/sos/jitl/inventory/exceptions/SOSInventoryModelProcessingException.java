package com.sos.jitl.inventory.exceptions;


public class SOSInventoryModelProcessingException extends SOSInventoryException {

    private static final long serialVersionUID = 1L;
    
    public SOSInventoryModelProcessingException() {
        super();
    }

    public SOSInventoryModelProcessingException(String message) {
        super(message);
    }
    
    public SOSInventoryModelProcessingException(Throwable cause) {
        super(cause);
    }
    
    public SOSInventoryModelProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public SOSInventoryModelProcessingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
