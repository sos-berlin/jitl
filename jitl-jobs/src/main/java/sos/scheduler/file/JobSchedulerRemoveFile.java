package sos.scheduler.file;

import static com.sos.scheduler.messages.JSMessages.JSJ_F_0010;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.SOSFileSystemOperationsImpl;
import com.sos.i18n.annotation.I18NResourceBundle;

/** @author Florian Schreiber */
@I18NResourceBundle(baseName = "com.sos.scheduler.messages", defaultLocale = "en")
public class JobSchedulerRemoveFile extends JobSchedulerFileOperationBase {

    private static final String CLASSNAME = "JobSchedulerRemoveFile";

    @Override
    public boolean spooler_process() {
        try {
            initialize();
            sosFileOperations = new SOSFileSystemOperationsImpl();
            if (file == null) {
                file = source;
            }
            checkMandatoryFile();
            noOfHitsInResultSet =
                    sosFileOperations.removeFileCnt(file, fileSpec, flags, isCaseInsensitive, minFileAge, maxFileAge, minFileSize, maxFileSize,
                            skipFirstFiles, skipLastFiles,sortCriteria, sortOrder);
            flgOperationWasSuccessful = noOfHitsInResultSet > 0;
            return setReturnResult(flgOperationWasSuccessful);
        } catch (Exception e) {
			String strM = JSJ_F_0010.params(CLASSNAME, e.getMessage());
			logger.error(strM + e);
			throw new JobSchedulerException(strM, e);
        }
    }

}