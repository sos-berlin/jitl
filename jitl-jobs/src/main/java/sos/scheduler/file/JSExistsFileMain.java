package sos.scheduler.file;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.i18n.annotation.I18NResourceBundle;
import org.apache.log4j.Logger;

/** @author KB */
@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
public class JSExistsFileMain extends JSToolBox {

    protected JSExistsFileOptions objOptions = null;
    private static final Logger LOGGER = Logger.getLogger(JSExistsFileMain.class);

    public final static void main(String[] pstrArgs) {
        final String methodName = "JSExistsFileMain::Main";
        LOGGER.info("JSExistFile - Main");
        try {
            JSExistsFile objM = new JSExistsFile();
            JSExistsFileOptions objO = objM.getOptions();
            objO.commandLineArgs(pstrArgs);
            boolean flgResult = objM.Execute();
            if (flgResult) {
                System.exit(0);
            } else {
                System.exit(99);
            }
        } catch (Exception e) {
            LOGGER.error(methodName + ": " + "Error occured ..." + e.getMessage());
            int intExitCode = 99;
            LOGGER.error(String.format("JSJ-E-105: %1$s - terminated with exit-code %2$d", methodName, intExitCode), e);
            System.exit(intExitCode);
        }
        LOGGER.info(String.format("JSJ-I-106: %1$s - ended without errors", methodName));
    }

}