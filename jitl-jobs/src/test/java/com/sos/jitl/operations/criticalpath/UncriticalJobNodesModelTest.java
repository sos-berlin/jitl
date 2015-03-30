package com.sos.jitl.operations.criticalpath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.operations.criticalpath.job.UncriticalJobNodesJobOptions;
import com.sos.jitl.operations.criticalpath.model.UncriticalJobNodesModel;


public class UncriticalJobNodesModelTest {
	private static Logger logger = LoggerFactory.getLogger(UncriticalJobNodesModelTest.class); //Logger.getLogger(FactJob.class);
		
	public static void main(String[] args) throws Exception {
		UncriticalJobNodesJobOptions opt = new UncriticalJobNodesJobOptions();
		
		opt.target_scheduler_host.Value("localhost");
		opt.target_scheduler_port.value(4646);
		opt.operation.Value("unskip");
		
		//opt.exclude_job_chains.Value("/sos");
		opt.include_job_chains.Value("/sos/notification/CleanupNotifications");
		opt.processing_recursive.value(false);
		
		UncriticalJobNodesModel model = new UncriticalJobNodesModel(opt);
		
		model.process();
		
		//System.out.println(Arrays.toString(new String[]{"1","2,3"}));
		
	}

}
