package com.sos.jitl.jasperreports;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaSizeName;

import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.util.JRLoader;
import sos.connection.SOSConnection;
import sos.connection.SOSConnectionFileProcessor;
import sos.net.SOSMailOrder;
import sos.scheduler.managed.JobSchedulerManagedJob;
import sos.settings.SOSConnectionSettings;
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.spooler.Web_service_request;
import sos.spooler.Web_service_response;
import sos.spooler.Xslt_stylesheet;
import sos.textprocessor.SOSPlainTextProcessor;
import sos.util.SOSClassUtil;
import sos.util.SOSDate;
import sos.util.SOSSchedulerLogger;
import sos.util.SOSString;
import sos.xml.SOSXMLXPath;

/** @author Andreas Pueschel */
public class JobSchedulerJasperReportJob extends JobSchedulerManagedJob {

    protected Order order = null;
    protected Variable_set orderData = null;
    private String settingsFilename = "";
    private String reportFilename = "";
    private String queryFilename = "";
    private String outputType = "pdf";
    private String outputFilename = "";
    private String printerName = "";
    private String factorySettingsFile = "";
    private SOSString sosString = new SOSString();
    private File filledReportFile = null;
    private ArrayList listOfOutputFilename = null;
    private String parameterQueryFilename = "";
    private int printerCopies = 1;
    private boolean mailIt = false;
    private String mailTo = "";
    private String mailCc = "";
    private String mailBcc = "";
    private String mailSubject = "";
    private boolean deleteOldFilename = false;
    private String mailBody = "";
    private String queryStatement = "";
    private boolean suspendAttachment = false;

    public boolean spooler_init() {
        try {
            if (!super.spooler_init()) {
                return false;
            }
            return true;
        } catch (Exception e) {
            spooler_log.error("error occurred processing spooler_init(): " + e.getMessage());
            return false;
        }
    }

