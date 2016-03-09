package com.sos.jitl.jasperreports;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import sos.connection.SOSConnection;
import sos.spooler.Variable_set;
import sos.spooler.Web_service_request;
import sos.spooler.Xslt_stylesheet;
import sos.util.SOSClassUtil;
import sos.util.SOSString;
import sos.xml.SOSXMLXPath;

/** Es werden n-Mal Reports rausgeschrieben. n -> Anzahl der Statement Der
 * Parameter der JobSchedulerJasperReportJob wird mit der Resultat der Selekt
 * Statement rausgeschrieben.
 * 
 * @author mo */
public class JobSchedulerJasperReportJobRepeat extends JobSchedulerJasperReportJob {

    /** sos.util.SOSString Objekt */
    private SOSString sosString = new SOSString();

    private ArrayList result = null;

    /** Eine SQl Statement. Anzahl der Ergebnisse dieser SQL Stament ist der
     * Anzahl der Report, die erzeugt werden */
    private String repeatStatement = null;

    private String settingsFilename = null;

    private String outputFilename = null;

    public boolean spooler_process() {
        HashMap repeatParams = null;
        try {
            getJobParam();
            getOrderParam();
            spooler_log.debug9("repeateStatement " + repeatStatement);
            if (sosString.parseToString(repeatStatement).length() == 0) {
                spooler_log.debug9("keine repeateStatement");
                return super.spooler_process();
            }

            if (new File(repeatStatement).isFile() && new File(repeatStatement).getName().endsWith(".sql")) {
                repeatStatement = sos.util.SOSFile.readFile(new File(repeatStatement));
            }

            SOSConnection conn = getCurrConnection();
            if (conn != null) {
                result = conn.getArray(repeatStatement);

                for (int i = 0; result != null && i < result.size(); i++) {
                    String originOutputFilename = outputFilename;
                    repeatParams = (HashMap) result.get(i);
                    java.util.Iterator it = repeatParams.keySet().iterator();
                    String vals = "";
                    while (it.hasNext()) {
                        String key = sosString.parseToString(it.next());
                        String value = sosString.parseToString(repeatParams.get(key));
                        spooler_log.debug9("repeat Parameter " + key + "=" + value);
                        if (key.length() > 0 && value.length() > 0) {
                            spooler_log.debug9("params: " + spooler_task.params().names());
                            spooler_task.params().set_var(key, value);
                            vals = vals + value + "_";
                        }
                    }

                    File newOF = new File(originOutputFilename);
                    originOutputFilename = (newOF != null && newOF.getParent() != null ? newOF.getParent() : "") + "/" + vals + newOF.getName();
                    spooler_task.params().set_var("output_filename", originOutputFilename);
                    super.spooler_process();

                }
            }
            return false;
        } catch (Exception e) {
            spooler_job.set_state_text(e.getMessage());
            spooler_log.warn(e.getMessage());
            return false;
        }
    }

    public void getJobParam() throws Exception {
        try {
            if (sosString.parseToString(spooler_task.params().var("repeat_sql_statement")).length() > 0) {
                repeatStatement = sosString.parseToString(spooler_task.params().var("repeat_sql_statement"));
                spooler_log.debug1(".. job parameter [repeat_sql_statement]: " + repeatStatement);
            }
            if (sosString.parseToString(spooler_task.params().var("settings_filename")).length() > 0) {
                settingsFilename = spooler_task.params().var("settings_filename");
                spooler_log.debug1(".. job parameter [settings_filename]: " + settingsFilename);
            }

            if (sosString.parseToString(spooler_task.params().var("output_filename")).length() > 0) {
                outputFilename = spooler_task.params().var("output_filename");
                spooler_log.debug1(".. job parameter [output_filename]: " + outputFilename);
            }
        } catch (Exception e) {
            throw new Exception("..error occurred processing job parameters: " + e.getMessage());
        }
    }

