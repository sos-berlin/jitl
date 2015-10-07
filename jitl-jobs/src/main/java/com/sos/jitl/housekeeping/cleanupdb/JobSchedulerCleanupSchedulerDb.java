package com.sos.jitl.housekeeping.cleanupdb;

import java.io.File;
import java.sql.Connection;

import org.apache.log4j.Logger;

import sos.jadehistory.db.JadeFilesDBLayer;
import sos.jadehistory.db.JadeFilesHistoryDBLayer;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.dailyschedule.db.DailyScheduleDBLayer;
import com.sos.hibernate.classes.SosHibernateSession;
import com.sos.hibernate.options.HibernateOptions;
import com.sos.scheduler.history.db.SchedulerOrderHistoryDBLayer;
import com.sos.scheduler.history.db.SchedulerTaskHistoryDBLayer;
import com.sos.scheduler.messages.JSMessages;
 
public class JobSchedulerCleanupSchedulerDb extends JSJobUtilitiesClass<JobSchedulerCleanupSchedulerDbOptions> {
	private final String							conClassName		= "JobSchedulerCleanupSchedulerDb";						//$NON-NLS-1$
	private static Logger							logger				= Logger.getLogger(JobSchedulerCleanupSchedulerDb.class);

	protected JobSchedulerCleanupSchedulerDbOptions	objOptions			= null;
	private final JSJobUtilities							objJSJobUtilities	= this;

	/**
	 *
	 * \brief JobSchedulerCleanupSchedulerDb
	 *
	 * \details
	 *
	 */
	public JobSchedulerCleanupSchedulerDb() {
		super(new JobSchedulerCleanupSchedulerDbOptions());
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
	@Override
	public JobSchedulerCleanupSchedulerDbOptions getOptions() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Options"; //$NON-NLS-1$

		if (objOptions == null) {
			objOptions = new JobSchedulerCleanupSchedulerDbOptions();
		}
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
			getOptions().CheckMandatory();
			logger.debug(getOptions().dirtyString());

		 	if (getOptions().cleanup_job_scheduler_history_execute.isTrue()) {
    			SchedulerOrderHistoryDBLayer schedulerOrderHistoryDBLayer = new SchedulerOrderHistoryDBLayer(new File(getOptions().hibernate_configuration_file.Value()));
    			if (!getOptions().delete_history_interval.isDirty()) {
    				getOptions().delete_history_interval.Value(getOptions().delete_interval.Value());
    			}
     			
                schedulerOrderHistoryDBLayer.beginTransaction(Connection.TRANSACTION_READ_UNCOMMITTED);
    			long i = schedulerOrderHistoryDBLayer.deleteInterval(getOptions().delete_history_interval.value(),getOptions().cleanup_jobscheduler_history_limit.value());
             
    			logger.info(String.format("%s records deleted from SCHEDULER_ORDER_HISTORY that are older than %s days", i, getOptions().delete_history_interval.Value()));
    
    			SchedulerTaskHistoryDBLayer schedulerTaskHistoryDBLayer = new SchedulerTaskHistoryDBLayer(new File(getOptions().hibernate_configuration_file.Value()));
    			schedulerTaskHistoryDBLayer.beginTransaction(Connection.TRANSACTION_READ_UNCOMMITTED);
    			i = schedulerTaskHistoryDBLayer.deleteInterval(getOptions().delete_history_interval.value(),getOptions().cleanup_jobscheduler_history_limit.value());
    
    			logger.info(String.format("%s records deleted from SCHEDULER_HISTORY that are older than %s days", i, getOptions().delete_history_interval.Value()));
			}else {
                logger.info("Records in SCHEDULER_ORDER_HISTORY and SCHEDULER_HISTORY will not be deleted");
			}
			 
          if (getOptions().cleanup_daily_plan_execute.isTrue()) {
    			DailyScheduleDBLayer dailyScheduleDBLayer = new DailyScheduleDBLayer(new File(getOptions().hibernate_configuration_file.Value()));
                dailyScheduleDBLayer.beginTransaction(Connection.TRANSACTION_READ_UNCOMMITTED);
    			if (!getOptions().delete_daily_plan_interval.isDirty()) {
    				getOptions().delete_daily_plan_interval.Value(getOptions().delete_interval.Value());
    			}
    			long i = dailyScheduleDBLayer.deleteInterval(getOptions().delete_daily_plan_interval.value(),getOptions().cleanup_daily_plan_limit.value());
    			logger.info(String.format("%s records deleted from DAYS_SCHEDULE that are older than %s days", i, getOptions().delete_history_interval.Value()));
            }else {
                logger.info("Records in DAYS_SCHEDULE will not be deleted");
            }
			 
            if (getOptions().cleanup_jade_history_execute.isTrue()) {
                JadeFilesDBLayer jadeFilesDBLayer = new JadeFilesDBLayer(new File(getOptions().hibernate_configuration_file.Value()));
                jadeFilesDBLayer.beginTransaction(Connection.TRANSACTION_READ_UNCOMMITTED);
    			if (!getOptions().delete_jade_history_interval.isDirty()) {
    				getOptions().delete_jade_history_interval.Value(getOptions().delete_interval.Value());
    			}
    			long i = jadeFilesDBLayer.deleteInterval(getOptions().delete_jade_history_interval.value(),getOptions().cleanup_jade_history_limit.value());
    			logger.info(String.format("%s records deleted from JADE_FILES that are older than %s days", i, getOptions().delete_jade_history_interval.Value()));
    			
                JadeFilesHistoryDBLayer jadeFilesHistoryDBLayer = new JadeFilesHistoryDBLayer(new File(getOptions().hibernate_configuration_file.Value()));
                jadeFilesHistoryDBLayer.beginTransaction(Connection.TRANSACTION_READ_UNCOMMITTED);
                if (!getOptions().delete_jade_history_interval.isDirty()) {
                    getOptions().delete_jade_history_interval.Value(getOptions().delete_interval.Value());
                }
                
                i = jadeFilesHistoryDBLayer.deleteInterval(getOptions().delete_jade_history_interval.value(),getOptions().cleanup_jade_history_limit.value());
                logger.info(String.format("%s records deleted from JADE_FILES_HISTORY that are older than %s days", i, getOptions().delete_jade_history_interval.Value()));

            }else {
                logger.info("Records in JADE_FILES will not be deleted");
            }

			 

		}
		catch (Exception e) {
			String strM = String.format(JSMessages.JSJ_F_107.get(), conMethodName);
			throw new JobSchedulerException(strM, e);
		}
		finally {
		    SosHibernateSession.close();
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

} // class JobSchedulerCleanupSchedulerDb