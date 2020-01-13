package com.sos.jitl.blacklist;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.jitl.checkblacklist.JobSchedulerCheckBlacklistOptions;

public class JobSchedulerCheckBlacklistOptionsJUnitTest extends JSToolBox {

    private final String conClassName = "JobSchedulerCheckBlacklistOptionsJUnitTest"; //$NON-NLS-1$
    protected JobSchedulerCheckBlacklistOptions objOptions = null;

    public JobSchedulerCheckBlacklistOptionsJUnitTest() {
        //
    }

    @Before
    public void setUp() throws Exception {
        objOptions = new JobSchedulerCheckBlacklistOptions();
        objOptions.registerMessageListener(this);
        JSListenerClass.bolLogDebugInformation = true;
        JSListenerClass.intMaxDebugLevel = 9;
    }

    /** \brief testgranuality :
     * 
     * \details Defines the start of the job or the job_chain order: for each
     * order that is in a blacklist jobchain: for each job chain that has a
     * blacklist. blacklist: One start when a blacklist exists. */
    @Test
    public void testgranuality() {  // SOSOptionString
        objOptions.granuality.setValue("++blacklist++");
        assertEquals("", objOptions.granuality.getValue(), "++blacklist++");
    }

    /** \brief testjob :
     * 
     * \details The name of the job that should be startet Parameters of the job
     * filename: name of the file that is in the blacklist job_chain: name of
     * the job_chain that has a blacklist. created: creation time of the order
     * which is in the blacklist */
    @Test
    public void testjob() {  // SOSOptionString
        objOptions.job.setValue("++----++");
        assertEquals("", objOptions.job.getValue(), "++----++");
    }

    /** \brief testjob_chain : The name of the job chain that should be startet
     * Paramet
     * 
     * \details The name of the job chain that should be startet Parameters of
     * the order filename: name of the file that is in the blacklist job_chain:
     * name of the job_chain that has a blacklist. created: creation time of the
     * order which is in the blacklist */
    @Test
    public void testjob_chain() {  // SOSOptionString
        objOptions.job_chain.setValue("++----++");
        assertEquals("The name of the job chain that should be startet Paramet", objOptions.job_chain.getValue(), "++----++");
    }

    /** \brief testlevel :
     * 
     * \details Specifies the log entry info: a info entry will be made warning:
     * a warn entry will be made error: an error entry will be made */
    @Test
    public void testlevel() {  // SOSOptionString
        objOptions.level.setValue("++info++");
        assertEquals("", objOptions.level.getValue(), "++info++");
    }

}