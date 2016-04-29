package sos.scheduler.reports;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Basics.VersionInfo;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.i18n.annotation.I18NResourceBundle;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

@I18NResourceBundle(baseName = "com.sos.scheduler.messages", defaultLocale = "en")
public class JSReportAllParameters extends JSToolBox implements JSJobUtilities {

    protected JSReportAllParametersOptions objOptions = null;
    private static final Logger LOGGER = Logger.getLogger(JSReportAllParameters.class);
    private final String conSVNVersion = "$Id$";
    private JSJobUtilities objJSJobUtilities = this;

    public JSReportAllParameters() {
        super("com_sos_scheduler_messages");
    }

    public JSReportAllParametersOptions Options() {
        if (objOptions == null) {
            objOptions = new JSReportAllParametersOptions();
        }
        return objOptions;
    }

    public JSReportAllParametersOptions Options(final JSReportAllParametersOptions pobjOptions) {
        objOptions = pobjOptions;
        return objOptions;
    }

    public JSReportAllParameters Execute() throws Exception {
        final String conMethodName = "JSReportAllParameters::Execute";
        try {
            LOGGER.debug(String.format(Messages.getMsg("JSJ-I-110"), conMethodName));
            LOGGER.info(conSVNVersion);
            LOGGER.info(VersionInfo.VERSION_STRING);
            LOGGER.debug(Options().toString());
            HashMap<String, String> objSettings = Options().Settings();
            for (final Object element : objSettings.entrySet()) {
                final Map.Entry mapItem = (Map.Entry) element;
                final String strMapKey = mapItem.getKey().toString();
                String strTemp = "";
                if (mapItem.getValue() != null) {
                    strTemp = mapItem.getValue().toString();
                    if (strMapKey.contains("password")) {
                        strTemp = "***";
                    }
                }
                LOGGER.info("Key = " + strMapKey + " --> " + strTemp);
            }
        } catch (Exception e) {
            throw new JobSchedulerException(Messages.getMsg("JSJ-I-107", conMethodName), e);
        } finally {
            LOGGER.debug(Messages.getMsg("JSJ-I-111", conMethodName));
        }
        return this;
    }

    @Override
<<<<<<< HEAD
    public String replaceSchedulerVars(final String pstrString2Modify) {
        logger.debug("replaceSchedulerVars as Dummy-call executed. No Instance of JobUtilites specified.");
=======
    public String replaceSchedulerVars(final boolean isWindows, final String pstrString2Modify) {
        LOGGER.debug("replaceSchedulerVars as Dummy-call executed. No Instance of JobUtilites specified.");
>>>>>>> 64d9775fa1b10da33c929ee153b62bb86923b408
        return pstrString2Modify;
    }

    @Override
    public void setJSParam(final String pstrKey, final String pstrValue) {

    }

    @Override
    public void setJSParam(final String pstrKey, final StringBuffer pstrValue) {

    }

    @Override
    public void setJSJobUtilites(final JSJobUtilities pobjJSJobUtilities) {
        if (pobjJSJobUtilities == null) {
            objJSJobUtilities = this;
        } else {
            objJSJobUtilities = pobjJSJobUtilities;
        }
        LOGGER.debug("objJSJobUtilities = " + objJSJobUtilities.getClass().getName());
    }

    @Override
    public String getCurrentNodeName() {
        // TO DO Auto-generated method stub
        return null;
    }

    @Override
    public void setStateText(final String pstrStateText) {
        // TO DO Auto-generated method stub
    }

    @Override
    public void setCC(final int pintCC) {
        // TO DO Auto-generated method stub
    }

    @Override
    public void setNextNodeState(final String pstrNodeName) {
        // TO DO Auto-generated method stub
    }

}
