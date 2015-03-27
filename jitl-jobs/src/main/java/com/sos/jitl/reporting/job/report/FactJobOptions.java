package com.sos.jitl.reporting.job.report;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.jitl.reporting.job.ReportingJobOptionsSuperClass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * \class FactJobOptions - Fact
 * 
 * \brief An Options as a container for the Options super class. The Option
 * class will hold all the things, which would be otherwise overwritten at a
 * re-creation of the super-class.
 * 
 */
@JSOptionClass(name = "FactJobOptions", description = "FactJobOptions")
public class FactJobOptions extends ReportingJobOptionsSuperClass {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String conClassName = FactJobOptions.class
			.getSimpleName();
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(FactJobOptions.class);

	
	/**
	 * \var hibernate_configuration_file_scheduler :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "hibernate_configuration_file_scheduler", description = "", key = "hibernate_configuration_file_scheduler", type = "SOSOptionString", mandatory = true)
	public SOSOptionString hibernate_configuration_file_scheduler = new SOSOptionString(
			this, conClassName + ".hibernate_configuration_file_scheduler", // HashMap-Key
			"", // Titel
			"", // InitValue
			"", // DefaultValue
			true // isMandatory
	);

	/**
	 * \brief gethibernate_configuration_file_scheduler :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString gethibernate_configuration_file_scheduler() {
		return hibernate_configuration_file_scheduler;
	}

	/**
	 * \brief sethibernate_configuration_file_scheduler :
	 * 
	 * \details
	 * 
	 * 
	 * @param hibernate_configuration_file_scheduler
	 *            :
	 */
	public void sethibernate_configuration_file_scheduler(SOSOptionString val) {
		this.hibernate_configuration_file_scheduler = val;
	}
	
	/**
	 * \var max_read_history_interval in minutes :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "max_history_age", description = "", key = "max_history_age", type = "SOSOptionString", mandatory = false)
	public SOSOptionString max_history_age = new SOSOptionString(
			this, conClassName + ".max_history_age", // HashMap-Key
			"", // Titel
			"1w", // InitValue
			"1w", // DefaultValue 1 week (1w 1d 1h 1m)
			false // isMandatory
	);

	/**
	 * \brief getmax_history_age :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getmax_history_age() {
		return max_history_age;
	}

	/**
	 * \brief setmax_history_age :
	 * 
	 * \details
	 * 
	 * 
	 * @param max_history_age
	 *            :
	 */
	public void setmax_history_age(
			SOSOptionString p_max_history_age) {
		this.max_history_age = p_max_history_age;
	}

	/**
	 * \var force_max_history_age :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "force_max_history_age", description = "", key = "force_max_history_age", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean force_max_history_age = new SOSOptionBoolean(
			this, conClassName + ".force_max_history_age", // HashMap-Key
			"", // Titel
			"false", // InitValue
			"false", // DefaultValue 
			false // isMandatory
	);

	/**
	 * \brief getforce_max_history_age :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionBoolean getforce_max_history_age() {
		return force_max_history_age;
	}

	/**
	 * \brief setforce_max_history_age :
	 * 
	 * \details
	 * 
	 * 
	 * @param force_max_history_age
	 *            :
	 */
	public void setforce_max_history_age(
			SOSOptionBoolean p_force_max_history_age) {
		this.force_max_history_age = p_force_max_history_age;
	}

