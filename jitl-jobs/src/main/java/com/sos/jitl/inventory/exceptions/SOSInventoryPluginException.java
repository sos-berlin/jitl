package com.sos.jitl.inventory.exceptions;


public class SOSInventoryPluginException extends SOSInventoryException {

    private static final long serialVersionUID = 1L;
    
    public SOSInventoryPluginException() {
        super();
    }

    public SOSInventoryPluginException(String message) {
        super(message);
    }
    
    public SOSInventoryPluginException(Throwable cause) {
        super(cause);
    }
    
    public SOSInventoryPluginException(String message, Throwable cause) {
        super(message, cause);
    }

    public SOSInventoryPluginException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
