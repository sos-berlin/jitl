package sos.scheduler.file;

import static com.sos.scheduler.messages.JSMessages.JSJ_F_0010;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.SOSFileSystemOperationsImpl;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
public class JobSchedulerExistsFile extends JobSchedulerFileOperationBase {

	private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerExistsFile.class);
	private static final String CLASSNAME = "JobSchedulerExistsFile";

	@Override
	public boolean spooler_process() {
		try {
			initialize();
			checkMandatoryFile();
			sosFileOperations = new SOSFileSystemOperationsImpl();
			flgOperationWasSuccessful = sosFileOperations.existsFile(file, fileSpec, Pattern.CASE_INSENSITIVE,
					minFileAge, maxFileAge, minFileSize, maxFileSize, skipFirstFiles, skipLastFiles);
			if (flgOperationWasSuccessful) {
				flgOperationWasSuccessful = checkSteadyStateOfFiles();
			}
			return setReturnResult(flgOperationWasSuccessful);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			String strM = JSJ_F_0010.params(CLASSNAME, e.getMessage());
			logger.error(strM);
			throw new JobSchedulerException(strM, e);
		}
	}

}