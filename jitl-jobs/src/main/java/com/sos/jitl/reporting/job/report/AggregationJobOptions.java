package com.sos.jitl.reporting.job.report;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.jitl.reporting.job.ReportingJobOptionsSuperClass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * \class AggregationJobOptions - Aggregation
 * 
 * \brief An Options as a container for the Options super class. The Option
 * class will hold all the things, which would be otherwise overwritten at a
 * re-creation of the super-class.
 * 
 */
@JSOptionClass(name = "AggregationJobOptions", description = "AggregationJobOptions")
public class AggregationJobOptions extends ReportingJobOptionsSuperClass {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String conClassName = AggregationJobOptions.class
			.getSimpleName();
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(AggregationJobOptions.class);

	
	/**
	 * \var do_process :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "do_process", description = "", key = "do_process", type = "SOSOptionBoolean", mandatory = true)
	public SOSOptionBoolean do_process = new SOSOptionBoolean(
			this, conClassName + ".do_process", // HashMap-Key
			"", // Titel
			"true", // InitValue
			"true", // DefaultValue 
			true // isMandatory
	);

	/**
	 * \brief getdo_process :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionBoolean getdo_process() {
		return do_process;
	}

	/**
	 * \brief setdo_process :
	 * 
	 * \details
	 * 
	 * 
	 * @param do_process
	 *            :
	 */
	public void setdo_process(
			SOSOptionBoolean p_do_process) {
		this.do_process = p_do_process;
	}

	/**
	 * \var batch_size :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "batch_size", description = "", key = "batch_size", type = "SOSOptionInteger", mandatory = true)
	public SOSOptionInteger batch_size = new SOSOptionInteger(
			this, conClassName + ".batch_size", // HashMap-Key
			"", // Titel
			"100", // InitValue
			"100", // DefaultValue
			true // isMandatory
	);

	/**
	 * \brief getbatch_size :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionInteger getbatch_size() {
		return batch_size;
	}

	/**
	 * \brief setbatch_size :
	 * 
	 * \details
	 * 
	 * 
	 * @param batch_size
	 *            :
	 */
	public void setbatch_size(
			SOSOptionInteger p_batch_size) {
		this.batch_size = p_batch_size;
	}

	/**
	 * \var log_info_step :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "log_info_step", description = "", key = "log_info_step", type = "SOSOptionInteger", mandatory = true)
	public SOSOptionInteger log_info_step = new SOSOptionInteger(
			this, conClassName + ".log_info_step", // HashMap-Key
			"", // Titel
			"10000", // InitValue
			"10000", // DefaultValue
			true // isMandatory
	);

	/**
	 * \brief getlog_info_step :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionInteger getlog_info_step() {
		return log_info_step;
	}

	/**
	 * \brief setlog_info_step :
	 * 
	 * \details
	 * 
	 * 
	 * @param log_info_step
	 *            :
	 */
	public void setlog_info_step(
			SOSOptionInteger p_log_step) {
		this.log_info_step = p_log_step;
	}

	
	/**
	 * \var force_update_from_inventory :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "force_update_from_inventory", description = "", key = "force_update_from_inventory", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean force_update_from_inventory = new SOSOptionBoolean(
			this, conClassName + ".force_update_from_inventory", // HashMap-Key
			"", // Titel
			"false", // InitValue
			"false", // DefaultValue 
			false // isMandatory
	);

	/**
	 * \brief getforce_update_from_inventory :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionBoolean getforce_update_from_inventory() {
		return force_update_from_inventory;
	}

	/**
	 * \brief setforce_update_from_inventory :
	 * 
	 * \details
	 * 
	 * 
	 * @param force_update_from_inventory
	 *            :
	 */
	public void setforce_update_from_inventory(
			SOSOptionBoolean val) {
		this.force_update_from_inventory = val;
	}

	
	
	
	/**
	 * constructors
	 */

	public AggregationJobOptions() {
	}

	/**
	 * 
	 * @param listener
	 */
	public AggregationJobOptions(JSListener listener) {
		super(listener);
	}

	/**
	 * 
	 * @param jsSettings
	 * @throws Exception
	 */
	public AggregationJobOptions(HashMap<String, String> jsSettings)
			throws Exception {
		super(jsSettings);
	}

	/**
	 * \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
	 * 
	 * \details
	 * 
	 * @throws Exception
	 * 
	 * @throws Exception
	 *             - wird ausgelöst, wenn eine mandatory-Option keinen Wert hat
	 */
	@Override
	public void CheckMandatory() {
		try {
			super.CheckMandatory();
		} catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	}
}