    private void getOrderParam() throws Exception {
        try {
            if (spooler_task.job().order_queue() != null) {

                order = spooler_task.order();
                // order = createOrderPayload(order);

                // create order payload and xml payload from web service request
                if (order.web_service_operation_or_null() != null) {
                    SOSXMLXPath xpath = null;
                    Web_service_request request = order.web_service_operation().request();

                    // should the request be previously transformed ...
                    if (order.web_service().params().var("request_stylesheet") != null
                            && order.web_service().params().var("request_stylesheet").length() > 0) {
                        Xslt_stylesheet stylesheet = spooler.create_xslt_stylesheet();
                        stylesheet.load_file(order.web_service().params().var("request_stylesheet"));
                        String xml_document = stylesheet.apply_xml(request.string_content());
                        spooler_log.debug3("content of request:\n" + request.string_content());
                        spooler_log.debug3("content of request transformation:\n" + xml_document);

                        xpath = new sos.xml.SOSXMLXPath(new java.lang.StringBuffer(xml_document));
                        // add order parameters from transformed request
                        Variable_set params = spooler.create_variable_set();
                        if (xpath.selectSingleNodeValue("//param[@name[.='repeat_sql_statement']]/@value") != null) {
                            params.set_var("repeat_sql_statement", xpath.selectSingleNodeValue("//param[@name[.='repeat_sql_statement']]/@value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[@name[.='settings_filename']]/@value") != null) {
                            params.set_var("settings_filename", xpath.selectSingleNodeValue("//param[@name[.='settings_filename']]/@value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[@name[.='output_filename']]/@value") != null) {
                            params.set_var("output_filename", xpath.selectSingleNodeValue("//param[@name[.='output_filename']]/@value"));
                        }
                        order.set_payload(params);
                    } else {
                        xpath = new sos.xml.SOSXMLXPath(new java.lang.StringBuffer(request.string_content()));
                        // add order parameters from request
                        Variable_set params = spooler.create_variable_set();
                        if (xpath.selectSingleNodeValue("//param[name[text()='repeat_sql_statement']]/value") != null) {
                            params.set_var("repeat_sql_statement", xpath.selectSingleNodeValue("//param[name[text()='repeat_sql_statement']]/value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[name[text()='settings_filename']]/value") != null) {
                            params.set_var("settings_filename", xpath.selectSingleNodeValue("//param[name[text()='settings_filename']]/value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[name[text()='output_filename']]/value") != null) {
                            params.set_var("output_filename", xpath.selectSingleNodeValue("//param[name[text()='output_filename']]/value"));
                        }
                        order.set_payload(params);
                    }

                }

                orderData = (Variable_set) order.payload();
                if (orderData != null && orderData.var("repeat_sql_statement") != null
                        && orderData.var("repeat_sql_statement").toString().length() > 0) {
                    repeatStatement = orderData.var("repeat_sql_statement").toString();
                    spooler_log.debug1(".. order parameter [repeat_sql_statement]: " + repeatStatement);
                }
                if (orderData != null && orderData.var("settings_filename") != null && orderData.var("settings_filename").toString().length() > 0) {
                    settingsFilename = (orderData.var("settings_filename").toString());
                    spooler_log.debug1(".. order parameter [settings_filename]: " + settingsFilename);
                }
                if (orderData != null && orderData.var("output_filename") != null && orderData.var("output_filename").toString().length() > 0) {
                    outputFilename = orderData.var("output_filename").toString();
                    spooler_log.debug1(".. order parameter [output_filename]: " + outputFilename);
                }

            }
        } catch (Exception e) {
            throw new Exception("error occurred processing parameters: " + e.toString());
        }
    }

    private SOSConnection getCurrConnection() throws Exception {

        SOSConnection conn = null;
        try {
            this.spooler_log.debug5("DB Connecting.. .");
            if (settingsFilename != null && settingsFilename.length() > 0) {
                conn = SOSConnection.createInstance(settingsFilename, new sos.util.SOSSchedulerLogger(spooler_log));
                conn.connect();
            } else {
                conn = getConnection();
            }
            spooler_log.debug5("DB Connected");
            return conn;
        } catch (Exception e) {
            throw new Exception("\n -> ..error occurred in " + SOSClassUtil.getMethodName() + ": " + e);
        }
    }

}