	/**
	 * \var batch_size :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "batch_size", description = "", key = "batch_size", type = "SOSOptionInteger", mandatory = false)
	public SOSOptionInteger batch_size = new SOSOptionInteger(
			this, conClassName + ".batch_size", // HashMap-Key
			"", // Titel
			"100", // InitValue
			"100", // DefaultValue
			false // isMandatory
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
	@JSOptionDefinition(name = "log_info_step", description = "", key = "log_info_step", type = "SOSOptionInteger", mandatory = false)
	public SOSOptionInteger log_info_step = new SOSOptionInteger(
			this, conClassName + ".log_info_step", // HashMap-Key
			"", // Titel
			"10000", // InitValue
			"10000", // DefaultValue
			false // isMandatory
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
	 * \var connection_transaction_isolation_scheduler :
	 * Default 2 wegen Oracle, weil Oracle kein TRANSACTION_READ_UNCOMMITTED unterstützt, sonst wäre 1
	 * 
	 */
	@JSOptionDefinition(name = "connection_transaction_isolation_scheduler", description = "", key = "connection_transaction_isolation_scheduler", type = "SOSOptionInterval", mandatory = false)
	public SOSOptionInteger connection_transaction_isolation_scheduler = new SOSOptionInteger(
			this, conClassName + ".connection_transaction_isolation_scheduler", // HashMap-Key
			"", // Titel
			"2", // InitValue
			"2", // 1 - READ_UNCOMMITED, 2 -READ_COMMITED
			false // isMandatory
	);

	/**
	 * \brief getconnection_transaction_isolation_scheduler :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionInteger getconnection_transaction_isolation_scheduler() {
		return connection_transaction_isolation_scheduler;
	}

	/**
	 * \brief setconnection_transaction_isolation_scheduler :
	 * 
	 * \details
	 * 
	 * 
	 * @param connection_transaction_isolation_scheduler
	 *            :
	 */
	public void setconnection_transaction_isolation_scheduler(
			SOSOptionInteger p_connection_transaction_isolation) {
		this.connection_transaction_isolation_scheduler = p_connection_transaction_isolation;
	}

	/**
	 * \var connection_autocommit_scheduler :
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "connection_autocommit_scheduler", description = "", key = "connection_autocommit_scheduler", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean connection_autocommit_scheduler = new SOSOptionBoolean(
			this, conClassName + ".connection_autocommit_scheduler", // HashMap-Key
			"", // Titel
			"true", // InitValue
			"true", // 
			false // isMandatory
	);

	/**
	 * \brief getconnection_autocommit_scheduler :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionBoolean getconnection_autocommit_scheduler() {
		return connection_autocommit_scheduler;
	}

	/**
	 * \brief setconnection_autocommit_scheduler :
	 * 
	 * \details
	 * 
	 * 
	 * @param connection_autocommit
	 *            :
	 */
	public void setconnection_autocommit_scheduler(
			SOSOptionBoolean p_connection_autocommit) {
		this.connection_autocommit_scheduler = p_connection_autocommit;
	}

	
   /**
	 * orders mit endTime null werden als uncompleted markiert und werden immer wieder synchronisiert.
	 * max Differenze zwischen currentTime und startTime in Minuten um den "uncompleted" Zustand bei der Synchronisierung zu reduzieren.
	 * 
	 * 
	 */
	@JSOptionDefinition(name = "max_uncompleted_age", description = "", key = "max_uncompleted_age", type = "SOSOptionString", mandatory = false)
	public SOSOptionString max_uncompleted_age = new SOSOptionString(
			this, conClassName + ".max_uncompleted_age", // HashMap-Key
			"", // Titel
			"1d", // InitValue
			"1d", // DefaultValue 1 day (1w 1d 1h 1m)
			false // isMandatory
	);

	/**
	 * \brief getmax_uncompleted_age :
	 * 
	 * \details
	 * 
	 * 
	 * \return
	 * 
	 */
	public SOSOptionString getmax_uncompleted_age() {
		return max_uncompleted_age;
	}

	/**
	 * \brief setmax_uncompleted_age :
	 * 
	 * \details
	 * 
	 * 
	 * @param max_uncompleted_age
	 *            :
	 */
	public void setmax_uncompleted_age(
			SOSOptionString p_max_uncompleted_age) {
		this.max_uncompleted_age = p_max_uncompleted_age;
	}

	
	/**
	 * constructors
	 */

	public FactJobOptions() {
	}

	/**
	 * 
	 * @param listener
	 */
	public FactJobOptions(JSListener listener) {
		super(listener);
	}

	/**
	 * 
	 * @param jsSettings
	 * @throws Exception
	 */
	public FactJobOptions(HashMap<String, String> jsSettings)
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
