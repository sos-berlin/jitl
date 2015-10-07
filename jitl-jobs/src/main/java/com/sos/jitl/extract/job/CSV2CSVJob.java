package com.sos.jitl.extract.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.jitl.extract.model.CSV2CSVModel;

/**
 * 
 * @author Robert Ehrlich
 *
 */
public class CSV2CSVJob extends JSJobUtilitiesClass<CSV2CSVJobOptions> {
	private final String conClassName = CSV2CSVJob.class.getSimpleName(); //$NON-NLS-1$
	private static Logger logger = LoggerFactory.getLogger(CSV2CSVJob.class); //Logger.getLogger(FactJob.class);

	/**
	 * 
	 */
	public CSV2CSVJob() {
		super(new CSV2CSVJobOptions());
	}

	/**
	 * 	
	 * @return
	 * @throws Exception
	 */
	public CSV2CSVJob Execute() throws Exception {
		final String conMethodName = conClassName + "::Execute";  //$NON-NLS-1$

		logger.debug(conMethodName);

		try { 
			getOptions().CheckMandatory();
			logger.debug(getOptions().toString());
			
			CSV2CSVModel model = new CSV2CSVModel(getOptions());
			model.process();
			
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			logger.error(String.format("%s: %s", conMethodName, e.toString()));
			throw e;			
		}
		
		return this;
	}
	
	/**
	 * 
	 */
	public CSV2CSVJobOptions getOptions() {

		@SuppressWarnings("unused")  //$NON-NLS-1$
		final String conMethodName = conClassName + "::Options";  //$NON-NLS-1$

		if (objOptions == null) {
			objOptions = new CSV2CSVJobOptions();
		}
		return objOptions;
	}

}  