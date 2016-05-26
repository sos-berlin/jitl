package sos.scheduler.file;

import static com.sos.scheduler.messages.JSMessages.JSJ_F_0010;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.i18n.annotation.I18NResourceBundle;

/** @author Florian Schreiber */
@I18NResourceBundle(baseName = "com.sos.scheduler.messages", defaultLocale = "en")
public class JobSchedulerRenameFile extends JobSchedulerFileOperationBase {

    private static final String CLASSNAME = "JobSchedulerRenameFile";
    private static final Logger LOGGER = Logger.getLogger(JobSchedulerRenameFile.class);

    @Override
    public boolean spooler_process() {
        try {
            initialize();
            if (file == null) {
                file = source;
            }
            checkMandatoryFile();
            intNoOfHitsInResultSet =
                    SOSFileOperations.renameFileCnt(file, target, fileSpec, flags, isCaseInsensitive, replacing, replacement, minFileAge, maxFileAge,
                            minFileSize, maxFileSize, skipFirstFiles, skipLastFiles, objSOSLogger);
            flgOperationWasSuccessful = intNoOfHitsInResultSet > 0;
            processResult(flgOperationWasSuccessful, source);
            return setReturnResult(flgOperationWasSuccessful);
        } catch (Exception e) {
            try {
                LOGGER.error(e.getMessage(), e);
                processResult(flgOperationWasSuccessful, source);
                String strM = JSJ_F_0010.params(CLASSNAME, e.getMessage());
                logger.fatal(strM);
                throw new JobSchedulerException(strM, e);
            } catch (Exception x) {
            }
            return false;
        }
    }

    protected void processResult(final boolean rc1, final String message) {
        // do nothing, entry point for subclasses
    }

}
