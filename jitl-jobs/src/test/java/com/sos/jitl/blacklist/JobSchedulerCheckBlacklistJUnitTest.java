package com.sos.jitl.blacklist;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.jitl.checkblacklist.JobSchedulerCheckBlacklistOptions;

public class JobSchedulerCheckBlacklistJUnitTest extends JSToolBox {

    @SuppressWarnings("unused")//$NON-NLS-1$
    private final static String conClassName = "JobSchedulerCheckBlacklistJUnitTest"; //$NON-NLS-1$
    @SuppressWarnings("unused")//$NON-NLS-1$
    private static Logger logger = Logger.getLogger(JobSchedulerCheckBlacklistJUnitTest.class);

    protected JobSchedulerCheckBlacklistOptions objOptions = null;

    public JobSchedulerCheckBlacklistJUnitTest() {
        //
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

}  // class JobSchedulerCheckBlacklistJUnitTest