package com.sos.jitl.eventing;

import org.apache.log4j.Logger;

public class JSEventsClientJSAdapterClass extends JSEventsClientBaseClass {

    private final String conClassName = "JSEventsClientJSAdapterClass";						//$NON-NLS-1$
    private static Logger logger = Logger.getLogger(JSEventsClientJSAdapterClass.class);

    public void init() {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::init"; //$NON-NLS-1$
        doInitialize();
    }

    private void doInitialize() {
    } // doInitialize

    @Override
    public boolean spooler_process() throws Exception {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::spooler_process"; //$NON-NLS-1$

        try {
            super.spooler_process();
            doProcessing();
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
            throw e;
        } finally {
        } // finally
        return signalSuccess();

    } // spooler_process

}
