package com.sos.jitl.textprocessor;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;

public class JobSchedulerTextProcessorJUnitTest extends JSToolBox {

    protected JobSchedulerTextProcessorOptions objOptions = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerTextProcessorJUnitTest.class);
    private JobSchedulerTextProcessor objE = null;
    private File inputFile;

    public JobSchedulerTextProcessorJUnitTest() {
        //
    }

    @Before
    public void setUp() throws Exception {
        objE = new JobSchedulerTextProcessor();
        objE.registerMessageListener(this);
        objOptions = objE.getOptions();
        objOptions.registerMessageListener(this);
        JSListenerClass.bolLogDebugInformation = true;
        JSListenerClass.intMaxDebugLevel = 9;
        inputFile = File.createTempFile("textprocessor_test", null);
        FileOutputStream fos = new FileOutputStream(inputFile);
        String content1 = "This is the house from the nicolaus\n";
        String content2 = "This is the second line\n";
        String content3 = "This is the last line\n";
        if (!inputFile.exists()) {
            inputFile.createNewFile();
        }
        fos.write(content1.getBytes());
        fos.write(content2.getBytes());
        fos.write(content3.getBytes());
        fos.flush();
        fos.close();
    }

    @After
    public void tearDown() throws Exception {
        inputFile.delete();
    }

    @Test
    public void testExecuteCount() throws Exception {
        String command = " count   line";
        JobSchedulerTextProcessorExecuter textProcessor = new JobSchedulerTextProcessorExecuter(inputFile, command);
        String result = textProcessor.execute();
        LOGGER.info(command + " ->" + result);
        assertEquals("testExecuteCount", "2", result);
    }

    @Test
    public void testExecuteMissingParameter() throws Exception {
        String command = "count";
        String s = "";
        JobSchedulerTextProcessorExecuter textProcessor = new JobSchedulerTextProcessorExecuter(inputFile, command);
        try {
            String result = textProcessor.execute();
        } catch (Exception e) {
            s = e.getMessage();
        }
        assertEquals("testExecuteMissingParameter", s, "Param missing in: count");
    }

    @Test
    public void testExecuteUnknownCommand() throws Exception {
        String command = "countxxx a";
        String s = "";
        JobSchedulerTextProcessorExecuter textProcessor = new JobSchedulerTextProcessorExecuter(inputFile, command);
        try {
            String result = textProcessor.execute();
        } catch (Exception e) {
            s = e.getMessage();
        }
        assertEquals("testExecuteUnknownCommand", s, "Unknown command: (not in count, add, read) countxxx");
    }

    @Test
    public void testExecuteInsertLast() throws Exception {
        String command1 = "insert  last letzte";
        String command2 = "count letzte";
        JobSchedulerTextProcessorExecuter textProcessor = new JobSchedulerTextProcessorExecuter(inputFile, command2);
        String result = textProcessor.execute();
        assertEquals("testExecuteInsertLast", "0", result);
        result = textProcessor.execute(command1);
        result = textProcessor.execute(command2);
        assertEquals("testExecuteInsertLast", "1", result);
    }

    @Test
    public void testExecuteInsert() throws Exception {
        String command1 = "insert  2 zweite";
        String command2 = "count zweite";
        JobSchedulerTextProcessorExecuter textProcessor = new JobSchedulerTextProcessorExecuter(inputFile, command2);
        String result = textProcessor.execute();
        assertEquals("testExecuteInsert", "0", result);
        result = textProcessor.execute(command1);
        result = textProcessor.execute(command2);
        assertEquals("testExecuteInsert", "1", result);
    }

    @Test
    public void testExecuteInsertFirst() throws Exception {
        String command1 = "insert  first erste";
        String command2 = "count erste";
        JobSchedulerTextProcessorExecuter textProcessor = new JobSchedulerTextProcessorExecuter(inputFile, command2);
        String result = textProcessor.execute();
        assertEquals("testExecuteInsertFirst", "0", result);
        result = textProcessor.execute(command1);
        result = textProcessor.execute(command2);
        assertEquals("testExecuteInsertFirst", "1", result);
    }

    @Test
    public void testExecuteInsertReadLast() throws Exception {
        String command1 = "insert  last letzte";
        String command2 = "read last";
        JobSchedulerTextProcessorExecuter textProcessor = new JobSchedulerTextProcessorExecuter(inputFile, command2);
        String result = textProcessor.execute();
        assertEquals("testExecuteInsertReadLast", "This is the last line", result);
        result = textProcessor.execute(command1);
        result = textProcessor.execute(command2);
        assertEquals("testExecuteInsertReadLast", "letzte", result);
    }

    @Test
    public void testExecuteInsertReadFirst() throws Exception {
        String command1 = "insert  first erste";
        String command2 = "read first";
        JobSchedulerTextProcessorExecuter textProcessor = new JobSchedulerTextProcessorExecuter(inputFile, command2);
        String result = textProcessor.execute();
        assertEquals("testExecuteInsertReadFirst", "This is the house from the nicolaus", result);
        result = textProcessor.execute(command1);
        result = textProcessor.execute(command2);
        assertEquals("testExecuteInsertReadFirst", "erste", result);
    }

    @Test
    public void testExecuteInsertReadLine() throws Exception {
        String command1 = "insert  2 zweite";
        String command2 = "read 2";
        JobSchedulerTextProcessorExecuter textProcessor = new JobSchedulerTextProcessorExecuter(inputFile, command2);
        String result = textProcessor.execute();
        assertEquals("testExecuteInsertReadLine", "This is the second line", result);
        result = textProcessor.execute(command1);
        result = textProcessor.execute(command2);
        assertEquals("testExecuteInsertReadLine", "zweite", result);
    }

    @Test
    public void testExecuteAddLine() throws Exception {
        String command1 = "add letzte";
        String command2 = "read last";
        JobSchedulerTextProcessorExecuter textProcessor = new JobSchedulerTextProcessorExecuter(inputFile, command2);
        String result = textProcessor.execute();
        assertEquals("testExecuteAddLine", "This is the last line", result);
        result = textProcessor.execute(command1);
        result = textProcessor.execute(command2);
        assertEquals("testExecuteAddLine", "letzte", result);
    }

}