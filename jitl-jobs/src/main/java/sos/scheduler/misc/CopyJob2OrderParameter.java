package sos.scheduler.misc;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Basics.JSToolBox;

public class CopyJob2OrderParameter extends JSToolBox implements JSJobUtilities {

    protected CopyJob2OrderParameterOptions objOptions = null;
    private static final Logger LOGGER = Logger.getLogger(CopyJob2OrderParameter.class);
    private JSJobUtilities objJSJobUtilities = this;

    public CopyJob2OrderParameter() {
        super("com_sos_scheduler_messages");
    }

    public CopyJob2OrderParameterOptions Options() {
        if (objOptions == null) {
            objOptions = new CopyJob2OrderParameterOptions();
        }
        return objOptions;
    }

    public CopyJob2OrderParameterOptions Options(final CopyJob2OrderParameterOptions pobjOptions) {
        objOptions = pobjOptions;
        return objOptions;
    }

    public CopyJob2OrderParameter Execute() throws Exception {
        final String conMethodName = "CopyJob2OrderParameter::Execute";
        LOGGER.debug(String.format(Messages.getMsg("JSJ-I-110"), conMethodName));
        try {
            Options().checkMandatory();
            LOGGER.debug(Options().toString());
            HashMap<String, String> objSettings = Options().settings();
            for (final Object element : objSettings.entrySet()) {
                final Map.Entry mapItem = (Map.Entry) element;
                String strMapKey = mapItem.getKey().toString();
                String strTemp = mapItem.getValue().toString();
                objJSJobUtilities.setJSParam(strMapKey, strTemp);
            }
        } catch (Exception e) {
            LOGGER.error(String.format(Messages.getMsg("JSJ-I-107"), conMethodName), e);
        } finally {
            LOGGER.debug(String.format(Messages.getMsg("JSJ-I-111"), conMethodName));
        }
        return this;
    }

    @Override
    public String replaceSchedulerVars(final String pstrString2Modify) {
        LOGGER.debug("replaceSchedulerVars as Dummy-call executed. No Instance of JobUtilites specified.");
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
