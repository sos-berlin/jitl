package com.sos.jitl.housekeeping.rotatelog;

import static com.sos.JSHelper.Options.SOSOptionRegExp.strCaseInsensitive;
import static sos.scheduler.job.JobSchedulerConstants.JobSchedulerLogFileName;
import static sos.scheduler.job.JobSchedulerConstants.JobSchedulerLogFileNameExtension;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.Files.JSFile;
import com.sos.JSHelper.io.Files.JSFolder;
import com.sos.scheduler.messages.JSMsg;
/**
 * \class 		JobSchedulerRotateLog - Workerclass for "Rotate compress and delete log files"
 *
 * \brief AdapterClass of JobSchedulerRotateLog for the SOSJobScheduler
 *
 * This Class JobSchedulerRotateLog is the worker-class.
 *

 *
 * see \see C:\Users\KB\AppData\Local\Temp\scheduler_editor-1724231827372138737html for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by com/sos/resources/xsl/JSJobDoc2JSWorkerClass.xsl from http://www.sos-berlin.com at 20140906131052 
 * \endverbatim
 */
public class JobSchedulerRotateLog extends JSJobUtilitiesClass<JobSchedulerRotateLogOptions> {
	private final String		conClassName	= this.getClass().getSimpleName();
	private static final String	conSVNVersion	= "$Id$";
	private final Logger		logger			= Logger.getLogger(this.getClass());

	private String				strSchedulerID	= "";

	/**
	 * 
	 * \brief JobSchedulerRotateLog
	 *
	 * \details
	 *
	 */
	public JobSchedulerRotateLog() {
		super(new JobSchedulerRotateLogOptions());
	}

	/**
	 * 
	 * \brief Options - returns the JobSchedulerRotateLogOptionClass
	 * 
	 * \details
	 * The JobSchedulerRotateLogOptionClass is used as a Container for all Options (Settings) which are
	 * needed.
	 *  
	 * \return JobSchedulerRotateLogOptions
	 *
	 */
	@Override
	public JobSchedulerRotateLogOptions Options() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Options";

		if (objOptions == null) {
			objOptions = new JobSchedulerRotateLogOptions();
		}
		return objOptions;
	}

	/**
	 * 
	 * \brief Execute - Start the Execution of JobSchedulerRotateLog
	 * 
	 * \details
	 * 
	 * For more details see
	 * 
	 * \see JobSchedulerAdapterClass 
	 * \see JobSchedulerRotateLogMain
	 * 
	 * \return JobSchedulerRotateLog
	 *
	 * @return
	 */
	public boolean Execute()  {
		final String conMethodName = conClassName + "::Execute";
		int intNoOfCompressedLogFilesDeleted = 0;
		int intNoOfLogFilesCompressed = 0;
		String deleteSchedulerLogFileSpec = "";
		String strRegExpr4CompressedFiles2Delete = "";
		String strRegExpr4LogFiles2Compress = "";

		logger.info(String.format(new JSMsg("JSJ-I-110").get(), conMethodName));
		logger.info(conSVNVersion);

		try {
			Options().CheckMandatory();
			logger.debug(Options().dirtyString());
			strSchedulerID = objOptions.JobSchedulerID.Value(); // "KB-XPS-Laptop_4445";   //spooler.id();

			try {
				JSFolder objLogDirectory = objOptions.file_path.getFolder();
				JSFile fleSchedulerLog = objLogDirectory.newFile(JobSchedulerLogFileName);
				String strNewLogFileName = JobSchedulerLogFileName + "-" + fleSchedulerLog.getTimeStamp() + "-" + strSchedulerID
						+ JobSchedulerLogFileNameExtension;
				JSFile objNewLogFileName = objLogDirectory.newFile(strNewLogFileName);
				fleSchedulerLog.copy(objNewLogFileName);
				objNewLogFileName.createZipFile(objLogDirectory.getFolderName());
				objNewLogFileName.delete();

				deleteSchedulerLogFileSpec = strCaseInsensitive + "^(" + JobSchedulerLogFileName + "\\.)([0-9\\-]+)" + getRegExp4SchedulerID() + "(\\"
						+ JobSchedulerLogFileNameExtension + ")(\\.gz)?$";

				if (objOptions.delete_file_age.Value().equals("0") == false) {
					objLogDirectory.IncludeOlderThan = objOptions.delete_file_age.getTimeAsMilliSeconds();
					strRegExpr4CompressedFiles2Delete = strCaseInsensitive + objOptions.delete_file_specification.Value();
					intNoOfCompressedLogFilesDeleted = objLogDirectory.deleteFiles(strRegExpr4CompressedFiles2Delete);
					logger.info(intNoOfCompressedLogFilesDeleted + " compressed log files deleted for regexp: " + strRegExpr4CompressedFiles2Delete);

					intNoOfCompressedLogFilesDeleted = objLogDirectory.deleteFiles(deleteSchedulerLogFileSpec);
					logger.info(intNoOfCompressedLogFilesDeleted + " compressed log files deleted for regexp: " + deleteSchedulerLogFileSpec);
				}
				if (objOptions.compress_file_age.Value().equals("0") == false) {
					objLogDirectory.IncludeOlderThan = objOptions.compress_file_age.getTimeAsMilliSeconds();
					strRegExpr4LogFiles2Compress = strCaseInsensitive + objOptions.compress_file_spec.Value();
					intNoOfLogFilesCompressed = objLogDirectory.compressFiles(strRegExpr4LogFiles2Compress);
					logger.info(intNoOfLogFilesCompressed + " log files compressed for regexp: " + strRegExpr4LogFiles2Compress);
				}
			}
			catch (Exception e) {
				String strT = "an error occurred cleaning up log files: " + e.getMessage();
				throw new JobSchedulerException(strT, e);
			}
			finally {
				logger.info(intNoOfCompressedLogFilesDeleted + " compressed log files deleted for regexp: " + deleteSchedulerLogFileSpec);
			}
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			logger.error(String.format(new JSMsg("JSJ-I-107").get(), conMethodName), e);
			throw e;
		}
		finally {
		}
		
		logger.debug(String.format(new JSMsg("JSJ-I-111").get(), conMethodName));
		return true;
	}

	private String getRegExp4SchedulerID() {
		String strR = "";
		if (strSchedulerID != null) {
			strR += "(\\." + strSchedulerID + ")";
		}
		return strR;
	}

	public void init() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::init";
		doInitialize();
	}

	private void doInitialize() {
	} // doInitialize

} // class JobSchedulerRotateLog