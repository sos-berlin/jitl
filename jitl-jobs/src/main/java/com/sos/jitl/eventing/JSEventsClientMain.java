package com.sos.jitl.eventing;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSToolBox;

public class JSEventsClientMain extends JSToolBox {

    protected JSEventsClientOptions objOptions = null;
    private static final Logger LOGGER = Logger.getLogger(JSEventsClientMain.class);

    public final static void main(final String[] pstrArgs) {
        final String conMethodName = "JSEventsClientMain::Main";
        LOGGER.info("JSEventsClient - Main");
        try {
            JSEventsClient objM = new JSEventsClient();
            JSEventsClientOptions objO = objM.getOptions();
            objO.commandLineArgs(pstrArgs);
            objM.execute();
        } catch (Exception e) {
            LOGGER.error(conMethodName + ": " + "Error occured ..." + e.getMessage());
            int intExitCode = 99;
            LOGGER.error(String.format("JSJ-E-105: %1$s - terminated with exit-code %2$d", conMethodName, intExitCode), e);
            System.exit(intExitCode);
        }
        LOGGER.info(String.format("JSJ-I-106: %1$s - ended without errors", conMethodName));
    }

}