    public boolean spooler_process() {
        order = null;
        orderData = null;
        File settingsFile = null;
        File reportFile = null;
        File queryFile = null;
        File currQueryFile = null;
        File queryStatementFile = null;
        File outputFile = null;
        File parameterQueryFile = null;
        String stateText = "";
        String tmpOutputFileWithoutExtension = ""; // hilfsvariable
        SOSConnectionFileProcessor queryProcessor = null;
        try {
            spooler_log.debug3("******************spooler_process*****************************");
            this.prepareParams();
            listOfOutputFilename = new ArrayList();
            this.setSettingsFilename("");
            this.setReportFilename("");
            this.setQueryFilename("");
            this.setOutputType("pdf");
            this.setOutputFilename("");
            this.setQueryStatement("");
            this.getJobParameters();
            this.getOrderParameters();
            checkParams();
            try {
                reportFile = new File(this.getReportFilename());
                if (!reportFile.exists()) {
                    throw new Exception("report file does not exist: " + reportFile.getCanonicalPath());
                }
                filledReportFile = File.createTempFile("sos", ".tmp");
                filledReportFile.deleteOnExit();
                queryFile = new File(this.getQueryFilename());
                outputFile = null;
                if (this.getOutputFilename() != null && !this.getOutputFilename().isEmpty()) {
                    String outputFile_ = maskFilename(this.getOutputFilename());
                    outputFile = new File(outputFile_);
                } else {
                    outputFile = File.createTempFile("sos", ".tmp");
                    outputFile.deleteOnExit();
                }
                if (this.isDeleteOldFilename()) {
                    if (outputFile.exists()) {
                        spooler_log.debug3("..deleting old File " + outputFile.getCanonicalPath());
                        if (!outputFile.delete()) {
                            spooler_log.warn("..could not delete old File " + outputFile.getCanonicalPath());
                        } else {
                            spooler_log.debug3("..successfully delete old File " + outputFile.getCanonicalPath());
                        }
                    }
                }
                if (this.getSettingsFilename() != null && !this.getSettingsFilename().isEmpty()) {
                    settingsFile = new File(this.getSettingsFilename());
                    if (!settingsFile.exists()) {
                        throw new Exception("settings file does not exist: " + settingsFile.getCanonicalPath());
                    }
                    queryProcessor = new SOSConnectionFileProcessor(settingsFile.getCanonicalPath(), new sos.util.SOSSchedulerLogger(spooler_log));
                } else {
                    if (this.getConnection() == null) {
                        throw new Exception("job scheduler runs without database");
                    }
                    queryProcessor = new SOSConnectionFileProcessor(this.getConnection(), new sos.util.SOSSchedulerLogger(spooler_log));
                }
                Map parameters = new HashMap();
                Variable_set params = spooler_task.params();
                if (orderData != null) {
                    spooler_log.debug6(".......orderDatanames: " + orderData.names());
                    java.util.StringTokenizer tokenizero = new java.util.StringTokenizer(orderData.names(), ";");
                    while (tokenizero.hasMoreTokens()) {
                        String name = tokenizero.nextToken();
                        parameters.put(name, orderData.var(name));
                        spooler_log.debug6(".......orderData: " + name + "=" + orderData.var(name));
                    }
                }
                spooler_log.debug6(".......paramsnames: " + params.names());
                java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(params.names(), ";");
                while (tokenizer.hasMoreTokens()) {
                    String name = tokenizer.nextToken();
                    parameters.put(name, params.var(name));
                    spooler_log.debug6(".......jobparameter: " + name + "=" + params.var(name));
                }
                if (!sosString.parseToString(parameterQueryFilename).isEmpty()) {
                    parameterQueryFile = new File(this.parameterQueryFilename);
                    if (parameterQueryFile.exists()) {
                        queryProcessor.process(parameterQueryFile);
                        try {
                            parameters.putAll(queryProcessor.getConnection().get());
                        } catch (Exception e) {
                            spooler_log.warn("..error while get Resultset from query Processor Connection: " + e.toString());
                        }
                    } else {
                        spooler_log.warn(".." + parameterQueryFilename + " not exists");
                    }
                }
                if (!sosString.parseToString(parameters.get("report_locale")).isEmpty()) {
                    parameters.put(JRParameter.REPORT_LOCALE, new Locale(sosString.parseToString(parameters.get("report_locale"))));
                } else {
                    parameters.put(JRParameter.REPORT_LOCALE, Locale.GERMAN);
                }
                Object[] param = parameters.entrySet().toArray();
                for (int i = 0; i < param.length; i++) {
                    spooler_log.debug3("..report parameter " + param[i].toString());
                }
                if (!sosString.parseToString(queryStatement).isEmpty()) {
                    queryStatementFile = this.decodeBase64(this.queryStatement);
                    this.spooler_log.debug3("queryStatementFile: " + queryStatementFile);
                    if (queryStatementFile.exists()) {
                        queryProcessor.process(queryStatementFile);
                    }
                }
                if (queryFile.exists()) {
                    SOSPlainTextProcessor processor_ = new SOSPlainTextProcessor();
                    currQueryFile = processor_.process(queryFile, (HashMap) parameters);
                    currQueryFile.deleteOnExit();
                    queryProcessor.process(currQueryFile);
                    this.spooler_log.debug5("query " + processor_.getDocumentContent());
                }

                if (queryFile.exists() || (queryStatementFile != null && queryStatementFile.exists())) {
                    JasperFillManager.fillReportToFile(reportFile.getCanonicalPath(), filledReportFile.getCanonicalPath(), parameters, 
                            new JRResultSetDataSource(queryProcessor.getConnection().getResultSet()));
                } else {
                    JasperFillManager.fillReportToFile(reportFile.getCanonicalPath(), filledReportFile.getCanonicalPath(), parameters, 
                            queryProcessor.getConnection().getConnection());
                }
                tmpOutputFileWithoutExtension = outputFile.getCanonicalPath().substring(0, outputFile.getCanonicalPath().lastIndexOf(".")) + ".";
                if (getOutputType().indexOf("pdf") > -1) {
                    outputFile = new File(tmpOutputFileWithoutExtension + "pdf");
                    JasperExportManager.exportReportToPdfFile(filledReportFile.getCanonicalPath(), outputFile.getCanonicalPath());
                    listOfOutputFilename.add(outputFile);
                }
                if (getOutputType().indexOf("htm") > -1 || getOutputType().indexOf("html") > -1) {
                    if (getOutputType().indexOf("html") > -1) {
                        outputFile = new File(tmpOutputFileWithoutExtension + "html");
                    } else {
                        outputFile = new File(tmpOutputFileWithoutExtension + "htm");
                    }
                    JasperPrint jasperPrint = (JasperPrint) JRLoader.loadObject(filledReportFile);
                    JRHtmlExporter exporter = new JRHtmlExporter();
                    exporter.setParameter(JRHtmlExporterParameter.JASPER_PRINT, jasperPrint);
                    exporter.setParameter(JRHtmlExporterParameter.OUTPUT_FILE_NAME, outputFile.getCanonicalPath());
                    exporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, Boolean.FALSE);
                    exporter.exportReport();
                    listOfOutputFilename.add(outputFile);
                }
                if (this.getOutputType().indexOf("xml") > -1) {
                    outputFile = new File(tmpOutputFileWithoutExtension + "xml");
                    JasperExportManager.exportReportToXmlFile(filledReportFile.getCanonicalPath(), outputFile.getCanonicalPath(), true);
                    listOfOutputFilename.add(outputFile);
                }
                if (this.getOutputType().indexOf("xls") > -1) {
                    outputFile = new File(tmpOutputFileWithoutExtension + "xls");
                    JasperPrint jasperPrint = (JasperPrint) JRLoader.loadObject(filledReportFile);
                    JRXlsExporter exporter = new JRXlsExporter();
                    exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                    exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, outputFile.getCanonicalPath());
                    exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
                    exporter.exportReport();
                    listOfOutputFilename.add(outputFile);
                }
                if (this.getOutputType().indexOf("rtf") > -1) {
                    outputFile = new File(tmpOutputFileWithoutExtension + "rtf");
                    JasperPrint jasperPrint = (JasperPrint) JRLoader.loadObject(filledReportFile);
                    JRRtfExporter exporter = new JRRtfExporter();
                    exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                    exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, outputFile.getCanonicalPath());
                    exporter.exportReport();
                    listOfOutputFilename.add(outputFile);
                }
                stateText = printDocument();
                stateText = stateText + "..report generated in output file: " + tmpOutputFileWithoutExtension + "[" + getOutputType() + "] " + stateText;
                stateText = sendEmail(stateText);
                spooler_log.info(stateText);
                spooler_job.set_state_text(stateText);
            } catch (Exception e) {
                throw new Exception("error occurred processing report: " + e);
            }
            try {
                if (spooler_task.job().order_queue() != null) {
                    order = spooler_task.order();
                    if (order.web_service_operation_or_null() != null) {
                        Web_service_response response = order.web_service_operation().response();
                        if (this.getOutputFilename() != null && !this.getOutputFilename().isEmpty()) {
                            if (order.web_service().params().var("response_stylesheet") != null
                                    && !order.web_service().params().var("response_stylesheet").isEmpty()) {
                                Xslt_stylesheet stylesheet = spooler.create_xslt_stylesheet();
                                stylesheet.load_file(order.web_service().params().var("response_stylesheet"));
                                String xml_document = stylesheet.apply_xml(order.xml());
                                spooler_log.debug3("content of response transformation:/n" + xml_document);
                                response.set_string_content(xml_document);
                            } else {
                                response.set_string_content(order.xml());
                            }
                        } else {
                            BufferedInputStream in = new BufferedInputStream(new FileInputStream(outputFile));
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            byte buffer[] = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = in.read(buffer)) != -1) {
                                out.write(buffer, 0, bytesRead);
                            }
                            order.web_service_operation().response().set_binary_content(out.toByteArray());
                        }
                        response.send();
                        spooler_log.debug3("web service response successfully processed for order \"" + order.id() + "\"");
                    }
                }
            } catch (Exception e) {
                throw new Exception("error occurred processing web service response: " + e.getMessage());
            }
            if (filledReportFile != null && filledReportFile.exists()) {
                spooler_log.debug5("..delete " + filledReportFile.getCanonicalPath() + ": " + filledReportFile.delete());
            }
            if (currQueryFile != null && currQueryFile.exists()) {
                spooler_log.debug5("delete " + currQueryFile.getCanonicalPath() + ": " + currQueryFile.delete());
            }
            return (spooler_task.job().order_queue() != null);
        } catch (Exception e) {
            spooler_job.set_state_text(e.getMessage());
            try {
                if (filledReportFile != null && filledReportFile.exists()) {
                    spooler_log.debug("could delete " + filledReportFile.getCanonicalPath() + ": " + filledReportFile.delete());
                }
            } catch (Exception se) {
            }
            spooler_log.warn(e.getMessage());
            return false;
        }
    }

    private boolean isValidOutputType() throws Exception {
        try {
            if ((this.getOutputType().indexOf("pdf") > -1) || (this.getOutputType().indexOf("htm") > -1) || (this.getOutputType().indexOf("html") > -1)
                    || (this.getOutputType().indexOf("xml") > -1) || (this.getOutputType().indexOf("xls") > -1) || (this.getOutputType().indexOf("rtf") > -1)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            throw new Exception("..error in " + SOSClassUtil.getClassName() + ": " + e);
        }
    }

    public File decodeBase64(String sencode) throws Exception {
        try {
            byte[] buf = new sun.misc.BASE64Decoder().decodeBuffer(sencode.trim());
            File f = File.createTempFile("query_statement", ".sql");
            f.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(buf);
            fos.close();
            return f;
        } catch (Exception e) {
            throw new Exception("..error in " + SOSClassUtil.getClassName() + ": " + e);
        }
    }

    private String printDocument() throws Exception {
        try {
            String prName = getPrinter();
            if (!sosString.parseToString(prName).isEmpty()) {
                JasperPrint jasperPrint = (JasperPrint) JRLoader.loadObject(filledReportFile);
                net.sf.jasperreports.engine.export.JRPrintServiceExporter exporter = new net.sf.jasperreports.engine.export.JRPrintServiceExporter();
                exporter.setParameter(net.sf.jasperreports.engine.export.JRPrintServiceExporterParameter.JASPER_PRINT, jasperPrint);
                javax.print.attribute.PrintRequestAttributeSet aset = new javax.print.attribute.HashPrintRequestAttributeSet();
                aset.add(new Copies(getPrinterCopies()));
                aset.add(MediaSizeName.ISO_A4);
                exporter.setParameter(net.sf.jasperreports.engine.export.JRPrintServiceExporterParameter.PRINT_REQUEST_ATTRIBUTE_SET, aset);
                javax.print.attribute.PrintServiceAttributeSet serviceAttributeSet = new javax.print.attribute.HashPrintServiceAttributeSet();
                serviceAttributeSet.add(new javax.print.attribute.standard.PrinterName(prName, null));
                exporter.setParameter(net.sf.jasperreports.engine.export.JRPrintServiceExporterParameter.PRINT_SERVICE_ATTRIBUTE_SET, serviceAttributeSet);
                exporter.exportReport();
                spooler_log.info("..report successfully printed " + getPrinterCopies() + "x.");
                return "..report successfully printed " + getPrinterCopies() + "x.";
            }
            return "";
        } catch (Exception e) {
            throw new Exception("..error in " + SOSClassUtil.getMethodName() + " " + e);
        }
    }

    public String getOutputFilename() {
        return outputFilename;
    }

    public void setOutputFilename(String outputFilename) {
        this.outputFilename = outputFilename;
    }

    public String getOutputType() {
        return outputType;
    }

    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    public String getReportFilename() {
        return reportFilename;
    }

    public void setReportFilename(String reportFilename) {
        this.reportFilename = reportFilename;
    }

    public String getQueryFilename() {
        return queryFilename;
    }

    public void setQueryFilename(String queryFilename) {
        this.queryFilename = queryFilename;
    }

    public String getParameterQueryFilename() {
        return parameterQueryFilename;
    }

    public void setParameterQueryFilename(String parameterQueryFilename) {
        this.parameterQueryFilename = parameterQueryFilename;
    }

    public String getSettingsFilename() {
        return settingsFilename;
    }

    public void setSettingsFilename(String settingsFilename) {
        this.settingsFilename = settingsFilename;
    }

    public String getPrinterName() {
        return printerName;
    }

    public void setPrinterName(String printerName) {
        this.printerName = printerName;
    }

    public String getFactorySettingsFile() {
        return factorySettingsFile;
    }

    public void setFactorySettingsFile(String factorySettingsFile) {
        this.factorySettingsFile = factorySettingsFile;
    }

    public int getPrinterCopies() {
        return printerCopies;
    }

    public void setPrinterCopies(int printerCopies) {
        this.printerCopies = printerCopies;
    }

    public String getMailBcc() {
        return mailBcc;
    }

    public void setMailBcc(String mailBcc) {
        this.mailBcc = mailBcc;
    }

    public String getMailBody() {
        return mailBody;
    }

    public void setMailBody(String mailBody) {
        this.mailBody = mailBody;
    }

    public String getMailCc() {
        return mailCc;
    }

    public void setMailCc(String mailCc) {
        this.mailCc = mailCc;
    }

    public String getMailSubject() {
        return mailSubject;
    }

    public void setMailSubject(String mailsubject) {
        this.mailSubject = mailsubject;
    }

    public String getMailTo() {
        return mailTo;
    }

    public void setMailTo(String mailTo) {
        this.mailTo = mailTo;
    }

    public boolean isMailIt() {
        return mailIt;
    }

    public void setMailIt(boolean mailIt) {
        this.mailIt = mailIt;
    }

    private SOSConnection getFactoryConnection() throws Exception {
        SOSConnection factoryConnection = null;
        try {
            spooler_log.debug5("DB Connecting.. .");
            factoryConnection = SOSConnection.createInstance(this.getFactorySettingsFile(), new sos.util.SOSSchedulerLogger(spooler_log));
            factoryConnection.connect();
            spooler_log.debug5("DB Connected");
            return factoryConnection;
        } catch (Exception e) {
            throw new Exception("\n -> ..error occurred in " + SOSClassUtil.getMethodName() + ": " + e);
        }
    }

    private String getPrinter() throws Exception {
        SOSConnection facConn = null;
        String printName = "";
        String prefix = "";
        try {
            if (this.getPrinterName().toLowerCase().startsWith("factory:")) {
                prefix = "factory";
                printName = getPrinterName().substring("factory:".length());
            } else {
                printName = getPrinterName();
            }
            if (sosString.parseToString(prefix).isEmpty()) {
                printName = this.getPrinterName();
            } else {
                if (!sosString.parseToString(getFactorySettingsFile()).isEmpty()) {
                    facConn = getFactoryConnection();
                } else {
                    facConn = this.getConnection();
                }
                printName = getActiveFactoryPrinter(facConn, printName);
            }
            spooler_log.debug4("..gedruckt wird auf der Drucker: " + printName + " ");
            return printName;
        } catch (Exception e) {
            throw new Exception("..error in " + SOSClassUtil.getClassName() + ": " + e);
        } finally {
            if (facConn != null) {
                facConn.rollback();
                facConn.disconnect();
            }
        }
    }

    public static String getActiveFactoryPrinter(SOSConnection sosConnection, String queue) throws Exception {
        String rv = "";
        String tableQueues = "LF_QUEUES";
        String tablePrinters = "LF_PRINTERS";
        String tableResources = "LF_RESOURCES";
        HashMap printer = sosConnection.getSingle("SELECT d.\"SYSTEM_NAME\", q.\"STATUS\", "
                + "d.\"STATUS\" as \"PRINTER_STATUS\", r.\"STATUS\" as \"RESOURCE_STATUS\" FROM " + tableQueues + " q, " + "( " + tablePrinters
                + " d LEFT OUTER JOIN " + tableResources + " r ON d.\"SYSTEM_NAME\"=r.\"RESOURCE_KEY\")" + " WHERE q.\"NAME\"='" + queue
                + "' AND d.\"PRINTER\"=q.\"PRINTER\" AND" + " (r.\"RESOURCE\" IS NULL OR r.\"RESOURCE_TYPE\"='printer')");
        if (!printer.isEmpty()) {
            rv = printer.get("system_name").toString();
            String status = printer.get("status").toString();
            if ("0".equals(status)) {
                throw new Exception("Queue " + queue + " is suspended.");
            }
            String printerStatus = printer.get("printer_status").toString();
            if ("0".equals(printerStatus)) {
                throw new Exception("Printer " + rv + " is suspended.");
            }
            if (printer.get("resource_status") != null) {
                String resourceStatus = printer.get("resource_status").toString();
                if ("0".equals(resourceStatus)) {
                    throw new Exception("Resource for printer " + rv + " is suspended.");
                }
            }
        }
        return rv;
    }

    private String sendEmail(String stateText) throws Exception {
        SOSSchedulerLogger sosLogger = null;
        SOSConnection currConn = null;
        String currSubject = "";
        String currBody = "";
        try {
            sosLogger = new SOSSchedulerLogger(this.spooler_log);
            if (isMailIt()) {
                sosLogger.debug("..email sending with mail_it Parameter.");
                if (!getSuspendAttachment()) {
                    for (int i = 0; i < listOfOutputFilename.size(); i++) {
                        this.spooler_log.mail().add_file(sosString.parseToString(listOfOutputFilename.get(i)));
                    }
                }
                if (!sosString.parseToString(getMailSubject()).isEmpty()) {
                    currSubject = this.maskFilename(getMailSubject());
                    spooler_log.mail().set_subject(currSubject);
                    sosLogger.debug("..email subject: " + currSubject);
                } else {
                    spooler_log.mail().set_subject("JasperReports: report delivery");
                }
                if (!sosString.parseToString(getMailBody()).isEmpty()) {
                    currBody = this.maskFilename(getMailBody());
                    spooler_log.mail().set_body(currBody);
                    sosLogger.debug("..email body: " + currBody);
                } else {
                    spooler_log.mail().set_body(stateText);
                }
                if (!sosString.parseToString(getMailTo()).isEmpty()) {
                    spooler_log.mail().set_to(getMailTo());
                    sosLogger.debug("..email send to: " + getMailTo());
                }
                if (!sosString.parseToString(getMailCc()).isEmpty()) {
                    spooler_log.mail().set_cc(getMailCc());
                    sosLogger.debug("..email CC send to: " + getMailCc());
                }
                if (!sosString.parseToString(getMailBcc()).isEmpty()) {
                    spooler_log.mail().set_bcc(getMailBcc());
                    sosLogger.debug("..email BCC send to: " + getMailBcc());
                }
                spooler_log.set_mail_it(true);
                sosLogger.debug("..email successfully send with mail_it Paramater. ");
                return stateText + "..email successfully send. ";
            }
            if (sosString.parseToString(getMailTo()).isEmpty() && sosString.parseToString(getMailCc()).isEmpty() 
                    && sosString.parseToString(getMailBcc()).isEmpty()) {
                sosLogger.debug("..there is no Recipient to send email.");
                return stateText;
            }
            SOSConnectionSettings sett = null;
            SOSMailOrder mailOrder = null;
            try {
                if (!sosString.parseToString(getFactorySettingsFile()).isEmpty()) {
                    sosLogger.debug9(".. get new Connection from " + this.getFactorySettingsFile());
                    currConn = getFactoryConnection();
                } else {
                    currConn = getConnection();
                }
                sett = new SOSConnectionSettings(currConn, "SETTINGS", sosLogger);
                String val = currConn.getSingleValue("SELECT \"NAME\" FROM SETTINGS WHERE \"APPLICATION\" = 'email' AND \"SECTION\" = 'mail_server' "
                        + "AND \"SECTION\" <> \"NAME\"");
                if (!sosString.parseToString(val).isEmpty()) {
                    mailOrder = new SOSMailOrder(sett, currConn);
                } else {
                    sosLogger.warn("..error could not get application [email] and [mail_server] from SETTINGS ");
                    throw new Exception("..error could not get application [email] and [mail_server] from SETTINGS ");
                }
            } catch (Exception e) {
                sosLogger.warn("..error could not get application [email] and [mail_server] from SETTINGS " + e.toString());
                throw new Exception("..error could not get application [email] and [mail_server] from SETTINGS " + e.toString());
            }
            mailOrder.setSOSLogger(sosLogger);
            if (!sosString.parseToString(this.getMailSubject()).isEmpty()) {
                currSubject = this.maskFilename(getMailSubject());
                sosLogger.debug("Mail subject: " + currSubject);
                if (getMailSubject().startsWith("factory:")) {
                    mailOrder.setSubjectTemplateType(SOSMailOrder.TEMPLATE_TYPE_FACTORY);
                    mailOrder.setSubjectTemplate(currSubject.substring("factory:".length()));
                } else if (getMailSubject().startsWith("factory_file:")) {
                    mailOrder.setSubjectTemplateType(SOSMailOrder.TEMPLATE_TYPE_FACTORY_FILE);
                    mailOrder.setSubjectTemplate(currSubject.substring("factory_file:".length()));
                } else if (getMailSubject().startsWith("plain:")) {
                    mailOrder.setSubjectTemplateType(SOSMailOrder.TEMPLATE_TYPE_PLAIN);
                    mailOrder.setSubjectTemplate(currSubject.substring("plain:".length()));
                } else if (getMailSubject().startsWith("plain_file:")) {
                    mailOrder.setSubjectTemplateType(SOSMailOrder.TEMPLATE_TYPE_PLAIN_FILE);
                    mailOrder.setSubjectTemplate(currSubject.substring("plain_file:".length()));
                } else {
                    mailOrder.setSOSLogger(sosLogger);
                    mailOrder.setSubject(currSubject);
                }
            }
            if (!sosString.parseToString(this.getMailBody()).isEmpty()) {
                currBody = this.maskFilename(getMailBody());
                sosLogger.debug("Mail body: " + currBody);
                if (getMailBody().startsWith("factory:")) {
                    mailOrder.setBodyTemplateType(SOSMailOrder.TEMPLATE_TYPE_FACTORY);
                    mailOrder.setBodyTemplate(currBody.substring("factory:".length()));
                } else if (getMailBody().startsWith("factory_file:")) {
                    mailOrder.setBodyTemplateType(SOSMailOrder.TEMPLATE_TYPE_FACTORY_FILE);
                    mailOrder.setBodyTemplate(currBody.substring("factory_file:".length()));
                } else if (getMailBody().startsWith("plain:")) {
                    mailOrder.setBodyTemplateType(SOSMailOrder.TEMPLATE_TYPE_PLAIN);
                    mailOrder.setBodyTemplate(currBody.substring("plain:".length()));
                } else if (getMailBody().startsWith("plain_file:")) {
                    mailOrder.setBodyTemplateType(SOSMailOrder.TEMPLATE_TYPE_PLAIN_FILE);
                    mailOrder.setBodyTemplate(currBody.substring("plain_file:".length()));
                } else {
                    mailOrder.setSOSLogger(sosLogger);
                    mailOrder.setBody(currBody);
                }
            }
            mailOrder.addRecipient(this.getMailTo());
            if (!sosString.parseToString(getMailBcc()).isEmpty()) {
                mailOrder.addBCC(this.getMailBcc());
            }
            if (!sosString.parseToString(getMailCc()).isEmpty()) {
                mailOrder.addCC(this.getMailCc());
            }
            if (!getSuspendAttachment()) {
                for (int i = 0; i < listOfOutputFilename.size(); i++) {
                    mailOrder.addAttachment(sosString.parseToString(listOfOutputFilename.get(i)));
                }
            }
            sosLogger.debug("..replacement job_name = " + this.spooler_task.job().name());
            mailOrder.addReplacement("job_name", this.spooler_task.job().name());
            sosLogger.debug("..replacement job_title = " + this.spooler_task.job().title());
            mailOrder.addReplacement("job_title", this.spooler_task.job().title());
            sosLogger.debug("..replacement state_text = " + stateText);
            mailOrder.addReplacement("state_text", stateText);
            mailOrder.send();
            spooler_log.debug("..email successfully send. ");
            return stateText + "..email successfully send. ";
        } catch (Exception e) {
            throw new Exception("..error in " + SOSClassUtil.getClassName() + ": " + e);
        } finally {
            if (!sosString.parseToString(getFactorySettingsFile()).isEmpty()) {
                if (currConn != null) {
                    currConn.rollback();
                    currConn.disconnect();
                }
            }
        }
    }

    protected void getJobParameters() throws Exception {
        try {
            if (!sosString.parseToString(spooler_task.params().var("settings_filename")).isEmpty()) {
                this.setSettingsFilename(spooler_task.params().var("settings_filename"));
                spooler_log.debug1(".. job parameter [settings_filename]: " + this.getSettingsFilename());
            }
            if (!sosString.parseToString(spooler_task.params().var("report_filename")).isEmpty()) {
                this.setReportFilename(spooler_task.params().var("report_filename"));
                spooler_log.debug1(".. job parameter [report_filename]: " + this.getReportFilename());
            }
            if (!sosString.parseToString(spooler_task.params().var("query_filename")).isEmpty()) {
                this.setQueryFilename(spooler_task.params().var("query_filename"));
                spooler_log.debug1(".. job parameter [query_filename]: " + this.getQueryFilename());
            }
            if (!sosString.parseToString(spooler_task.params().var("query_statement")).isEmpty()) {
                this.setQueryStatement(spooler_task.params().var("query_statement"));
                spooler_log.debug1(".. job parameter [query_statement]: " + this.getQueryStatement());
            }
            if (!sosString.parseToString(spooler_task.params().var("output_type")).isEmpty()) {
                this.setOutputType(spooler_task.params().var("output_type"));
                spooler_log.debug1(".. job parameter [output_type]: " + this.getOutputType());
            }
            if (!sosString.parseToString(spooler_task.params().var("output_filename")).isEmpty()) {
                this.setOutputFilename(spooler_task.params().var("output_filename"));
                spooler_log.debug1(".. job parameter [output_filename]: " + this.getOutputFilename());
            }
            if (!sosString.parseToString(spooler_task.params().var("printer_name")).isEmpty()) {
                this.setPrinterName(spooler_task.params().var("printer_name"));
                spooler_log.debug1(".. job parameter [printer_name]: " + this.getPrinterName());
            }
            if (!sosString.parseToString(spooler_task.params().var("factory_settings_file")).isEmpty()) {
                this.setFactorySettingsFile(spooler_task.params().var("factory_settings_file"));
                spooler_log.debug1(".. job parameter [factory_settings_file]: " + this.getFactorySettingsFile());
            }
            if (!sosString.parseToString(spooler_task.params().var("mail_it")).isEmpty()) {
                this.setMailIt(sosString.parseToBoolean((spooler_task.params().var("mail_it"))));
                spooler_log.debug1(".. job parameter [mail_it]: " + this.isMailIt());
            }
            if (!sosString.parseToString(spooler_task.params().var("parameter_query_filename")).isEmpty()) {
                this.parameterQueryFilename = sosString.parseToString((spooler_task.params().var("parameter_query_filename")));
                spooler_log.debug1(".. job parameter [parameter_query_filename]: " + parameterQueryFilename);
            }
            if (!sosString.parseToString(spooler_task.params().var("printer_copies")).isEmpty()) {
                String pc = sosString.parseToString((spooler_task.params().var("printer_copies")));
                if ("0".equals(pc)) {
                    spooler_log.warn(".. job parameter [printer_copies] is 0 not in range 1..2147483647 ");
                    throw new Exception(".. job parameter [printer_copies] is 0 not in range 1..2147483647 ");
                }
                char c[] = pc.toCharArray();
                for (int i = 0; i < c.length; i++) {
                    if (!(Character.isDigit(c[i]))) {
                        spooler_log.warn(".. job parameter [printer_copies] is not digit: " + pc);
                        throw new Exception(".. job parameter [printer_copies] is not digit: " + pc);
                    }
                }
                this.printerCopies = Integer.parseInt(pc);
                spooler_log.debug1(".. job parameter [printer_copies]: " + printerCopies);
            }
            if (!sosString.parseToString(spooler_task.params().var("mail_to")).isEmpty()) {
                this.setMailTo(sosString.parseToString((spooler_task.params().var("mail_to"))));
                spooler_log.debug1(".. job parameter [mail_to]: " + this.getMailTo());
            }
            if (!sosString.parseToString(spooler_task.params().var("mail_cc")).isEmpty()) {
                this.setMailCc(sosString.parseToString((spooler_task.params().var("mail_cc"))));
                spooler_log.debug1(".. job parameter [mail_cc]: " + this.getMailCc());
            }
            if (!sosString.parseToString(spooler_task.params().var("mail_bcc")).isEmpty()) {
                this.setMailBcc(sosString.parseToString((spooler_task.params().var("mail_bcc"))));
                spooler_log.debug1(".. job parameter [mail_bcc]: " + this.getMailBcc());
            }
            if (!sosString.parseToString(spooler_task.params().var("mail_subject")).isEmpty()) {
                this.setMailSubject(sosString.parseToString((spooler_task.params().var("mail_subject"))));
                spooler_log.debug1(".. job parameter [mail_subject]: " + this.getMailSubject());
            }
            if (!sosString.parseToString(spooler_task.params().var("mail_body")).isEmpty()) {
                this.setMailBody(sosString.parseToString((spooler_task.params().var("mail_body"))));
                spooler_log.debug1(".. job parameter [mail_body]: " + this.getMailBody());
            }
            if (!sosString.parseToString(spooler_task.params().var("suspend_attachment")).isEmpty()) {
                this.setSuspendAttachment(sosString.parseToBoolean((spooler_task.params().var("suspend_attachment"))));
                spooler_log.debug1(".. job parameter [suspend_attachment]: " + this.getSuspendAttachment());
            }
            if (!sosString.parseToString(spooler_task.params().var("delete_old_output_file")).isEmpty()) {
                this.setDeleteOldFilename(sosString.parseToBoolean((spooler_task.params().var("delete_old_output_file"))));
                spooler_log.debug1(".. job parameter [delete_old_output_file]: " + this.isDeleteOldFilename());
            }
        } catch (Exception e) {
            throw new Exception("..error occurred processing job parameters: " + e.getMessage());
        }
    }

    private void getOrderParameters() throws Exception {
        try {
            if (spooler_task.job().order_queue() != null) {
                order = spooler_task.order();
                if (order.web_service_operation_or_null() != null) {
                    SOSXMLXPath xpath = null;
                    Web_service_request request = order.web_service_operation().request();
                    if (order.web_service().params().var("request_stylesheet") != null && !order.web_service().params().var("request_stylesheet").isEmpty()) {
                        Xslt_stylesheet stylesheet = spooler.create_xslt_stylesheet();
                        stylesheet.load_file(order.web_service().params().var("request_stylesheet"));
                        String xml_document = stylesheet.apply_xml(request.string_content());
                        spooler_log.debug3("content of request:\n" + request.string_content());
                        spooler_log.debug3("content of request transformation:\n" + xml_document);
                        xpath = new sos.xml.SOSXMLXPath(new java.lang.StringBuffer(xml_document));
                        Variable_set params = spooler.create_variable_set();
                        if (xpath.selectSingleNodeValue("//param[@name[.='settings_filename']]/@value") != null) {
                            params.set_var("settings_filename", xpath.selectSingleNodeValue("//param[@name[.='settings_filename']]/@value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[@name[.='report_filename']]/@value") != null) {
                            params.set_var("report_filename", xpath.selectSingleNodeValue("//param[@name[.='report_filename']]/@value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[@name[.='query_filename']]/@value") != null) {
                            params.set_var("query_filename", xpath.selectSingleNodeValue("//param[@name[.='query_filename']]/@value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[@name[.='query_statement']]/@value") != null) {
                            params.set_var("query_statement", xpath.selectSingleNodeValue("//param[@name[.='query_statement']]/@value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[@name[.='output_type']]/@value") != null) {
                            params.set_var("output_type", xpath.selectSingleNodeValue("//param[@name[.='output_type']]/@value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[@name[.='output_filename']]/@value") != null) {
                            params.set_var("output_filename", xpath.selectSingleNodeValue("//param[@name[.='output_filename']]/@value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[@name[.='scheduler_order_report_mailto']]/@value") != null) {
                            params.set_var("scheduler_order_report_mailto", xpath.selectSingleNodeValue("//param[@name[.='scheduler_order_report_mailto']]/@value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[@name[.='scheduler_order_report_printer_id']]/@value") != null) {
                            params.set_var("scheduler_order_report_printer_id", 
                                    xpath.selectSingleNodeValue("//param[@name[.='scheduler_order_report_printer_id']]/@value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[@name[.='printer_name']]/@value") != null) {
                            params.set_var("printer_name", xpath.selectSingleNodeValue("//param[@name[.='printer_name']]/@value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[@name[.='factory_settings_file']]/@value") != null) {
                            params.set_var("factory_settings_file", xpath.selectSingleNodeValue("//param[@name[.='factory_settings_file']]/@value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[@name[.='parameter_query_filename']]/@value") != null) {
                            params.set_var("parameter_query_filename", xpath.selectSingleNodeValue("//param[@name[.='parameter_query_filename']]/@value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[@name[.='printer_copies']]/@value") != null) {
                            params.set_var("printer_copies", xpath.selectSingleNodeValue("//param[@name[.='printer_copies']]/@value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[@name[.='mail_it']]/@value") != null) {
                            params.set_var("mail_it", xpath.selectSingleNodeValue("//param[@name[.='mail_it']]/@value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[@name[.='mail_to']]/@value") != null) {
                            params.set_var("mail_to", xpath.selectSingleNodeValue("//param[@name[.='mail_to']]/@value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[@name[.='mail_cc']]/@value") != null) {
                            params.set_var("mail_cc", xpath.selectSingleNodeValue("//param[@name[.='mail_cc']]/@value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[@name[.='mail_bcc']]/@value") != null) {
                            params.set_var("mail_bcc", xpath.selectSingleNodeValue("//param[@name[.='mail_bcc']]/@value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[@name[.='mail_subject']]/@value") != null) {
                            params.set_var("mail_subject", xpath.selectSingleNodeValue("//param[@name[.='mail_subject']]/@value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[@name[.='mail_body']]/@value") != null) {
                            params.set_var("mail_body", xpath.selectSingleNodeValue("//param[@name[.='mail_body']]/@value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[@name[.='suspend_attachment']]/@value") != null) {
                            params.set_var("suspend_attachment", xpath.selectSingleNodeValue("//param[@name[.='suspend_attachment']]/@value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[@name[.='delete_old_output_file']]/@value") != null) {
                            params.set_var("delete_old_output_file", xpath.selectSingleNodeValue("//param[@name[.='delete_old_output_file']]/@value"));
                        }
                        order.set_payload(params);
                    } else {
                        xpath = new sos.xml.SOSXMLXPath(new java.lang.StringBuffer(request.string_content()));
                        Variable_set params = spooler.create_variable_set();
                        if (xpath.selectSingleNodeValue("//param[name[text()='settings_filename']]/value") != null) {
                            params.set_var("settings_filename", xpath.selectSingleNodeValue("//param[name[text()='settings_filename']]/value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[name[text()='report_filename']]/value") != null) {
                            params.set_var("report_filename", xpath.selectSingleNodeValue("//param[name[text()='report_filename']]/value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[name[text()='query_filename']]/value") != null) {
                            params.set_var("query_filename", xpath.selectSingleNodeValue("//param[name[text()='query_filename']]/value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[name[text()='query_statement']]/value") != null) {
                            params.set_var("query_statement", xpath.selectSingleNodeValue("//param[name[text()='query_statement']]/value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[name[text()='output_type']]/value") != null) {
                            params.set_var("output_filename", xpath.selectSingleNodeValue("//param[name[text()='output_type']]/value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[name[text()='output_filename']]/value") != null) {
                            params.set_var("output_filename", xpath.selectSingleNodeValue("//param[name[text()='output_filename']]/value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[name[text()='scheduler_order_report_mailto']]/value") != null) {
                            params.set_var("scheduler_order_report_mailto", 
                                    xpath.selectSingleNodeValue("//param[name[text()='scheduler_order_report_mailto']]/value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[name[text()='scheduler_order_report_printer_id']]/value") != null) {
                            params.set_var("scheduler_order_report_printer_id", 
                                    xpath.selectSingleNodeValue("//param[name[text()='scheduler_order_report_printer_id']]/value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[name[text()='printer_name']]/value") != null) {
                            params.set_var("printer_name", xpath.selectSingleNodeValue("//param[name[text()='printer_name']]/value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[name[text()='factory_settings_file']]/value") != null) {
                            params.set_var("factory_settings_file", xpath.selectSingleNodeValue("//param[name[text()='factory_settings_file']]/value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[name[text()='parameter_query_filename']]/value") != null) {
                            params.set_var("parameter_query_filename", xpath.selectSingleNodeValue("//param[name[text()='parameter_query_filename']]/value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[name[text()='printer_copies']]/value") != null) {
                            params.set_var("printer_copies", xpath.selectSingleNodeValue("//param[name[text()='printer_copies']]/value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[name[text()='mail_it']]/value") != null) {
                            params.set_var("mail_it", xpath.selectSingleNodeValue("//param[name[text()='mail_it']]/value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[name[text()='mail_to']]/value") != null) {
                            params.set_var("mail_to", xpath.selectSingleNodeValue("//param[name[text()='mail_to']]/value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[name[text()='mail_cc']]/value") != null) {
                            params.set_var("mail_cc", xpath.selectSingleNodeValue("//param[name[text()='mail_cc']]/value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[name[text()='mail_bcc']]/value") != null) {
                            params.set_var("mail_bcc", xpath.selectSingleNodeValue("//param[name[text()='mail_bcc']]/value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[name[text()='mail_subject']]/value") != null) {
                            params.set_var("mail_subject", xpath.selectSingleNodeValue("//param[name[text()='mail_subject']]/value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[name[text()='mail_body']]/value") != null) {
                            params.set_var("mail_body", xpath.selectSingleNodeValue("//param[name[text()='mail_body']]/value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[name[text()='suspend_attachment']]/value") != null) {
                            params.set_var("suspend_attachment", xpath.selectSingleNodeValue("//param[name[text()='suspend_attachment']]/value"));
                        }
                        if (xpath.selectSingleNodeValue("//param[name[text()='delete_old_output_file']]/value") != null) {
                            params.set_var("delete_old_output_file", xpath.selectSingleNodeValue("//param[name[text()='delete_old_output_file']]/value"));
                        }
                        order.set_payload(params);
                    }
                }
                orderData = (Variable_set) order.payload();
                if (orderData != null && orderData.var("settings_filename") != null && !orderData.var("settings_filename").toString().isEmpty()) {
                    this.setSettingsFilename(orderData.var("settings_filename").toString());
                    spooler_log.debug1(".. order parameter [settings_filename]: " + this.getSettingsFilename());
                }
                if (orderData != null && orderData.var("report_filename") != null && !orderData.var("report_filename").toString().isEmpty()) {
                    this.setReportFilename(orderData.var("report_filename").toString());
                    spooler_log.debug1(".. order parameter [report_filename]: " + this.getReportFilename());
                }
                if (orderData != null && orderData.var("query_filename") != null && !orderData.var("query_filename").toString().isEmpty()) {
                    this.setQueryFilename(orderData.var("query_filename").toString());
                    spooler_log.debug1(".. order parameter [query_filename]: " + this.getQueryFilename());
                }
                if (orderData != null && orderData.var("query_statement") != null && !orderData.var("query_statement").toString().isEmpty()) {
                    this.setQueryStatement(orderData.var("query_statement").toString());
                    spooler_log.debug1(".. order parameter [query_statement]: " + this.getQueryStatement());
                }
                if (orderData != null && orderData.var("output_type") != null && !orderData.var("output_type").toString().isEmpty()) {
                    this.setOutputType(orderData.var("output_type").toString());
                    spooler_log.debug1(".. order parameter [output_type]: " + this.getOutputType());
                }
                if (orderData != null && orderData.var("output_filename") != null && !orderData.var("output_filename").toString().isEmpty()) {
                    this.setOutputFilename(orderData.var("output_filename").toString());
                    spooler_log.debug1(".. order parameter [output_filename]: " + this.getOutputFilename());
                }
                if (orderData != null && orderData.var("printer_name") != null && !orderData.var("printer_name").toString().isEmpty()) {
                    this.setPrinterName(orderData.var("printer_name").toString());
                    spooler_log.debug1(".. order parameter [printer_name]: " + this.getPrinterName());
                }
                if (orderData != null && orderData.var("factory_settings_file") != null && !orderData.var("factory_settings_file").toString().isEmpty()) {
                    this.setFactorySettingsFile(orderData.var("factory_settings_file").toString());
                    spooler_log.debug1(".. order parameter [factory_settings_file]: " + this.getFactorySettingsFile());
                }
                if (orderData != null && orderData.var("parameter_query_filename") != null && !orderData.var("parameter_query_filename").toString().isEmpty()) {
                    this.parameterQueryFilename = this.sosString.parseToString((orderData.var("parameter_query_filename")));
                    spooler_log.debug1(".. order parameter [parameter_query_filename]: " + parameterQueryFilename);
                }
                if (orderData != null && orderData.var("printer_copies") != null && !orderData.var("printer_copies").toString().isEmpty()) {
                    this.setPrinterCopies(Integer.parseInt(this.sosString.parseToString((orderData.var("printer_copies")))));
                    spooler_log.debug1(".. order parameter [printer_copies]: " + this.getPrinterCopies());
                }
                if (orderData != null && !sosString.parseToString(orderData.var("mail_it")).isEmpty()) {
                    this.setMailIt(sosString.parseToBoolean((orderData.var("mail_it"))));
                    spooler_log.debug1(".. order parameter [mail_it]: " + this.isMailIt());
                }
                if (orderData != null && !sosString.parseToString(orderData.var("mail_to")).isEmpty()) {
                    this.setMailTo(sosString.parseToString((orderData.var("mail_to"))));
                    spooler_log.debug1(".. order parameter [mail_to]: " + this.getMailTo());
                }
                if (orderData != null && !sosString.parseToString(orderData.var("mail_cc")).isEmpty()) {
                    this.setMailCc(sosString.parseToString((orderData.var("mail_cc"))));
                    spooler_log.debug1(".. order parameter [mail_cc]: " + this.getMailCc());
                }
                if (orderData != null && !sosString.parseToString(orderData.var("mail_bcc")).isEmpty()) {
                    this.setMailBcc(sosString.parseToString((orderData.var("mail_bcc"))));
                    spooler_log.debug1(".. order parameter [mail_bcc]: " + this.getMailBcc());
                }
                if (orderData != null && !sosString.parseToString(orderData.var("mail_subject")).isEmpty()) {
                    this.setMailSubject(sosString.parseToString((orderData.var("mail_subject"))));
                    spooler_log.debug1(".. order parameter [mail_subject]: " + this.getMailSubject());
                }
                if (orderData != null && !sosString.parseToString(orderData.var("mail_body")).isEmpty()) {
                    this.setMailBody(sosString.parseToString((orderData.var("mail_body"))));
                    spooler_log.debug1(".. order parameter [mail_body]: " + this.getMailBody());
                }
                if (orderData != null && !sosString.parseToString(orderData.var("suspend_attachment")).isEmpty()) {
                    this.setSuspendAttachment(sosString.parseToBoolean((orderData.var("suspend_attachment"))));
                    spooler_log.debug1(".. order parameter [suspend_attachment]: " + this.getSuspendAttachment());
                }

                if (orderData != null && !sosString.parseToString(orderData.var("delete_old_output_file")).isEmpty()) {
                    this.setDeleteOldFilename(sosString.parseToBoolean((orderData.var("delete_old_output_file"))));
                    spooler_log.debug1(".. order parameter [delete_old_output_file]: " + this.isDeleteOldFilename());
                }
            }
        } catch (Exception e) {
            throw new Exception("error occurred processing parameters: " + e.toString());
        }
    }

    private void checkParams() throws Exception {
        try {
            if (this.getReportFilename() == null || this.getReportFilename().isEmpty()) {
                throw new Exception("no report filename was given");
            }
            if (this.getOutputType() == null || this.getOutputType().isEmpty()) {
                throw new Exception("no output type [pdf, html, xml, xls, rtf] was given");
            }
            if (!isValidOutputType()) {
                throw new Exception("unsupported output type [pdf, htm, xml, xls, rtf]: " + this.getOutputType());
            }
            if (!sosString.parseToString(this.queryFilename).isEmpty() && !sosString.parseToString(this.queryStatement).isEmpty()) {
                throw new Exception("to many parameter [query_filename] and [query_statement]");
            }
        } catch (Exception e) {
            throw new Exception("error occurred checking parameters: " + e.toString());
        }
    }

    public String getQueryStatement() {
        return queryStatement;
    }

    public void setQueryStatement(String queryStatement) {
        this.queryStatement = queryStatement;
    }

    public boolean getSuspendAttachment() {
        return suspendAttachment;
    }

    public void setSuspendAttachment(boolean suspendAttachment) {
        this.suspendAttachment = suspendAttachment;
    }

    private void init() throws Exception {
        try {
            settingsFilename = "";
            reportFilename = "";
            queryFilename = "";
            outputType = "pdf";
            outputFilename = "";
            printerName = "";
            factorySettingsFile = "";
            sosString = new SOSString();
            filledReportFile = null;
            listOfOutputFilename = null;
            parameterQueryFilename = "";
            order = null;
            orderData = null;
            printerCopies = 1;
            mailIt = false;
            mailTo = "";
            mailCc = "";
            mailBcc = "";
            mailSubject = "";
            mailBody = "";
            queryStatement = "";
            suspendAttachment = false;
        } catch (Exception e) {
            throw new Exception("..error in " + SOSClassUtil.getMethodName() + " :" + e);
        }
    }

    private String maskFilename(String filename) throws Exception {
        String targetFilename = filename;
        try {
            if (targetFilename.matches("(.*)(\\[date\\:)([^\\]]+)(\\])(.*)")) {
                int posBegin = targetFilename.indexOf("[date:");
                if (posBegin > -1) {
                    int posEnd = targetFilename.indexOf("]", posBegin + 6);
                    if (posEnd > -1) {
                        targetFilename = ((posBegin > 0) ? targetFilename.substring(0, posBegin) : "")
                                + SOSDate.getCurrentTimeAsString(targetFilename.substring(posBegin + 6, posEnd))
                                + ((targetFilename.length() > posEnd) ? targetFilename.substring(posEnd + 1) : "");
                    }
                }
            }
            if (targetFilename.indexOf("[lastmonth]") > -1) {
                String lastMonth_ = SOSDate.getDateAsString(getLastMonth(SOSDate.getCurrentTimeAsString("dd-MM-yyyy")), "MM-yyyy");
                targetFilename = targetFilename.replaceAll("\\[lastmonth\\]", lastMonth_);
            }
            Matcher m = Pattern.compile("\\[[^\\]]*\\]").matcher(targetFilename);
            if (m.find()) {
                throw new Exception("unsupported file mask found:" + m.group());
            }
            spooler_log.debug(filename + " mask in " + targetFilename);
            return targetFilename;
        } catch (Exception e) {
            throw new Exception("..error in " + SOSClassUtil.getMethodName() + " : " + e.getMessage());
        }
    }

    private Date getLastMonth(String MMyy) throws Exception {
        try {
            Date d = SOSDate.getDate(MMyy, "dd-MM-yy");
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            cal.add(cal.MONTH, -1);
            Date s = cal.getTime();
            return s;
        } catch (Exception e) {
            throw e;
        }
    }

    public boolean isDeleteOldFilename() {
        return deleteOldFilename;
    }

    public void setDeleteOldFilename(boolean deleteOldFilename) {
        this.deleteOldFilename = deleteOldFilename;
    }

}
