package com.sos.jitl.operations.criticalpath.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSToolBox;

public class UncriticalJobNodesJobMain extends JSToolBox {

    private final static String className = UncriticalJobNodesJobMain.class.getSimpleName();
    private static Logger logger = LoggerFactory.getLogger(UncriticalJobNodesJobMain.class);

    public final static void main(String[] args) {
        final String methodName = className + "::main";

        logger.info(String.format(methodName));
        int exitCode = 0;
        try {
            UncriticalJobNodesJob job = new UncriticalJobNodesJob();
            UncriticalJobNodesJobOptions options = job.getOptions();
            options.CommandLineArgs(args);

            job.execute();
            logger.info(String.format("JSJ-I-106: %1$s - ended without errors", methodName));
        } catch (Exception e) {
            exitCode = 99;
            logger.error(String.format("JSJ-E-105: %1$s - terminated with exit-code %2$d", methodName, exitCode), e);
        }
        System.exit(exitCode);
    }
}