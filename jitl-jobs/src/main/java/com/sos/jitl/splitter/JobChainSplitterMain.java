package com.sos.jitl.splitter;

import com.sos.JSHelper.Basics.JSToolBox;
import org.apache.log4j.Logger;

public class JobChainSplitterMain extends JSToolBox {

    protected JobChainSplitterOptions objOptions = null;
    private static final Logger LOGGER = Logger.getLogger(JobChainSplitterMain.class);

    public final static void main(String[] pstrArgs) {
        final String methodName = "JobChainSplitterMain::Main";
        LOGGER.info("JobChainSplitter - Main");
        try {
            JobChainSplitter objM = new JobChainSplitter();
            JobChainSplitterOptions objO = objM.getOptions();
            objO.CommandLineArgs(pstrArgs);
            objM.Execute();
        } catch (Exception e) {
            LOGGER.error(methodName + ": " + "Error occured ..." + e.getMessage());
            int intExitCode = 99;
            LOGGER.error(String.format("JSJ-E-105: %1$s - terminated with exit-code %2$d", methodName, intExitCode), e);
            System.exit(intExitCode);
        }
        LOGGER.info(String.format("JSJ-I-106: %1$s - ended without errors", methodName));
    }

}