package com.sos.jitl.housekeeping.rotatelog;

import static com.sos.JSHelper.Options.SOSOptionRegExp.strCaseInsensitive;
import static sos.scheduler.job.JobSchedulerConstants.JOB_SCHEDULER_LOG_FILE_NAME;
import static sos.scheduler.job.JobSchedulerConstants.JOB_SCHEDULER_LOG_FILE_NAME_EXTENSION;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.io.Files.JSFile;
import com.sos.JSHelper.io.Files.JSFolder;
import com.sos.scheduler.messages.JSMsg;

public class JobSchedulerRotateLog extends JSJobUtilitiesClass<JobSchedulerRotateLogOptions> {

    private static final String JOB_SCHEDULER_OLD_LOG_FILENAME = "scheduler-old";
    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerRotateLog.class);
    private String schedulerID = "";

    public JobSchedulerRotateLog() {
        super(new JobSchedulerRotateLogOptions());
    }

    @Override
    public JobSchedulerRotateLogOptions getOptions() {
        if (objOptions == null) {
            objOptions = new JobSchedulerRotateLogOptions();
        }
        return objOptions;
    }

    public boolean executeDebugLog() {
        final String methodName = "JobSchedulerRotateLog::ExecuteDebugLog";
        LOGGER.info(String.format(new JSMsg("JSJ-I-110").get(), methodName));
        try {
            schedulerID = objOptions.jobSchedulerID.getValue();
            getOptions().setJobSchedulerID(new SOSOptionString(schedulerID));
            getOptions().checkMandatory();
            try {
                JSFolder objLogDirectory = objOptions.file_path.getFolder();
                JSFile fleSchedulerLog = objLogDirectory.getNewFile(JOB_SCHEDULER_LOG_FILE_NAME);
                String strNewLogFileName = JOB_SCHEDULER_OLD_LOG_FILENAME + JOB_SCHEDULER_LOG_FILE_NAME_EXTENSION;
                JSFile objNewLogFileName = objLogDirectory.getNewFile(strNewLogFileName);
                fleSchedulerLog.copy(objNewLogFileName);
            } catch (Exception e) {
                String strT = "an error occurred copying log file to scheduler-old.log: " + e.getMessage();
                throw new JobSchedulerException(strT, e);
            }
        } catch (Exception e) {
            LOGGER.error(String.format(new JSMsg("JSJ-I-107").get(), methodName) + " " + e.getMessage(), e);
            throw e;
        }
        LOGGER.debug(String.format(new JSMsg("JSJ-I-111").get(), methodName));
        return true;
    }

    public boolean executeMainLog() {
        final String methodName = "JobSchedulerRotateLog::ExecuteMainLog";
        int intNoOfCompressedLogFilesDeleted = 0;
        int intNoOfLogFilesCompressed = 0;
        String deleteSchedulerLogFileSpec = "";
        String strRegExpr4CompressedFiles2Delete = "";
        String strRegExpr4LogFiles2Compress = "";
        LOGGER.info(String.format(new JSMsg("JSJ-I-110").get(), methodName));
        try {
            schedulerID = objOptions.jobSchedulerID.getValue();
            getOptions().setJobSchedulerID(new SOSOptionString(schedulerID));
            getOptions().checkMandatory();
            LOGGER.debug(getOptions().dirtyString());
            try {
                JSFolder objLogDirectory = objOptions.file_path.getFolder();
                deleteSchedulerLogFileSpec =
                        strCaseInsensitive + "^(" + JOB_SCHEDULER_LOG_FILE_NAME + "\\.)([0-9\\-]+)" + getRegExp4SchedulerID() + "(\\"
                                + JOB_SCHEDULER_LOG_FILE_NAME_EXTENSION + ")(\\.gz)?$";
                if (!"0".equals(objOptions.delete_file_age.getValue())) {
                    objLogDirectory.IncludeOlderThan = objOptions.delete_file_age.getTimeAsMilliSeconds();
                    strRegExpr4CompressedFiles2Delete = strCaseInsensitive + objOptions.delete_file_specification.getValue();
                    intNoOfCompressedLogFilesDeleted = objLogDirectory.deleteFiles(strRegExpr4CompressedFiles2Delete);
                    LOGGER.info(intNoOfCompressedLogFilesDeleted + " compressed log files deleted for regexp: " + strRegExpr4CompressedFiles2Delete);
                    intNoOfCompressedLogFilesDeleted = objLogDirectory.deleteFiles(deleteSchedulerLogFileSpec);
                    LOGGER.info(intNoOfCompressedLogFilesDeleted + " compressed log files deleted for regexp: " + deleteSchedulerLogFileSpec);
                }
                if (!"0".equals(objOptions.compress_file_age.getValue())) {
                    LOGGER.debug(String.format("compress files older than %s mSecs", objOptions.compress_file_age.getTimeAsMilliSeconds()));
                    objLogDirectory.IncludeOlderThan = objOptions.compress_file_age.getTimeAsMilliSeconds();
                    strRegExpr4LogFiles2Compress = strCaseInsensitive + objOptions.compress_file_spec.getValue();
                    intNoOfLogFilesCompressed = objLogDirectory.compressFiles(strRegExpr4LogFiles2Compress);
                    LOGGER.info(intNoOfLogFilesCompressed + " log files compressed for regexp: " + strRegExpr4LogFiles2Compress);
                }
            } catch (Exception e) {
                String strT = "an error occurred cleaning up log files: " + e.getMessage();
                throw new JobSchedulerException(strT, e);
            } finally {
                LOGGER.info(intNoOfCompressedLogFilesDeleted + " compressed log files deleted for regexp: " + deleteSchedulerLogFileSpec);
            }
        } catch (Exception e) {
            LOGGER.error(String.format(new JSMsg("JSJ-I-107").get(), methodName) + " " + e.getMessage(), e);
            throw e;
        }
        LOGGER.debug(String.format(new JSMsg("JSJ-I-111").get(), methodName));
        return true;
    }

    private String getRegExp4SchedulerID() {
        String strR = "";
        if (schedulerID != null) {
            strR += "(\\." + schedulerID + ")";
        }
        return strR;
    }

    public void init() {
        doInitialize();
    }

    private void doInitialize() {
        // doInitialize
    }

}