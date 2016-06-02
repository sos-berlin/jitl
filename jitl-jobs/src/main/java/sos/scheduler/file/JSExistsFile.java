package sos.scheduler.file;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.sos.i18n.annotation.I18NResourceBundle;

/** @author KB */
@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
public class JSExistsFile extends JSFileOperationBase {

    private static final Logger LOGGER = Logger.getLogger(JSExistsFile.class);

    public JSExistsFile() {
        super();
    }

    public boolean Execute() throws Exception {
        final String methodName = "JSExistsFile::Execute";
        LOGGER.debug(String.format(Messages.getMsg("JSJ-I-110"), methodName));
        try {
            initialize();
            getOptions().file.checkMandatory();
            getOptions().file_spec.setRegExpFlags(Pattern.CASE_INSENSITIVE);
            flgOperationWasSuccessful =
                    existsFile(getOptions().file, getOptions().file_spec, getOptions().min_file_age, getOptions().max_file_age, getOptions().min_file_size,
                            getOptions().max_file_size, getOptions().skip_first_files, getOptions().skip_last_files, -1, -1);
            flgOperationWasSuccessful = createResultListParam(flgOperationWasSuccessful);
            return flgOperationWasSuccessful;
        } catch (Exception e) {
            LOGGER.error(Messages.getMsg("JSJ-I-107", methodName) + " " + e.getMessage(), e);
        } finally {
            if (flgOperationWasSuccessful) {
                LOGGER.debug(Messages.getMsg("JSJ-I-111", methodName));
            }
        }
        return flgOperationWasSuccessful;
    }

    public void init() {
        doInitialize();
    }

    private void doInitialize() {
        // doInitialize
    }

}