package sos.scheduler.file;

import static com.sos.scheduler.messages.JSMessages.JSJ_F_0010;

import com.sos.i18n.annotation.I18NResourceBundle;

/** @author Florian Schreiber */
@I18NResourceBundle(baseName = "com.sos.scheduler.messages", defaultLocale = "en")
public class JobSchedulerRemoveFile extends JobSchedulerFileOperationBase {

    private static final String CLASSNAME = "JobSchedulerRemoveFile";

    @Override
    public boolean spooler_process() {
        try {
            initialize();
            if (file == null) {
                file = source;
            }
            checkMandatoryFile();
            intNoOfHitsInResultSet =
                    SOSFileOperations.removeFileCnt(file, fileSpec, flags, isCaseInsensitive, minFileAge, maxFileAge, minFileSize, maxFileSize,
                            skipFirstFiles, skipLastFiles, objSOSLogger);
            flgOperationWasSuccessful = intNoOfHitsInResultSet > 0;
            return setReturnResult(flgOperationWasSuccessful);
        } catch (Exception e) {
            String strM = JSJ_F_0010.params(CLASSNAME, e.getMessage());
            logger.error(strM);
            logger.trace("", e);
            return signalFailure();
        }
    }

}