package sos.scheduler.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.JSHelper.io.Files.JSTextFile;

public class JSExistsFileJUnitTest extends JSToolBox {

    protected JSExistsFileOptions objOptions = null;
    private static final int conNumberOfFiles2Skip = 2;
    private static final int conNumberOfTestFiles = 10;
    private static final String conTestRegularExpression1 = "^.*\\.kb$";
    private static final String conTestBaseFolderName = "j:/e/java/development-testdata/com.sos.scheduler/";
    private JSExistsFile objE = null;

    public JSExistsFileJUnitTest() {
        //
    }

    @Before
    public void setUp() throws Exception {
        objE = new JSExistsFile();
        objOptions = objE.getOptions();
        JSListenerClass.bolLogDebugInformation = true;
        JSListenerClass.intMaxDebugLevel = 9;
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testExecute() throws Exception {
        boolean flgResult = objE.Execute();
        assertTrue("Directory must exists", flgResult);
        objOptions.file.setValue("abcdef");
        flgResult = objE.Execute();
        assertFalse("Directory must exists", flgResult);
        objOptions.file.setValue(conTestBaseFolderName);
        flgResult = objE.Execute();
        assertTrue("Directory must exists", flgResult);
    }

    private void createTestFiles() throws Exception {
        String strT = "Testfile Testfile Testfile";
        long lngTimeLag = 3600000;
        for (int i = 0; i < conNumberOfTestFiles; i++) {
            JSTextFile objF = new JSTextFile(conTestBaseFolderName + "testfile" + i + ".kb");
            if (objF.exists()) {
                break;
            }
            objF.write(strT);
            strT = strT + strT;
            objF.deleteOnExit();
            Date lastModified = new Date(objF.lastModified());
            objF.close();
            boolean blnSuccess = objF.setLastModified(lastModified.getTime() - lngTimeLag);
            lngTimeLag += 3600000;
        }
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testFileSpec() throws Exception {
        createTestFiles();
        objOptions.file.setValue(conTestBaseFolderName);
        objOptions.file_spec.setValue(conTestRegularExpression1);
        boolean flgResult = objE.Execute();
        assertTrue("Dateien wurden gefunden", flgResult);
        assertEquals("i expect exactly " + conNumberOfTestFiles + " files", conNumberOfTestFiles, objE.getResultList().size());
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testSkipFirstFile() throws Exception {
        createTestFiles();
        objOptions.file.setValue(conTestBaseFolderName);
        objOptions.file_spec.setValue(conTestRegularExpression1);
        objOptions.skip_first_files.value(conNumberOfFiles2Skip);
        objOptions.min_file_size.setValue("5");
        boolean flgResult = objE.Execute();
        assertTrue("Dateien wurden gefunden", flgResult);
        assertEquals("i expect exactly " + conNumberOfTestFiles + " files", conNumberOfTestFiles - conNumberOfFiles2Skip, objE.getResultList().size());
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testSkipLastFile() throws Exception {
        createTestFiles();
        objOptions.file.setValue(conTestBaseFolderName);
        objOptions.file_spec.setValue(conTestRegularExpression1);
        objOptions.skip_last_files.value(conNumberOfFiles2Skip);
        objOptions.min_file_size.setValue("5");
        boolean flgResult = objE.Execute();
        assertTrue("Dateien wurden gefunden", flgResult);
        assertEquals("i expect exactly " + conNumberOfTestFiles + " files", conNumberOfTestFiles - conNumberOfFiles2Skip, objE.getResultList().size());
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testMinFileSize() throws Exception {
        createTestFiles();
        objOptions.file.setValue(conTestBaseFolderName);
        objOptions.file_spec.setValue(conTestRegularExpression1);
        objOptions.min_file_size.setValue("4KB");
        boolean flgResult = objE.Execute();
        assertTrue("Dateien wurden gefunden", flgResult);
        assertEquals("i expect exactly " + conNumberOfTestFiles + " files", 2, objE.getResultList().size());
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testMaxFileSize() throws Exception {
        createTestFiles();
        objOptions.file.setValue(conTestBaseFolderName);
        objOptions.file_spec.setValue(conTestRegularExpression1);
        objOptions.max_file_size.setValue("4KB");
        boolean flgResult = objE.Execute();
        assertTrue("Dateien wurden gefunden", flgResult);
        assertEquals("i expect exactly " + conNumberOfTestFiles + " files", 8, objE.getResultList().size());
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testMinMaxFileSize() throws Exception {
        createTestFiles();
        objOptions.file.setValue(conTestBaseFolderName);
        objOptions.file_spec.setValue(conTestRegularExpression1);
        objOptions.min_file_size.setValue("2KB");
        objOptions.max_file_size.setValue("4KB");
        boolean flgResult = objE.Execute();
        assertTrue("Dateien wurden gefunden", flgResult);
        assertEquals("i expect exactly " + conNumberOfTestFiles + " files", 1, objE.getResultList().size());
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testMinFileAge() throws Exception {
        createTestFiles();
        objOptions.file.setValue(conTestBaseFolderName);
        objOptions.file_spec.setValue(conTestRegularExpression1);
        objOptions.min_file_age.setValue("02:00:00");
        boolean flgResult = objE.Execute();
        assertTrue("Dateien wurden gefunden", flgResult);
        assertEquals("i expect exactly " + conNumberOfTestFiles + " files", 8, objE.getResultList().size());
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testMaxFileAge() throws Exception {
        createTestFiles();
        objOptions.file.setValue(conTestBaseFolderName);
        objOptions.file_spec.setValue(conTestRegularExpression1);
        objOptions.max_file_age.setValue("03:00:00");
        boolean flgResult = objE.Execute();
        assertTrue("Dateien wurden gefunden", flgResult);
        assertEquals("i expect exactly " + conNumberOfTestFiles + " files", 3, objE.getResultList().size());
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testMaxFileAge2() throws Exception {
        createTestFiles();
        objOptions.file.setValue(conTestBaseFolderName);
        objOptions.file_spec.setValue(conTestRegularExpression1);
        objOptions.max_file_age.setValue("60");
        long intF = objOptions.max_file_age.calculateFileAge();
        assertEquals("long milliseconds ", 60000L, intF);
        boolean flgResult = objE.Execute();
        assertTrue("Dateien wurden gefunden", flgResult);
        assertEquals("i expect exactly " + conNumberOfTestFiles + " files", 3, objE.getResultList().size());
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testMinMaxFileAge() throws Exception {
        createTestFiles();
        objOptions.file.setValue(conTestBaseFolderName);
        objOptions.file_spec.setValue(conTestRegularExpression1);
        objOptions.min_file_age.setValue("02:00:00");
        objOptions.max_file_age.setValue("05:00:00");
        boolean flgResult = objE.Execute();
        assertTrue("Dateien wurden gefunden", flgResult);
        assertEquals("i expect exactly " + conNumberOfTestFiles + " files", 3, objE.getResultList().size());
    }

}