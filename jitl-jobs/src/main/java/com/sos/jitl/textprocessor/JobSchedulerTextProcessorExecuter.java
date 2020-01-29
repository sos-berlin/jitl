package com.sos.jitl.textprocessor;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JobSchedulerTextProcessorExecuter {

    private static Logger logger = LoggerFactory.getLogger(JobSchedulerTextProcessorExecuter.class);
    private String command;
    private HashMap<String, String> commands;
    private File file;
    private String param = "";

    public JobSchedulerTextProcessorExecuter(File file_, String command_) throws Exception {
        this.command = command_.trim().toLowerCase().replaceAll("\\s{2,}", " ");
        this.file = file_;
        param = command.replaceFirst("^[^\\s]+\\s*(.*)$", "$1");
        command = command.replaceFirst("^([^\\s]+)\\s*.*$", "$1");
        commands = new HashMap<String, String>();
        commands.put("count", "1");
        commands.put("countCaseSensitive", "2");
        commands.put("add", "3");
        commands.put("read", "4");
        commands.put("insert", "5");
    }

    public String execute() throws Exception {
        return go();
    }

    public String execute(String command_) throws Exception {
        param = command_.replaceFirst("^[^\\s]+\\s*(.*)$", "$1");
        command = command_.replaceFirst("^([^\\s]+)\\s*.*$", "$1");
        return go();
    }

    public String exexute(String command_, String param_) throws Exception {
        this.command = command_;
        this.param = param_;
        return go();
    }

    private String count(boolean ignoreCase) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String rec = null;
        String s = param;
        if (ignoreCase) {
            s = s.toLowerCase();
        }
        int i = 0;
        while ((rec = reader.readLine()) != null) {
            if (ignoreCase) {
                rec = rec.toLowerCase();
            }
            while (rec.indexOf(s) >= 0) {
                i++;
                rec = rec.replaceFirst(s, "");
            }
        }
        reader.close();
        return String.valueOf(i);
    }

    private String add() throws IOException {
        FileOutputStream f = new FileOutputStream(file, true);
        String s = "\n" + param;
        f.write(s.getBytes(), 0, s.length());
        f.close();
        return param;
    }

    private String insert() throws Exception {
        String line = param.replaceFirst("^[^\\s]+\\s*(.*).*$", "$1");
        String c = line.replaceFirst("^.*\\{char:\\s*([0-9]+)\\s*\\}.*$", "$1");
        if (!c.equals(line)) {
            int intVal = 0;
            try {
                intVal = Integer.parseInt(c, 10);
            } catch (NumberFormatException e) {
                logger.warn(c + " is not a valid number. 0 assumed");
                intVal = 0;
            }
            char charVal = (char) intVal;
            String s = String.valueOf(charVal);
            line = line.replaceFirst("\\{char:\\s*" + c + "\\s*\\}", s);
        }
        line = line + "\n";
        param = param.replaceFirst("^([^\\s]+)\\s*.*$", "$1");
        if ("last".equals(param)) {
            param = line;
            add();
        } else {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String rec = "";
            int i = 0;
            if ("first".equals(param)) {
                i = 1;
            } else {
                try {
                    i = Integer.parseInt(param);
                } catch (NumberFormatException e) {
                    logger.error(param + " is not a valid line number: 0 assumed");
                    i = 0;
                }
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            i--;
            while ((rec = reader.readLine()) != null && i > 0) {
                rec = rec + "\n";
                baos.write(rec.getBytes());
                i--;
            }
            baos.write(line.getBytes());
            if (rec != null) {
                rec = rec + "\n";
                baos.write(rec.getBytes());
            }
            while ((rec = reader.readLine()) != null) {
                rec = rec + "\n";
                baos.write(rec.getBytes());
            }
            reader.close();
            FileOutputStream f = new FileOutputStream(file, false);
            f.write(baos.toByteArray());
            f.close();
        }
        return param;
    }

    private String read() throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String rec = "";
        String erg = "";
        int i = 0;
        if ("first".equals(param)) {
            i = 1;
        } else {
            if (!"last".equals(param)) {
                try {
                    i = Integer.parseInt(param);
                } catch (NumberFormatException e) {
                    logger.error(param + " is not a valid line number: 0 assumed");
                    i = 0;
                }
            }
        }
        while ((rec = reader.readLine()) != null && ("last".equals(param) || i > 0)) {
            erg = rec;
            i--;
        }
        if (!"last".equals(param) && rec == null && i > 0) {
            erg = "(eof)";
        }
        reader.close();
        return erg;
    }

    private String go() throws Exception {
        String erg = "";
        if ("".equals(param)) {
            throw new Exception("Param missing in: " + command);
        }
        int command_id = getCommandId();
        switch (command_id) {
        case 1:
            return count(true);
        case 2:
            return count(false);
        case 3:
            return add();
        case 4:
            return read();
        case 5:
            return insert();
        }
        return erg;
    }

    private int getCommandId() throws Exception {
        if (commands.get(command) == null) {
            throw new Exception("Unknown command: (not in count, add, read) " + command);
        }
        String s = commands.get(command).toString();
        int commandId = 0;
        if (s != null) {
            commandId = Integer.parseInt(s);
        }
        return commandId;
    }

}