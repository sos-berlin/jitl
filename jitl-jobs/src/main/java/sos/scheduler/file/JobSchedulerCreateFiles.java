package sos.scheduler.file;

import static com.sos.scheduler.messages.JSMessages.JSJ_F_0010;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.SOSFileSystemOperationsImpl;
import com.sos.JSHelper.io.Files.JSFile;
import com.sos.i18n.annotation.I18NResourceBundle;

/** @author KB */
@I18NResourceBundle(baseName = "com.sos.scheduler.messages", defaultLocale = "en")
public class JobSchedulerCreateFiles extends JobSchedulerFileOperationBase {

    private static final String PARAMETER_FILE_AGE = "file_age";
    private static final String PARAMETER_FILE_SIZE = "file_size";
    private static final String PARAMETER_CREATE_FILE = "create_file";
    private static final String CLASSNAME = "JobSchedulerCreateFiles";
    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerCreateFiles.class);

    @Override
    public boolean spooler_process() {
        int fileSize;
        long fileAge;
        try {
            initialize();
            sosFileOperations = new SOSFileSystemOperationsImpl();
            String strCreateFile = getParamValue(PARAMETER_CREATE_FILE);
            if (isNotEmpty(strCreateFile)) {
                fileSize = getParamInteger(PARAMETER_FILE_SIZE, 10);
                fileAge = getFileAge(getParamValue(PARAMETER_FILE_AGE));
                populateFile(new JSFile(strCreateFile), fileSize, fileAge);
            }
            for (int i = 0; i < 20; i++) {
                String strFileName2Create = getParamValue(PARAMETER_CREATE_FILE + "_" + i);
                if (isNotEmpty(strFileName2Create)) {
                    fileSize = getParamInteger(PARAMETER_FILE_SIZE + "_" + i, 10);
                    fileAge = getFileAge(getParamValue(PARAMETER_FILE_AGE + "_" + i));
                    populateFile(new JSFile(strFileName2Create), fileSize, fileAge);
                }
            }
            return signalSuccess(spooler_task.order());
        } catch (Exception e) {
            String strM = JSJ_F_0010.params(CLASSNAME, e.getMessage());
            LOGGER.error(strM);
            throw new JobSchedulerException(strM);
        }
    }

    public long getFileAge(final String pstrFileAge) {
        long fileAge = System.currentTimeMillis();
        if (isNotEmpty(pstrFileAge)) {
            fileAge = System.currentTimeMillis() - 1000 * Long.parseLong(spooler_task.order().params().var(PARAMETER_FILE_AGE));
        }
        return fileAge;
    }

    public void populateFile(final JSFile file1, final int fileSize, final long lastModified) throws Exception {
        try {
            LOGGER.info("populating file: " + file1.getName() + "   " + file1.getAbsolutePath());
            file1.write(new StringBuffer(fileSize));
        } catch (Exception e) {
            throw new Exception("could not populate file [" + file1.getAbsolutePath() + "]: " + e.getMessage());
        } finally {
            try {
                file1.close();
                file1.setLastModified(lastModified);
            } catch (Exception x) {
            }
        }
    }

}