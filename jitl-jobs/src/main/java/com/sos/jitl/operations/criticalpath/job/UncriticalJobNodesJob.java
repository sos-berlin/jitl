package com.sos.jitl.operations.criticalpath.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.spooler.Spooler;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.jitl.operations.criticalpath.model.UncriticalJobNodesModel;

public class UncriticalJobNodesJob extends JSJobUtilitiesClass<UncriticalJobNodesJobOptions> {
	private final String className = UncriticalJobNodesJob.class.getSimpleName();
	private static Logger logger = LoggerFactory.getLogger(UncriticalJobNodesJob.class);
	private Spooler spooler;
	
	public UncriticalJobNodesJob() {
		super(new UncriticalJobNodesJobOptions());
	}

	public UncriticalJobNodesJob execute() throws Exception {
		final String methodName = className + "::execute";

		logger.debug(methodName);

		try { 
			Options().CheckMandatory();
			logger.debug(Options().toString());
			
			UncriticalJobNodesModel model = new UncriticalJobNodesModel(Options());
			model.setSpooler(spooler);
			model.process();
			
		}
		catch (Exception e) {
			logger.error(String.format("%s: %s", methodName, e.toString()));
			throw e;			
		}
		
		return this;
	}
	
	public void setSpooler(Spooler sp){
		spooler = sp;
	}
	
	public UncriticalJobNodesJobOptions Options() {
		if (objOptions == null) {
			objOptions = new UncriticalJobNodesJobOptions();
		}
		return objOptions;
	}

}  