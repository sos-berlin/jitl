package com.sos.jitl.extract.job;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Robert Ehrlich
 *
 */
@JSOptionClass(name = "ResultSet2CSVJobOptions", description = "ResultSet2CSV")
public class ResultSet2CSVJobOptions extends ResultSet2CSVJobOptionsSuperClass {
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private final static String conClassName = ResultSet2CSVJobOptions.class
			.getSimpleName();
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(ResultSet2CSVJobOptions.class);

	/**
	 * 
	 */
   	public ResultSet2CSVJobOptions() {
	}

   	/**
   	 * 
   	 * @param pobjListener
   	 */
	public ResultSet2CSVJobOptions(JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} 

	/**
	 * 
	 * @param JSSettings
	 * @throws Exception
	 */
	public ResultSet2CSVJobOptions (HashMap <String, String> JSSettings) throws Exception {
		super(JSSettings);
	} 
	
	@Override
	public void CheckMandatory() {
		try {
			super.CheckMandatory();
		}
		catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	}
}

