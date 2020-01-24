package sos.scheduler.file;

import static com.sos.scheduler.messages.JSMessages.JSJ_F_0010;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.SOSFileSystemOperationsRename;
import com.sos.i18n.annotation.I18NResourceBundle;

/** @author Florian Schreiber */
@I18NResourceBundle(baseName = "com.sos.scheduler.messages", defaultLocale = "en")
public class JobSchedulerRenameFile extends JobSchedulerFileOperationBase {

	private static final String CLASSNAME = "JobSchedulerRenameFile";
	private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerRenameFile.class);

	@Override
	public boolean spooler_process() {
		try {
			initialize();
			sosFileOperations = new SOSFileSystemOperationsRename();
			if (file == null) {
				file = source;
			}
			checkMandatoryFile();
			noOfHitsInResultSet = sosFileOperations.renameFileCnt(file, target, fileSpec, flags, isCaseInsensitive,
					replacing, replacement, minFileAge, maxFileAge, minFileSize, maxFileSize, skipFirstFiles,
					skipLastFiles, sortCriteria, sortOrder);
			flgOperationWasSuccessful = noOfHitsInResultSet > 0;
			processResult(flgOperationWasSuccessful, source);
			return setReturnResult(flgOperationWasSuccessful);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			processResult(flgOperationWasSuccessful, source);
			String strM = JSJ_F_0010.params(CLASSNAME, e.getMessage());
			logger.error(strM);
			throw new JobSchedulerException(strM, e);
		}
	}

	protected void processResult(final boolean rc1, final String message) {
		// do nothing, entry point for subclasses
	}

}
