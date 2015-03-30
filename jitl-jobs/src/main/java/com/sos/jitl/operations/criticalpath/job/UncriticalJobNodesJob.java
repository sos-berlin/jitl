package com.sos.jitl.operations.criticalpath.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.spooler.Spooler;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.jitl.operations.criticalpath.model.UncriticalJobNodesModel;

/**
 * 
 * @author Robert Ehrlich
 *
 */
public class UncriticalJobNodesJob extends JSJobUtilitiesClass<UncriticalJobNodesJobOptions> {
	private final String conClassName = UncriticalJobNodesJob.class.getSimpleName(); //$NON-NLS-1$
	private static Logger logger = LoggerFactory.getLogger(UncriticalJobNodesJob.class); //Logger.getLogger(FactJob.class);
	private Spooler spooler;
	
	/**
	 * 
	 */
	public UncriticalJobNodesJob() {
		super(new UncriticalJobNodesJobOptions());
	}

	/**
	 * 	
	 * @return
	 * @throws Exception
	 */
	public UncriticalJobNodesJob Execute() throws Exception {
		final String conMethodName = conClassName + "::Execute";  //$NON-NLS-1$

		logger.debug(conMethodName);

		try { 
			Options().CheckMandatory();
			logger.debug(Options().toString());
			
			UncriticalJobNodesModel model = new UncriticalJobNodesModel(Options());
			model.setSpooler(spooler);
			model.process();
			
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			logger.error(String.format("%s: %s", conMethodName, e.toString()));
			throw e;			
		}
		
		return this;
	}
	
	public void setSpooler(Spooler sp){
		spooler = sp;
	}
	
	/**
	 * 
	 */
	public UncriticalJobNodesJobOptions Options() {

		@SuppressWarnings("unused")  //$NON-NLS-1$
		final String conMethodName = conClassName + "::Options";  //$NON-NLS-1$

		if (objOptions == null) {
			objOptions = new UncriticalJobNodesJobOptions();
		}
		return objOptions;
	}

}  