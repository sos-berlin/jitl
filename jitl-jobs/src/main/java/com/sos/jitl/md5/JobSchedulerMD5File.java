package com.sos.jitl.md5;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import com.sos.jitl.md5.JobSchedulerMD5File;
import com.sos.jitl.md5.JobSchedulerMD5FileOptions;
import org.apache.log4j.Logger;
import sos.util.SOSCrypt;
import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.localization.*;
import com.sos.scheduler.messages.JSMessages;
import com.sos.JSHelper.Basics.JSJobUtilities;

public class JobSchedulerMD5File extends JSJobUtilitiesClass<JobSchedulerMD5FileOptions> {
    private final String conClassName = "JobSchedulerMD5File"; //$NON-NLS-1$
    private static Logger logger = Logger.getLogger(JobSchedulerMD5File.class);

    private static final String conJobParameterFILE = "file";
    private static final String conJobParamMD5_SUFFIX = "md5_suffix";
    private static final String conModeCREATE = "create";
    private static final String conJobParamMODE = "mode";

    protected JobSchedulerMD5FileOptions jobSchedulerMD5FileOptions = null;
    private JSJobUtilities objJSJobUtilities = this;

     
    public JobSchedulerMD5File() {
        super(new JobSchedulerMD5FileOptions());
    }

    
    public JobSchedulerMD5FileOptions getOptions() {

        @SuppressWarnings("unused")//$NON-NLS-1$
        final String conMethodName = conClassName + "::Options"; //$NON-NLS-1$

        if (jobSchedulerMD5FileOptions == null) {
            jobSchedulerMD5FileOptions = new JobSchedulerMD5FileOptions();
        }
        return jobSchedulerMD5FileOptions;
    }

    
    public JobSchedulerMD5FileOptions getOptions(final JobSchedulerMD5FileOptions pobjOptions) {

        @SuppressWarnings("unused")//$NON-NLS-1$
        final String conMethodName = conClassName + "::Options"; //$NON-NLS-1$

        jobSchedulerMD5FileOptions = pobjOptions;
        return jobSchedulerMD5FileOptions;
    }

    private void handleMD5File() {
      
        String mode = conModeCREATE;
        try {
            // Job oder Order

            logger.info(".. job parameter [" + conJobParameterFILE + "]: " + jobSchedulerMD5FileOptions.file.Value());
            logger.info(".. job parameter [" + conJobParamMD5_SUFFIX + "]: " + jobSchedulerMD5FileOptions.md5_suffix.Value());
            logger.info(".. job parameter [" + conJobParamMODE + "]: " + jobSchedulerMD5FileOptions.mode.Value());

            File file = new File(jobSchedulerMD5FileOptions.file.Value());
            if (!file.canRead()) {
                logger.warn(String.format("Failed to read file: '%1$s'", file.getAbsolutePath()));
                jobSchedulerMD5FileOptions.result.value(false);
            }
            File md5File = new File(file.getAbsolutePath() + jobSchedulerMD5FileOptions.md5_suffix.Value());
            String strFileMD5 = SOSCrypt.MD5encrypt(file);
            logger.info("md5 of " + file.getAbsolutePath() + ": " + strFileMD5);

            if (mode.equalsIgnoreCase(conModeCREATE)) {
                logger.debug("creating md5 file: " + md5File.getAbsolutePath());
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(md5File)));
                out.write(strFileMD5);
                out.close();
            } else {
                logger.debug("checking md5 file: " + md5File.getAbsolutePath());
                BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(md5File)));
                String strMD5fromFile = in.readLine();
                in.close();
                if (strMD5fromFile != null) {
                    // get only 1st part in case of md5sum format
                    strMD5fromFile = strMD5fromFile.split("\\s+")[0];
                } else
                    strMD5fromFile = "";
                logger.debug("md5 from " + md5File.getAbsolutePath() + ": " + strMD5fromFile);
                if (strMD5fromFile.equalsIgnoreCase(strFileMD5)) {
                    logger.info("md5 checksums are equal.");
                    jobSchedulerMD5FileOptions.result.value(true);
                } else {
                    logger.warn("md5 checksums are different.");
                    jobSchedulerMD5FileOptions.result.value(false);
                }
            }
        } catch (Exception e) {
            try {
                e.printStackTrace(System.err);
                logger.error("error occurred in JobSchedulerCreateMD5File: " + e.getMessage());
            } catch (Exception x) {
            }
        }

    }

    public JobSchedulerMD5File Execute() throws Exception {
        final String conMethodName = conClassName + "::Execute"; //$NON-NLS-1$

        logger.debug(String.format(JSMessages.JSJ_I_110.get(), conMethodName));

        try {
            getOptions().CheckMandatory();
            logger.debug(getOptions().toString());
            handleMD5File();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            logger.error(String.format(JSMessages.JSJ_F_107.get(), conMethodName), e);
            throw e;
        } finally {
            logger.debug(String.format(JSMessages.JSJ_I_111.get(), conMethodName));
        }

        return this;
    }

    public void init() {
        @SuppressWarnings("unused")//$NON-NLS-1$
        final String conMethodName = conClassName + "::init"; //$NON-NLS-1$
        doInitialize();
    }

    private void doInitialize() {
    } // doInitialize

    public String myReplaceAll(String pstrSourceString, String pstrReplaceWhat, String pstrReplaceWith) {

        String newReplacement = pstrReplaceWith.replaceAll("\\$", "\\\\\\$");
        return pstrSourceString.replaceAll("(?m)" + pstrReplaceWhat, newReplacement);
    }

    @Override
    public String replaceSchedulerVars(boolean isWindows, String pstrString2Modify) {
        logger.debug("replaceSchedulerVars as Dummy-call executed. No Instance of JobUtilites specified.");
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
        logger.debug("objJSJobUtilities = " + objJSJobUtilities.getClass().getName());
    }

} // class JobSchedulerMD5File