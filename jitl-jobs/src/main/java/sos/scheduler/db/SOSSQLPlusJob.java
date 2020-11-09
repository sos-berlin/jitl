package sos.scheduler.db;

import static com.sos.scheduler.messages.JSMessages.JSJ_F_107;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_110;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_111;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.Files.JSTextFile;
import com.sos.vfs.common.SOSShell;

public class SOSSQLPlusJob extends JSJobUtilitiesClass<SOSSQLPlusJobOptions> {

    private static final String CLASSNAME = "SOSSQLPlusJob";
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSQLPlusJob.class);

    public SOSSQLPlusJob() {
        super(new SOSSQLPlusJobOptions());
    }

    public SOSSQLPlusJob execute() throws Exception {
        final String conMethodName = CLASSNAME + "::Execute";
        JSJ_I_110.toLog(conMethodName);
        try {
            getOptions().checkMandatory();
            LOGGER.debug(objOptions.dirtyString());
            SOSShell objShell = new SOSShell();
            String strCommand = objOptions.shell_command.getOptionalQuotedValue();
            if (objShell.isWindows()) {
                strCommand = "echo 1 | " + strCommand;
            }
            String strCommandParams = "";
            if (objOptions.CommandLineOptions.isNotEmpty()) {
                strCommand += " " + objOptions.CommandLineOptions.getValue();
            }
            String strDBConn = objOptions.getConnectionString();
            if (!strDBConn.isEmpty()) {
                strCommandParams += " " + strDBConn;
            }
            File objTempFile = File.createTempFile("sos", ".sql");
            String strTempFileName = objTempFile.getAbsolutePath();
            objOptions.command_script_file.checkMandatory();
            if (objOptions.command_script_file.isDirty()) {
                strCommandParams += " @" + strTempFileName;
            }
            HashMap<String, String> objSettings = getSettings4StepName();
            JSTextFile objTF = new JSTextFile(strTempFileName);
            for (final Object element : objSettings.entrySet()) {
                final Map.Entry<String, String> mapItem = (Map.Entry<String, String>) element;
                String strMapKey = mapItem.getKey().toString();
                strMapKey = sqlPlusVariableName(strMapKey);
                if (mapItem.getValue() != null) {
                    strMapKey = strMapKey.replace(".", "_");
                    String strT = String.format("DEFINE %1$s = %2$s (char)", strMapKey, addQuotes(mapItem.getValue().toString()));
                    if ("db_password".equals(strMapKey)) {
                        LOGGER.debug("DEFINE db_password = ******** (char)");
                    }else {
                        LOGGER.debug(strT);
                    }
                    objTF.writeLine(strT);
                }
            }
            if (objOptions.include_files.isDirty()) {
                String[] strA = objOptions.include_files.getValue().split(";");
                for (String strFileName2Include : strA) {
                    LOGGER.debug(String.format("Append file '%1$s' to script", strFileName2Include));
                    objTF.appendFile(strFileName2Include);
                }
            }
            final String conNL = System.getProperty("line.separator");
            objOptions.Shell_command_Parameter.setValue(strCommandParams);
            String strFC = objOptions.command_script_file.getValue();
            strFC = objJSJobUtilities.replaceSchedulerVars(strFC);
            LOGGER.debug(objOptions.command_script_file.getValue());
            strFC += "\n" + "exit;\n";
            objTF.writeLine(strFC);
            int intCC = executeCommand(objOptions, objShell);
            String strCC = String.valueOf(intCC);
            String f = "00000";
            strCC = f.substring(0, strCC.length() - 1) + strCC;
            String strSQLError = "";
            int intRegExpFlags = Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.DOTALL;
            String strStdOut = objShell.getStdOut();
            String[] strA = strStdOut.split(conNL);
            boolean flgAVariableFound = false;
            String strRegExp = objOptions.VariableParserRegExpr.getValue();
            Pattern objRegExprPattern = Pattern.compile(strRegExp, intRegExpFlags);
            for (String string : strA) {
                Matcher objMatch = objRegExprPattern.matcher(string);
                if (objMatch.matches()) {
                    objJSJobUtilities.setJSParam(objMatch.group(1), objMatch.group(2).trim());
                    flgAVariableFound = true;
                }
            }
            if (!flgAVariableFound) {
                LOGGER.info(String.format("no JS-variable definitions found using reg-exp '%1$s'.", strRegExp));
            }
            boolean flgIgnoreSP2MsgNo = false;
            if (objOptions.ignore_sp2_messages.contains("*all")) {
                flgIgnoreSP2MsgNo = true;
            }
            boolean flgIgnoreOraMsgNo = false;
            if (objOptions.ignore_ora_messages.contains("*all")) {
                flgIgnoreOraMsgNo = true;
            }
            Pattern objErrorPattern = Pattern.compile("^\\s*SP2-(\\d\\d\\d\\d):\\s*(.*)$", intRegExpFlags);
            Pattern objORAPattern = Pattern.compile("^ORA-(\\d\\d\\d\\d\\d):\\s*(.*)$", intRegExpFlags);
            for (String strStdoutLine : strA) {
                strStdoutLine = strStdoutLine.trim();
                Matcher objMatch = objErrorPattern.matcher(strStdoutLine);
                Matcher objMatch2 = objORAPattern.matcher(strStdoutLine);
                if (objMatch.matches() || objMatch2.matches()) {
                    boolean flgIsError = false;
                    if (objMatch.matches() && !flgIgnoreSP2MsgNo) {
                        String strMsgNo = objMatch.group(1).toString();
                        if (!objOptions.ignore_sp2_messages.contains(strMsgNo)) {
                            flgIsError = true;
                        }
                    }
                    if (objMatch2.matches() && !flgIgnoreOraMsgNo) {
                        String strMsgNo = objMatch2.group(1).toString();
                        if (!objOptions.ignore_ora_messages.contains(strMsgNo)) {
                            flgIsError = true;
                        }
                    }
                    if (flgIsError) {
                        strSQLError += strStdoutLine + conNL;
                        LOGGER.debug("error found: " + strStdoutLine);
                        objJSJobUtilities.setStateText(strStdoutLine);
                    } else {
                        LOGGER.info(String.format("Error '%1$s' ignored due to settings", strStdoutLine));
                    }
                } else {
                    objJSJobUtilities.setStateText("");
                }
            }
            String strStdErr = objShell.getStdErr();
            objJSJobUtilities.setJSParam(conSettingSQL_ERROR, strSQLError.trim());
            if (intCC == 0) {
                if (!strStdErr.trim().isEmpty()) {
                    intCC = 99;
                }
                if (!strSQLError.isEmpty()) {
                    intCC = 98;
                }
            }
            objJSJobUtilities.setJSParam(conSettingEXIT_CODE, "" + intCC);
            if (intCC != 0 && !objOptions.ignore_ora_messages.contains(strCC)) {
                throw new JobSchedulerException(String.format("Exit-Code set to '%1$s': %2$s", "" + intCC, strSQLError.trim()));
            }
        } catch (Exception e) {
            throw new JobSchedulerException(JSJ_F_107.get(conMethodName) + ": " + e.getMessage(), e);
        }
        JSJ_I_111.toLog(conMethodName);
        return this;
    }

    public int executeCommand(final SOSSQLPlusJobOptions options, final SOSShell shell) throws Exception {
        return shell.executeCommand(createCommandUsingOptions(options, shell),false);
    }

    private String[] createCommands(final SOSSQLPlusJobOptions options, final SOSShell shell, final String envComSpecName,
            final String defaultComSpec, final String startParam) {
        final String[] command = { " ", " ", " " };
        String comSpec = "";
        int indx = 0;
        String startShellCommandParam = startParam;
        if (options.getStartShellCommand().isDirty()) {
            comSpec = options.getStartShellCommand().getValue();
            if (!"none".equalsIgnoreCase(comSpec)) {
                command[indx++] = comSpec;
                if (options.getStartShellCommandParameter().isDirty()) {
                    startShellCommandParam = options.getStartShellCommandParameter().getValue();
                    command[indx++] = startShellCommandParam;
                }
                command[indx++] = options.getShellCommand().getValue() + " " + options.getCommandLineOptions().getValue() + " " + options
                        .getShellCommandParameter().getValue();
            } else {
                command[indx++] = options.getShellCommand().getValue();
                command[indx++] = options.getCommandLineOptions().getValue() + " " + options.getShellCommandParameter().getValue();
            }
        } else {
            comSpec = System.getenv(envComSpecName);
            if (comSpec == null) {
                comSpec = defaultComSpec;
            }
            command[indx++] = comSpec;
            command[indx++] = startShellCommandParam;
            command[indx++] = options.getShellCommand().getValue() + " " + options.getCommandLineOptions().getValue() + " " + options
                    .getShellCommandParameter().getValue();
        }
        return command;
    }

    private String[] createCommandUsingOptions(final SOSSQLPlusJobOptions options, SOSShell shell) {
        if (shell.isWindows()) {
            return createCommands(options, shell, "comspec", "cmd.exe", "/C");
        } else {
            return createCommands(options, shell, "SHELL", "/bin/sh", "-c");
        }
    }

    private HashMap<String, String> getSettings4StepName() {
        HashMap<String, String> objS = new HashMap<String, String>();
        int intStartPos = objOptions.getCurrentNodeName().length() + 1;
        for (Map.Entry<String, String> mapItem : objOptions.getSettings().entrySet()) {
            String strMapKey = mapItem.getKey();
            String strValue = mapItem.getValue();
            if (strMapKey.indexOf("/") != -1) {
                if (strMapKey.startsWith(objOptions.getCurrentNodeName() + "/")) {
                    strMapKey = strMapKey.substring(intStartPos);
                } else {
                    strValue = null;
                }
            }
            if (strValue != null) {
            	if ("db_password".equals(strMapKey)) {
                    strValue = "********";
                }
                LOGGER.debug(strMapKey + " = " + strValue);
                objS.put(strMapKey, strValue);
            }
        }
        return objS;
    }

    public String sqlPlusVariableName(String s) {
        if (s.length() > 30) {
            s = s.substring(0, 29) + "_";
        }
        return s;
    }

}