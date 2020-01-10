package sos.scheduler.file;

import static com.sos.scheduler.messages.JSMessages.JSJ_F_0010;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.SOSFileSystemOperationsImpl;
import com.sos.i18n.annotation.I18NResourceBundle;

/** @author Uwe Risse */
@I18NResourceBundle(baseName = "com.sos.scheduler.messages", defaultLocale = "en")
public class JobSchedulerCanWrite extends JobSchedulerFileOperationBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerCanWrite.class);
	private static final String CLASSNAME = "JobSchedulerCanWrite";

    @Override
    public boolean spooler_process() {
        try {
            initialize();
            sosFileOperations = new SOSFileSystemOperationsImpl();
            checkMandatoryFile();
            flgOperationWasSuccessful = sosFileOperations.canWrite(file, fileSpec, isCaseInsensitive);
            return setReturnResult(flgOperationWasSuccessful);
        } catch (Exception e) {
            try {
                LOGGER.error(e.getMessage(), e);
                String strM = JSJ_F_0010.params(CLASSNAME, e.getMessage());
                logger.error(strM);
                throw new JobSchedulerException(strM, e);
            } catch (Exception x) {
            }
            return signalFailure();
        }
    }

}
