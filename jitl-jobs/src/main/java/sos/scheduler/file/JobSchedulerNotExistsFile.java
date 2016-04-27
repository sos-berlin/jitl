package sos.scheduler.file;

import static com.sos.scheduler.messages.JSMessages.JSJ_F_0010;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.i18n.annotation.I18NResourceBundle;

/** @author Florian Schreiber */
@I18NResourceBundle(baseName = "com.sos.scheduler.messages", defaultLocale = "en")
public class JobSchedulerNotExistsFile extends JobSchedulerFileOperationBase {

    private final String conClassName = this.getClass().getName();

    @Override
    public boolean spooler_process() {
        try {
            initialize();
            CheckMandatoryFile();
            flgOperationWasSuccessful =
                    !SOSFileOperations.existsFile(file, fileSpec, isCaseInsensitive, minFileAge, maxFileAge, minFileSize, maxFileSize,
                            skipFirstFiles, skipLastFiles, objSOSLogger);
            return setReturnResult(flgOperationWasSuccessful);
        } catch (Exception e) {
            try {
                String strM = JSJ_F_0010.params(conClassName, e.getMessage());
                logger.fatal(strM + e);
                throw new JobSchedulerException(strM, e);
            } catch (Exception x) {
            }
            return false;
        }
    }

}