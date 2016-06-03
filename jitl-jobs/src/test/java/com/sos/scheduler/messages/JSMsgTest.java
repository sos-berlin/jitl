package com.sos.scheduler.messages;

import static com.sos.scheduler.messages.JSMessages.JSJ_D_0032;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_110;

import org.junit.Before;
import org.junit.Test;

import com.sos.localization.SOSMsg;

public class JSMsgTest {

    private static final String CLASSNAME = "JSMsgTest";

    @Before
    public void setUp() throws Exception {
        SOSMsg.flgShowFullMessageText = true;
    }

    @Test(expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class)
    public void testJSMsgString() {
        JSMsg objMsg = new JSMsg("JSJ_F_107");
        objMsg.toLog(CLASSNAME);
    }

    @Test
    public void testJSMsg_I_110() {
        JSJ_I_110.toLog(CLASSNAME);
    }

    @Test
    public void testJSMsg_D_032() {
        JSMsg.VerbosityLevel = 5;
        JSJ_D_0032.toLog(CLASSNAME);
        JSMsg.VerbosityLevel = 1;
        JSJ_D_0032.toLog(CLASSNAME);
    }

}