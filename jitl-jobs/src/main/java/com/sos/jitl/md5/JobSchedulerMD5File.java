package com.sos.jitl.md5;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.util.SOSCrypt;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.scheduler.messages.JSMessages;

public class JobSchedulerMD5File extends JSJobUtilitiesClass<JobSchedulerMD5FileOptions> {

    protected JobSchedulerMD5FileOptions jobSchedulerMD5FileOptions = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerMD5File.class);
    private static final String JOB_PARAMETER_FILE = "file";
    private static final String JOB_PARAM_MD5_SUFFIX = "md5_suffix";
    private static final String MODE_CREATE = "create";
    private static final String JOB_PARAM_MODE = "mode";
    private JSJobUtilities objJSJobUtilities = this;

    public JobSchedulerMD5File() {
        super(new JobSchedulerMD5FileOptions());
    }

    public JobSchedulerMD5FileOptions getOptions() {
        if (jobSchedulerMD5FileOptions == null) {
            jobSchedulerMD5FileOptions = new JobSchedulerMD5FileOptions();
        }
        return jobSchedulerMD5FileOptions;
    }

    public JobSchedulerMD5FileOptions getOptions(final JobSchedulerMD5FileOptions pobjOptions) {
        jobSchedulerMD5FileOptions = pobjOptions;
        return jobSchedulerMD5FileOptions;
    }

    private void handleMD5File() {
        String mode = MODE_CREATE;
        try {
            LOGGER.info(".. job parameter [" + JOB_PARAMETER_FILE + "]: " + jobSchedulerMD5FileOptions.file.getValue());
            LOGGER.info(".. job parameter [" + JOB_PARAM_MD5_SUFFIX + "]: " + jobSchedulerMD5FileOptions.md5_suffix.getValue());
            LOGGER.info(".. job parameter [" + JOB_PARAM_MODE + "]: " + jobSchedulerMD5FileOptions.mode.getValue());
            File file = new File(jobSchedulerMD5FileOptions.file.getValue());
            if (!file.canRead()) {
                LOGGER.warn(String.format("Failed to read file: '%1$s'", file.getAbsolutePath()));
                jobSchedulerMD5FileOptions.result.value(false);
            }
            File md5File = new File(file.getAbsolutePath() + jobSchedulerMD5FileOptions.md5_suffix.getValue());
            String strFileMD5 = SOSCrypt.md5encrypt(file);
            LOGGER.info("md5 of " + file.getAbsolutePath() + ": " + strFileMD5);
            if (MODE_CREATE.equalsIgnoreCase(mode)) {
                LOGGER.debug("creating md5 file: " + md5File.getAbsolutePath());
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(md5File)));
                out.write(strFileMD5);
                out.close();
            } else {
                LOGGER.debug("checking md5 file: " + md5File.getAbsolutePath());
                BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(md5File)));
                String strMD5fromFile = in.readLine();
                in.close();
                if (strMD5fromFile != null) {
                    strMD5fromFile = strMD5fromFile.split("\\s+")[0];
                } else {
                    strMD5fromFile = "";
                }
                LOGGER.debug("md5 from " + md5File.getAbsolutePath() + ": " + strMD5fromFile);
                if (strMD5fromFile.equalsIgnoreCase(strFileMD5)) {
                    LOGGER.info("md5 checksums are equal.");
                    jobSchedulerMD5FileOptions.result.value(true);
                } else {
                    LOGGER.warn("md5 checksums are different.");
                    jobSchedulerMD5FileOptions.result.value(false);
                }
            }
        } catch (Exception e) {
            LOGGER.error("error occurred in JobSchedulerCreateMD5File: " + e.getMessage(), e);
        }
    }

    public JobSchedulerMD5File Execute() throws Exception {
        final String methodName = "JobSchedulerMD5File::Execute";
        LOGGER.debug(String.format(JSMessages.JSJ_I_110.get(), methodName));
        try {
            getOptions().checkMandatory();
            LOGGER.debug(getOptions().toString());
            handleMD5File();
        } catch (Exception e) {
            LOGGER.error(String.format(JSMessages.JSJ_F_107.get(), methodName) + " " + e.getMessage(), e);
            throw e;
        } finally {
            LOGGER.debug(String.format(JSMessages.JSJ_I_111.get(), methodName));
        }
        return this;
    }

    public void init() {
        doInitialize();
    }

    private void doInitialize() {
        // doInitialize
    }

    public String myReplaceAll(String pstrSourceString, String pstrReplaceWhat, String pstrReplaceWith) {
        String newReplacement = pstrReplaceWith.replaceAll("\\$", "\\\\\\$");
        return pstrSourceString.replaceAll("(?m)" + pstrReplaceWhat, newReplacement);
    }

    @Override
    public String replaceSchedulerVars(String pstrString2Modify) {
        LOGGER.debug("replaceSchedulerVars as Dummy-call executed. No Instance of JobUtilites specified.");
        return pstrString2Modify;
    }

    @Override
    public void setJSParam(String pstrKey, String pstrValue) {

    }

    @Override
    public void setJSParam(String pstrKey, StringBuffer pstrValue) {

    }

    public void setJSJobUtilites(JSJobUtilities pobjJSJobUtilities) {
        if (pobjJSJobUtilities == null) {
            objJSJobUtilities = this;
        } else {
            objJSJobUtilities = pobjJSJobUtilities;
        }
        LOGGER.debug("objJSJobUtilities = " + objJSJobUtilities.getClass().getName());
    }

}