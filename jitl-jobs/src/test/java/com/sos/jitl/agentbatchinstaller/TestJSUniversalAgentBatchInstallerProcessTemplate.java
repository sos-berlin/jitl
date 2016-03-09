package com.sos.jitl.agentbatchinstaller;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.junit.Test;

import sos.scheduler.misc.ParameterSubstitutor;

public class TestJSUniversalAgentBatchInstallerProcessTemplate {

    @Test
    public void testExecute() {
        JSUniversalAgentBatchInstallerProcessTemplate jsUniversalAgentBatchInstallerProcessTemplate = new JSUniversalAgentBatchInstallerProcessTemplate();
        File in = new File("C:/development_110/products/jitl/jitl-jobs/src/test/java/com/sos/jitl/agentbatchinstaller/jobscheduler_agent_instance_script.sh");
        File out = new File("C:/temp/1.txt");
        HashMap<String, String> h = new HashMap<String, String>();
        h.put("SCHEDULER_HOME", "mySCHEDULER_HOME");
        h.put("SCHEDULER_HTTP_PORT", "4446");

        jsUniversalAgentBatchInstallerProcessTemplate.execute(in, out, h);
    }

}
