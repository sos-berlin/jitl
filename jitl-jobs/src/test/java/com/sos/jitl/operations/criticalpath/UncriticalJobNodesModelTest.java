package com.sos.jitl.operations.criticalpath;

import com.sos.jitl.operations.criticalpath.job.UncriticalJobNodesJobOptions;
import com.sos.jitl.operations.criticalpath.model.UncriticalJobNodesModel;

public class UncriticalJobNodesModelTest {

    public static void main(String[] args) throws Exception {
        UncriticalJobNodesJobOptions opt = new UncriticalJobNodesJobOptions();
        opt.target_scheduler_host.setValue("localhost");
        opt.target_scheduler_port.value(4646);
        opt.operation.setValue("unskip");
        opt.include_job_chains.setValue("/sos/notification/CleanupNotifications");
        opt.processing_recursive.value(false);
        UncriticalJobNodesModel model = new UncriticalJobNodesModel(opt);
        model.process();
    }

}