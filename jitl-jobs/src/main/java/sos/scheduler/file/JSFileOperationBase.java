package sos.scheduler.file;

import static com.sos.scheduler.messages.JSMessages.JFO_F_0100;
import static com.sos.scheduler.messages.JSMessages.JFO_F_0101;
import static com.sos.scheduler.messages.JSMessages.JFO_F_0102;
import static com.sos.scheduler.messages.JSMessages.JFO_F_0103;
import static com.sos.scheduler.messages.JSMessages.JFO_I_0105;
import static com.sos.scheduler.messages.JSMessages.JFO_I_0106;
import static com.sos.scheduler.messages.JSMessages.JSJ_E_0020;
import static com.sos.scheduler.messages.JSMessages.JSJ_E_0040;
import static com.sos.scheduler.messages.JSMessages.JSJ_E_0110;
import static com.sos.scheduler.messages.JSMessages.JSJ_F_0080;
import static com.sos.scheduler.messages.JSMessages.JSJ_F_0090;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.util.SOSFilelistFilter;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionFileName;
import com.sos.JSHelper.Options.SOSOptionFileSize;
import com.sos.JSHelper.Options.SOSOptionGracious;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionRegExp;
import com.sos.JSHelper.Options.SOSOptionTime;
import com.sos.JSHelper.io.Files.JSTextFile;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
public class JSFileOperationBase extends JSToolBox implements JSJobUtilities {

    protected static final String PARAMETER_WARNING_FILE_LIMIT = "warning_file_limit";
    protected static final String PARAMETER_FILE_AGE = "file_age";
    protected static final String PARAMETER_FILE_SPEC = "file_spec";
    protected static final String PARAMETER_FILE_SPECIFICATION = "file_specification";
    protected static final String PARAMETER_FILE_PATH = "file_path";
    protected static final String PARAMETER_MAX_FILE_SIZE = "max_file_size";
    protected static final String PARAMETER_MIN_FILE_SIZE = "min_file_size";
    protected static final String PARAMETER_MAX_FILE_AGE = "max_file_age";
    protected static final String PARAMETER_MIN_FILE_AGE = "min_file_age";
    protected static final String PARAMETER_TARGET_FILE = "target_file";
    protected static final String PARAMETER_FILE = "file";
    protected static final String PARAMETER_SOURCE_FILE = "source_file";
    protected static final String PARAMETER_ON_EMPTY_RESULT_SET = "on_empty_result_set";
    protected static final String PARAMETER_SKIP_LAST_FILES = "skip_last_files";
    protected static final String PARAMETER_SKIP_FIRST_FILES = "skip_first_files";
    protected static final String PARAMETER_OVERWRITE = "overwrite";
    protected static final String PARAMETER_COUNT_FILES = "count_files";
    protected static final String PARAMETER_REPLACEMENT = "replacement";
    protected static final String PARAMETER_REPLACING = "replacing";
    protected static final String PARAMETER_RAISE_ERROR_IF_RESULT_SET_IS = "Raise_Error_If_Result_Set_Is";
    protected static final String PARAMETER_EXPECTED_SIZE_OF_RESULT_SET = "Expected_Size_Of_Result_Set";
    protected Logger logger = LoggerFactory.getLogger(JSFileOperationBase.class);
    protected static final String PARAMETER_RESULT_LIST_FILE = "Result_List_File";
    protected static final String PARAMETER_RECURSIVE = "recursive";
    protected static final String PARAMETER_CREATE_DIR = "create_dir";
    protected static final String CLASS_NAME = "JobSchedulerFileOperationBase";
    protected static final String VALUE_YES = "yes";
    protected static final String PROPERTY_JAVA_IO_TMPDIR = "java.io.tmpdir";
    protected JSExistsFileOptions objOptions = null;
    protected String filePath = System.getProperty(PROPERTY_JAVA_IO_TMPDIR);
    protected long lngFileAge = 86400000;
    protected int warningFileLimit = 0;
    protected boolean flgOperationWasSuccessful = false;
    protected String name = null;
    protected String file = null;
    protected String fileSpec = ".*";
    protected String minFileAge = "0";
    protected String maxFileAge = "0";
    protected String minFileSize = "-1";
    protected String maxFileSize = "-1";
    protected int skipFirstFiles = 0;
    protected int skipLastFiles = 0;
    protected String strGracious = "false";
    protected final int isCaseInsensitive = Pattern.CASE_INSENSITIVE;
    protected String strOnEmptyResultSet = null;
    protected String strResultList2File = null;
    protected int intExpectedSizeOfResultSet = 0;
    protected String strRaiseErrorIfResultSetIs = null;
    protected Vector<File> lstResultList = null;
    protected boolean flgCreateOrder = false;
    protected boolean flgCreateOrders4AllFiles = false;
    protected String strOrderJobChainName = null;
    protected String strNextState = null;
    private JSJobUtilities objJSJobUtilities = this;
    private final String strFileSpecDefault = ".*";
    public static final String conParameterGRACIOUS = "gracious";
    public int intNoOfHitsInResultSet = 0;
    String source = null;
    String target = null;
    int flags = 0;
    String replacing = null;
    String replacement = null;
    boolean count_files = false;

