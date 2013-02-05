package com.sos.jitl.housekeeping.cleanupdb;

import java.io.File;

import org.apache.log4j.Logger;

import sos.ftphistory.db.JadeFilesDBLayer;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.dailyschedule.db.DailyScheduleDBLayer;
import com.sos.scheduler.history.db.SchedulerOrderHistoryDBLayer;
import com.sos.scheduler.history.db.SchedulerTaskHistoryDBLayer;
import com.sos.scheduler.messages.JSMessages;

/**
 * \class 		JobSchedulerCleanupSchedulerDb - Workerclass for "Delete log entries in the Job Scheduler history Databaser tables"
 *
 * \brief AdapterClass of JobSchedulerCleanupSchedulerDb for the SOSJobScheduler
 *
 * This Class JobSchedulerCleanupSchedulerDb is the worker-class.
 *

 *
 * see \see C:\Dokumente und Einstellungen\Uwe Risse\Lokale Einstellungen\Temp\scheduler_editor-3271913404894833399.html for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Dokumente und Einstellungen\Uwe Risse\Eigene Dateien\sos-berlin.com\jobscheduler\scheduler_ur_current\config\JOETemplates\java\xsl\JSJobDoc2JSWorkerClass.xsl from http://www.sos-berlin.com at 20121211160841
 * \endverbatim
 */
public class JobSchedulerCleanupSchedulerDb extends JSToolBox implements JSJobUtilities {
	private final String							conClassName		= "JobSchedulerCleanupSchedulerDb";						//$NON-NLS-1$
	private static Logger							logger				= Logger.getLogger(JobSchedulerCleanupSchedulerDb.class);

	protected JobSchedulerCleanupSchedulerDbOptions	objOptions			= null;
	private JSJobUtilities							objJSJobUtilities	= this;

	/**
	 *
	 * \brief JobSchedulerCleanupSchedulerDb
	 *
	 * \details
	 *
	 */
	public JobSchedulerCleanupSchedulerDb() {
		super("com_sos_scheduler_messages");
	}

	/**
	 *
	 * \brief Options - returns the JobSchedulerCleanupSchedulerDbOptionClass
	 *
	 * \details
	 * The JobSchedulerCleanupSchedulerDbOptionClass is used as a Container for all Options (Settings) which are
	 * needed.
	 *
	 * \return JobSchedulerCleanupSchedulerDbOptions
	 *
	 */
	public JobSchedulerCleanupSchedulerDbOptions Options() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Options"; //$NON-NLS-1$

