package sos.scheduler.job;

import static com.sos.scheduler.messages.JSMessages.JFO_E_0016;
import static com.sos.scheduler.messages.JSMessages.JFO_I_0014;
import static com.sos.scheduler.messages.JSMessages.JFO_I_0015;
import static com.sos.scheduler.messages.JSMessages.JFO_I_0019;
import static com.sos.scheduler.messages.JSMessages.JFO_I_0020;
import static com.sos.scheduler.messages.JSMessages.JSJ_F_0010;

import java.io.File;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.SOSFileSystemOperationsImpl;
import com.sos.i18n.annotation.I18NResourceBundle;

import sos.scheduler.file.JobSchedulerFileOperationBase;
import sos.util.SOSFile;

/** @author andreas pueschel */
@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
public class JobSchedulerCleanupFiles extends JobSchedulerFileOperationBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerCleanupFiles.class);
    private final static String conClassName = "JobSchedulerCleanupFiles";

    @Override
    public boolean spooler_process() {
        initialize();
        sosFileOperations = new SOSFileSystemOperationsImpl();
        try {
            if (isEmpty(filePath)) {
                filePath = PROPERTY_JAVA_IO_TMPDIR;
            }
            String[] filePaths = filePath.split(";");
            String tmpFileSpec = getParamValue(new String[] { PARAMETER_FILE_SPEC, PARAMETER_FILE_SPECIFICATION }, EMPTY_STRING);
            if (isEmpty(tmpFileSpec)) {
                fileSpec = "^(sos.*)";
            }
            if (lngFileAge <= 0) {
                lngFileAge = calculateFileAge(getParamValue(PARAMETER_FILE_AGE, "24:00"));
            }
            String[] fileSpecs = fileSpec.split(";");
            boolean flgPathAndSpecHasSameNumberOfItems = filePaths.length == fileSpecs.length;
            fileSpec = fileSpecs[0];
            for (int i = 0; i < filePaths.length; i++) {
                int counter = 0;
                filePath = filePaths[i];
                if (filePath.trim().equalsIgnoreCase(PROPERTY_JAVA_IO_TMPDIR)) {
                    filePath = System.getProperty(PROPERTY_JAVA_IO_TMPDIR);
                }
                if (flgPathAndSpecHasSameNumberOfItems) {
                    fileSpec = fileSpecs[i];
                }
                LOGGER.debug(JFO_I_0019.params(filePath));
                Vector<File> filelist = SOSFile.getFolderlist(filePath, fileSpec, 0);
                if (filelist.isEmpty()) {
                    LOGGER.info(JFO_I_0020.params(fileSpec));
                }
                if (warningFileLimit > 0 && filelist.size() >= warningFileLimit) {
                    LOGGER.error(JFO_E_0016.params(filelist.size(), filePath, warningFileLimit, PARAMETER_WARNING_FILE_LIMIT));
                }
                for (File tempFile : filelist) {
                    long interval = System.currentTimeMillis() - tempFile.lastModified();
                    if (interval > lngFileAge) {
                        counter += SOSFile.deleteFile(tempFile);
                        LOGGER.info(JFO_I_0014.params(tempFile.getAbsolutePath()));
                    }
                }
                if (counter > 0) {
                    String strT = filePath;
                    LOGGER.info(JFO_I_0015.params(counter, strT));
                }
            }
            return signalSuccess(spooler_task.order());
        } catch (Exception e) {
            throw new JobSchedulerException(JSJ_F_0010.params(conClassName, e.getMessage()), e);
        }
    }

}