    public JSFileOperationBase() {
        super("com_sos_scheduler_messages");
    }

    protected void initialize() {
        lstResultList = new Vector<File>();
        intExpectedSizeOfResultSet = getOptions().expected_size_of_result_set.value();
        strRaiseErrorIfResultSetIs = getOptions().raise_error_if_result_set_is.getValue();
        strResultList2File = getOptions().result_list_file.getValue();
        strOnEmptyResultSet = getOptions().on_empty_result_set.getValue();
        file = getOptions().file.getValue();
        fileSpec = strFileSpecDefault;
        fileSpec = getOptions().file_spec.getValue();
        minFileAge = getOptions().min_file_age.getValue();
        maxFileAge = getOptions().max_file_age.getValue();
        minFileSize = getOptions().min_file_size.getValue();
        maxFileSize = getOptions().min_file_size.getValue();
        strGracious = getOptions().gracious.getValue();
        skipFirstFiles = getOptions().skip_first_files.value();
        skipLastFiles = getOptions().skip_last_files.value();
        flags = 0;
        if (getOptions().gracious.value()) {
            flags |= SOSOptionGracious.GRACIOUS;
        }
        String strM = JSJ_E_0110.get(PARAMETER_REPLACEMENT, PARAMETER_REPLACING);
        if (isNotNull(replacing) && isNull(replacement)) {
            throw new JobSchedulerException(strM);
        }
        if (isNull(replacing) && isNotNull(replacement)) {
            throw new JobSchedulerException(strM);
        }
    }

    public JSExistsFileOptions getOptions() {
        if (objOptions == null) {
            objOptions = new JSExistsFileOptions();
        }
        return objOptions;
    }

    public JSExistsFileOptions getOptions(final JSExistsFileOptions pobjOptions) {
        objOptions = pobjOptions;
        return objOptions;
    }

    public Vector<File> getResultList() {
        return lstResultList;
    }