		if (objOptions == null) {
			objOptions = new JobSchedulerCleanupSchedulerDbOptions();
		}
		return objOptions;
	}

	/**
	 *
	 * \brief Options - set the JobSchedulerCleanupSchedulerDbOptionClass
	 *
	 * \details
	 * The JobSchedulerCleanupSchedulerDbOptionClass is used as a Container for all Options (Settings) which are
	 * needed.
	 *
	 * \return JobSchedulerCleanupSchedulerDbOptions
	 *
	 */
	public JobSchedulerCleanupSchedulerDbOptions Options(final JobSchedulerCleanupSchedulerDbOptions pobjOptions) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Options"; //$NON-NLS-1$

		objOptions = pobjOptions;
		return objOptions;
	}

	/**
	 *
	 * \brief Execute - Start the Execution of JobSchedulerCleanupSchedulerDb
	 *
	 * \details
	 *
	 * For more details see
	 *
	 * \see JobSchedulerAdapterClass
	 * \see JobSchedulerCleanupSchedulerDbMain
	 *
	 * \return JobSchedulerCleanupSchedulerDb
	 *
	 * @return
	 */
	public JobSchedulerCleanupSchedulerDb Execute() throws Exception {
		final String conMethodName = conClassName + "::Execute"; //$NON-NLS-1$

		logger.debug(String.format(JSMessages.JSJ_I_110.get(), conMethodName));

		try {
			Options().CheckMandatory();
			logger.debug(Options().toString());

			SchedulerOrderHistoryDBLayer schedulerOrderHistoryDBLayer = new SchedulerOrderHistoryDBLayer(new File(
					Options().hibernate_configuration_file.Value()));
			if (!Options().delete_history_interval.isDirty()) {
				Options().delete_history_interval.Value(Options().delete_interval.Value());
			}
			int i = schedulerOrderHistoryDBLayer.deleteInterval(Options().delete_history_interval.Value());
			logger.info(String.format("%s records deleted from SCHEDULER_ORDER_HISTORY from the last %s days", i, Options().delete_history_interval.Value()));

			SchedulerTaskHistoryDBLayer schedulerTaskHistoryDBLayer = new SchedulerTaskHistoryDBLayer(new File(Options().hibernate_configuration_file.Value()));
			schedulerTaskHistoryDBLayer.setSession(schedulerOrderHistoryDBLayer);
			schedulerTaskHistoryDBLayer.beginTransaction();
			i = schedulerTaskHistoryDBLayer.deleteInterval(Options().delete_history_interval.Value());

			logger.info(String.format("%s records deleted from SCHEDULER_HISTORY from the last %s days", i, Options().delete_history_interval.Value()));

			DailyScheduleDBLayer dailyScheduleDBLayer = new DailyScheduleDBLayer(new File(Options().hibernate_configuration_file.Value()));
			dailyScheduleDBLayer.setSession(schedulerOrderHistoryDBLayer);
			if (!Options().delete_daily_plan_interval.isDirty()) {
				Options().delete_daily_plan_interval.Value(Options().delete_interval.Value());
			}
			i = dailyScheduleDBLayer.deleteInterval(Options().delete_daily_plan_interval.Value());
			logger.info(String.format("%s records deleted from DAYS_SCHEDULE from the last %s days", i, Options().delete_history_interval.Value()));

			JadeFilesDBLayer jadeFilesDBLayer = new JadeFilesDBLayer(new File(Options().hibernate_configuration_file.Value()));
			jadeFilesDBLayer.setSession(schedulerOrderHistoryDBLayer);
			if (!Options().delete_ftp_history_interval.isDirty()) {
				Options().delete_ftp_history_interval.Value(Options().delete_interval.Value());
			}
			i = jadeFilesDBLayer.deleteInterval(Options().delete_ftp_history_interval.Value());
			logger.info(String.format("%s records deleted from SOS_FTP_FILES from the last %s days", i, Options().delete_ftp_history_interval.Value()));

			schedulerOrderHistoryDBLayer.commit();
			schedulerOrderHistoryDBLayer.closeSession();

		}
		catch (Exception e) {
			String strM =  String.format(JSMessages.JSJ_F_107.get(), conMethodName);
			throw new JobSchedulerException(strM, e);
		}
		finally {
			logger.debug(String.format(JSMessages.JSJ_I_111.get(), conMethodName));
		}

		return this;
	}

	public void init() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::init"; //$NON-NLS-1$
		doInitialize();
	}

	private void doInitialize() {
	} // doInitialize

	@Override
	public String myReplaceAll(final String pstrSourceString, final String pstrReplaceWhat, final String pstrReplaceWith) {

		String newReplacement = pstrReplaceWith.replaceAll("\\$", "\\\\\\$");
		return pstrSourceString.replaceAll("(?m)" + pstrReplaceWhat, newReplacement);
	}

	/**
	 *
	 * \brief replaceSchedulerVars
	 *
	 * \details
	 * Dummy-Method to make sure, that there is always a valid Instance for the JSJobUtilities.
	 * \return
	 *
	 * @param isWindows
	 * @param pstrString2Modify
	 * @return
	 */
	@Override
	public String replaceSchedulerVars(final boolean isWindows, final String pstrString2Modify) {
		logger.debug("replaceSchedulerVars as Dummy-call executed. No Instance of JobUtilites specified.");
		return pstrString2Modify;
	}

	/**
	 *
	 * \brief setJSParam
	 *
	 * \details
	 * Dummy-Method to make shure, that there is always a valid Instance for the JSJobUtilities.
	 * \return
	 *
	 * @param pstrKey
	 * @param pstrValue
	 */
	@Override
	public void setJSParam(final String pstrKey, final String pstrValue) {

	}

	@Override
	public void setJSParam(final String pstrKey, final StringBuffer pstrValue) {

	}

	/**
	 *
	 * \brief setJSJobUtilites
	 *
	 * \details
	 * The JobUtilities are a set of methods used by the SSH-Job or can be used be other, similar, job-
	 * implementations.
	 *
	 * \return void
	 *
	 * @param pobjJSJobUtilities
	 */
	@Override
	public void setJSJobUtilites(final JSJobUtilities pobjJSJobUtilities) {

		if (pobjJSJobUtilities == null) {
			objJSJobUtilities = this;
		}
		else {
			objJSJobUtilities = pobjJSJobUtilities;
		}
		logger.debug("objJSJobUtilities = " + objJSJobUtilities.getClass().getName());
	}

	@Override
	public String getCurrentNodeName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStateText(final String pstrStateText) {
		// TODO Auto-generated method stub

	}

} // class JobSchedulerCleanupSchedulerDb