    public boolean createResultListParam(final boolean pflgResult) {
        String strT = "";
        intNoOfHitsInResultSet = lstResultList.size();
        if (isNotNull(lstResultList) && !lstResultList.isEmpty()) {
            intNoOfHitsInResultSet = lstResultList.size();
            for (File objFile : lstResultList) {
                strT += objFile.getAbsolutePath() + ";";
            }
        }
        if (isNotEmpty(strResultList2File) && isNotEmpty(strT)) {
            JSTextFile objResultListFile = new JSTextFile(strResultList2File);
            try {
                if (objResultListFile.canWrite()) {
                    objResultListFile.write(strT);
                    objResultListFile.close();
                } else {
                    throw new JobSchedulerException(JSJ_F_0090.get(PARAMETER_RESULT_LIST_FILE, strResultList2File));
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                JSJ_F_0080.toLog(strResultList2File, PARAMETER_RESULT_LIST_FILE);
            }
        }
        if (isNotEmpty(strRaiseErrorIfResultSetIs)) {
            boolean flgR = getOptions().raise_error_if_result_set_is.compareIntValues(intNoOfHitsInResultSet, intExpectedSizeOfResultSet);
            if (flgR) {
                logger.info(JSJ_E_0040.get(intNoOfHitsInResultSet, strRaiseErrorIfResultSetIs, intExpectedSizeOfResultSet));
                return false;
            }
        }
        return pflgResult;
    }

    public void checkMandatoryFile() {
        if (isNull(file)) {
            throw new JobSchedulerException(JSJ_E_0020.get(PARAMETER_FILE));
        }
    }

    public void checkMandatorySource() {
        if (isNull(source)) {
            throw new JobSchedulerException(JSJ_E_0020.get(PARAMETER_SOURCE_FILE));
        }
    }

    public void checkMandatoryTarget() {
        if (isNull(source)) {
            throw new JobSchedulerException(JSJ_E_0020.get(PARAMETER_TARGET_FILE));
        }
    }

    @Override
    public String replaceSchedulerVars(final String pstrString2Modify) {
        logger.debug("replaceSchedulerVars as Dummy-call executed. No Instance of JobUtilites specified.");
        return pstrString2Modify;
    }

    @Override
    public void setJSParam(final String pstrKey, final String pstrValue) {
        //
    }

    @Override
    public void setJSParam(final String pstrKey, final StringBuffer pstrValue) {
        //
    }

    @Override
    public void setJSJobUtilites(final JSJobUtilities pobjJSJobUtilities) {
        if (pobjJSJobUtilities == null) {
            objJSJobUtilities = this;
        } else {
            objJSJobUtilities = pobjJSJobUtilities;
        }
        logger.debug("objJSJobUtilities = " + objJSJobUtilities.getClass().getName());
    }

    public boolean existsFile(final SOSOptionFileName objFile, final SOSOptionRegExp fileSpec1, final SOSOptionTime minFileAge1,
            final SOSOptionTime maxFileAge1, final SOSOptionFileSize minFileSize1, final SOSOptionFileSize maxFileSize1,
            final SOSOptionInteger skipFirstFiles1, final SOSOptionInteger skipLastFiles1, final int minNumOfFiles, final int maxNumOfFiles)
            throws IOException, Exception {
        long minAge = 0;
        long maxAge = 0;
        long minSize = -1;
        long maxSize = -1;
        minAge = minFileAge1.calculateFileAge();
        maxAge = maxFileAge1.calculateFileAge();
        minSize = minFileSize1.getFileSize();
        maxSize = maxFileSize1.getFileSize();
        if (skipFirstFiles1.value() < 0) {
            throw new JobSchedulerException(JFO_F_0100.get(skipFirstFiles1.value(), "skipFirstFiles"));
        }
        if (skipLastFiles1.value() < 0) {
            throw new JobSchedulerException(JFO_F_0100.get(skipLastFiles1.value(), "skipLastFiles"));
        }
        if (skipFirstFiles1.value() > 0 && skipLastFiles1.value() > 0) {
            JFO_F_0101.toLog();
        }
        if ((skipFirstFiles1.value() > 0 || skipLastFiles1.value() > 0) && minAge == 0 && maxAge == 0 && minSize == -1 && maxSize == -1) {
            JFO_F_0103.toLog();
        }
        String filename = objFile.substituteAllDate();
        Matcher m = Pattern.compile("\\[[^]]*\\]").matcher(filename);
        if (m.find()) {
            JFO_F_0102.toLog(m.group());
        }
        File fleFile = new File(filename);
        if (!fleFile.exists()) {
            logger.debug(JFO_I_0105.get(fleFile.getCanonicalPath()));
            return false;
        } else {
            if (!fleFile.isDirectory()) {
                JFO_I_0106.toLog(fleFile.getCanonicalPath());
                long currentTime = System.currentTimeMillis();
                if (minAge > 0) {
                    long interval = currentTime - fleFile.lastModified();
                    if (interval < 0) {
                        throw new Exception("Cannot filter by file age. File [" + fleFile.getCanonicalPath() + "] was modified in the future.");
                    }
                    if (interval < minAge) {
                        log("checking file age " + fleFile.lastModified() + ": minimum age required is " + minAge);
                        return false;
                    }
                }
                if (maxAge > 0) {
                    long interval = currentTime - fleFile.lastModified();
                    if (interval < 0) {
                        throw new JobSchedulerException("Cannot filter by file age. File [" + fleFile.getCanonicalPath()
                                + "] was modified in the future.");
                    }
                    if (interval > maxAge) {
                        log("checking file age " + fleFile.lastModified() + ": maximum age required is " + maxAge);
                        return false;
                    }
                }
                if (minSize > -1 && minSize > fleFile.length()) {
                    log("checking file size " + fleFile.length() + ": minimum size required is " + minFileSize1);
                    return false;
                }
                if (maxSize > -1 && maxSize < fleFile.length()) {
                    log("checking file size " + fleFile.length() + ": maximum size required is " + maxFileSize1);
                    return false;
                }
                if (skipFirstFiles1.value() > 0 || skipLastFiles1.value() > 0) {
                    log("file skipped");
                    return false;
                }
                return true;
            } else {
                if (fileSpec1.IsEmpty()) {
                    log("checking file " + fleFile.getCanonicalPath() + ": directory exists");
                    return true;
                }
                Vector<File> fileList =
                        getFilelist(fleFile.getPath(), fileSpec1, false, minAge, maxAge, minSize, maxSize, skipFirstFiles1.value(),
                                skipLastFiles1.value());
                if (fileList.isEmpty()) {
                    log("checking file " + fleFile.getCanonicalPath() + ": directory contains no files matching " + fileSpec1);
                    return false;
                } else {
                    log("checking file " + fleFile.getCanonicalPath() + ": directory contains " + fileList.size() + " file(s) matching " + fileSpec1);
                    for (int i = 0; i < fileList.size(); i++) {
                        File checkFile = fileList.get(i);
                        log("found " + checkFile.getCanonicalPath());
                    }
                    if (minNumOfFiles >= 0 && fileList.size() < minNumOfFiles) {
                        log("found " + fileList.size() + " files, minimum expected " + minNumOfFiles + " files");
                        return false;
                    }
                    if (maxNumOfFiles >= 0 && fileList.size() > maxNumOfFiles) {
                        log("found " + fileList.size() + " files, maximum expected " + maxNumOfFiles + " files");
                        return false;
                    }
                    lstResultList.addAll(fileList);
                    return true;
                }
            }
        }
    }

    private Vector<File> getFilelist(final String folder, final SOSOptionRegExp regexp, final boolean withSubFolder, final long minFileAge1,
            final long maxFileAge1, final long minFileSize1, final long maxFileSize1, final int skipFirstFiles1, final int skipLastFiles1)
            throws Exception {
        Vector<File> filelist = new Vector<File>();
        Vector<File> temp = new Vector<File>();
        File objFile = null;
        File[] subDir = null;
        objFile = new File(folder);
        subDir = objFile.listFiles();
        temp = this.getFilelist(folder, regexp);
        temp = filelistFilterAge(temp, minFileAge1, maxFileAge1);
        temp = filelistFilterSize(temp, minFileSize1, maxFileSize1);
        if ((minFileSize1 != -1 || minFileSize1 != -1) && minFileAge1 == 0 && maxFileAge1 == 0) {
            temp = filelistSkipFiles(temp, skipFirstFiles1, skipLastFiles1, "sort_size");
        } else if (minFileAge1 != 0 || maxFileAge1 != 0) {
            temp = filelistSkipFiles(temp, skipFirstFiles1, skipLastFiles1, "sort_age");
        }
        filelist.addAll(temp);
        if (withSubFolder) {
            for (File element : subDir) {
                if (element.isDirectory()) {
                    filelist.addAll(getFilelist(element.getPath(), regexp, true, minFileAge1, maxFileAge1, minFileSize1, maxFileSize1,
                            skipFirstFiles1, skipLastFiles1));
                }
            }
        }
        return filelist;
    }

    public Vector<File> getFilelist(final String folder, final SOSOptionRegExp regexp) throws Exception {
        Vector<File> filelist = new Vector<File>();
        if (folder == null || folder.isEmpty()) {
            throw new JobSchedulerException("a null value for param 'directory' is not allowed");
        }
        File f = new File(folder);
        if (!f.exists()) {
            String strM = JFO_I_0105.get(folder);
            logger.error(strM);
            throw new JobSchedulerException(strM);
        }
        filelist = new Vector<File>();
        File[] files = f.listFiles(new SOSFilelistFilter(regexp.getValue(), regexp.getRegExpFlags()));
        for (File file2 : files) {
            if (file2.isFile()) {
                filelist.add(file2);
            }
        }
        return filelist;
    }

    private Vector<File> filelistFilterAge(Vector<File> filelist, final long minAge, final long maxAge) throws Exception {
        long currentTime = System.currentTimeMillis();
        if (minAge != 0) {
            File file1;
            Vector<File> newlist = new Vector<File>();
            for (int i = 0; i < filelist.size(); i++) {
                file1 = filelist.get(i);
                long interval = currentTime - file1.lastModified();
                if (interval < 0) {
                    throw new JobSchedulerException("Cannot filter by file age. File [" + file1.getCanonicalPath() + "] was modified in the future.");
                }
                if (interval >= minAge) {
                    newlist.add(file1);
                }
            }
            filelist = newlist;
        }
        if (maxAge != 0) {
            File file1;
            Vector<File> newlist = new Vector<File>();
            for (int i = 0; i < filelist.size(); i++) {
                file1 = filelist.get(i);
                long interval = currentTime - file1.lastModified();
                if (interval < 0) {
                    throw new JobSchedulerException("Cannot filter by file age. File [" + file1.getCanonicalPath() + "] was modified in the future.");
                }
                if (interval <= maxAge) {
                    newlist.add(file1);
                }
            }
            filelist = newlist;
        }
        return filelist;
    }

    private Vector<File> filelistFilterSize(Vector<File> filelist, final long minSize, final long maxSize) throws Exception {
        if (minSize > -1) {
            File file1;
            Vector<File> newlist = new Vector<File>();
            for (int i = 0; i < filelist.size(); i++) {
                file1 = filelist.get(i);
                if (file1.length() >= minSize) {
                    newlist.add(file1);
                }
            }
            filelist = newlist;
        }
        if (maxSize > -1) {
            File file1;
            Vector<File> newlist = new Vector<File>();
            for (int i = 0; i < filelist.size(); i++) {
                file1 = filelist.get(i);
                if (file1.length() <= maxSize) {
                    newlist.add(file1);
                }
            }
            filelist = newlist;
        }
        return filelist;
    }

    private Vector<File> filelistSkipFiles(Vector<File> filelist, final int skipFirstFiles1, final int skipLastFiles1, final String sorting)
            throws Exception {
        File[] oArr = filelist.toArray(new File[filelist.size()]);

        class SizeComparator implements Comparator<File> {

            @Override
            public int compare(final File o1, final File o2) {
                int ret = 0;
                long val1 = o1.length();
                long val2 = o2.length();
                if (val1 < val2) {
                    ret = -1;
                } else if (val1 == val2) {
                    ret = 0;
                } else if (val1 > val2) {
                    ret = 1;
                }
                return ret;
            }
        }

        class AgeComparator implements Comparator<File> {

            @Override
            public int compare(final File o1, final File o2) {
                int ret = 0;
                long val1 = o1.lastModified();
                long val2 = o2.lastModified();
                if (val1 > val2) {
                    ret = -1;
                } else if (val1 == val2) {
                    ret = 0;
                } else if (val1 < val2) {
                    ret = 1;
                }
                return ret;
            }
        }
        if ("sort_size".equals(sorting)) {
            Arrays.sort(oArr, new SizeComparator());
        } else if ("sort_age".equals(sorting)) {
            Arrays.sort(oArr, new AgeComparator());
        }
        filelist = new Vector<File>();
        for (int i = 0 + skipFirstFiles1; i < oArr.length - skipLastFiles1; i++) {
            filelist.add(oArr[i]);
        }
        return filelist;
    }

    private void log(final String msg) {
        try {
            logger.info(msg);
        } catch (Exception e) {
        }
    }

    @Override
    public void setStateText(final String pstrStateText) {
        //
    }

    @Override
    public void setCC(final int pintCC) {
        //
    }

    @Override
    public void setNextNodeState(final String pstrNodeName) {
        //
    }

}