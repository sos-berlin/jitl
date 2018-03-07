package sos.scheduler.file;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionFileAge;
import com.sos.JSHelper.Options.SOSOptionTime;
import com.sos.JSHelper.io.Files.JSTextFile;
import com.sos.JSHelper.io.SOSFileSystemOperations;
import com.sos.i18n.annotation.I18NResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Job_chain;
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.util.SOSSchedulerLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import static com.sos.scheduler.messages.JSMessages.*;

@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
public class JobSchedulerFileOperationBase extends JobSchedulerJobAdapter {

	public static final String ORDER_PARAMETER_SCHEDULER_SOS_FILE_OPERATIONS_RESULT_SET = "scheduler_SOSFileOperations_ResultSet";
	public static final String ORDEDR_PARAMETER_SCHEDULER_SOS_FILE_OPERATIONS_RESULT_SET_SIZE = "scheduler_SOSFileOperations_ResultSetSize";
	public static final String ORDER_PARAMETER_SCHEDULER_SOS_FILE_OPERATIONS_FILE_COUNT = "scheduler_SOSFileOperations_file_count";
	public static final String PARAMTER_GRACIOUS = "gracious";
	protected static final String PROPERTY_JAVA_IO_TMPDIR = "java.io.tmpdir";
	protected static final String PARAMETER_CHECK_STEADY_STATE_OF_FILE = "check_steady_state_of_files";
	protected static final String PARAMETER_CHECK_STEADY_STATE_INTERVAL = "check_steady_state_interval";
	protected static final String PARAMETER_STEADY_STATE_COUNT = "steady_state_count";
	protected static final String PARAMETER_WARNING_FILE_LIMIT = "warning_file_limit";
	protected static final String PARAMETER_FILE_AGE = "file_age";
	protected static final String PARAMETER_FILE_SPEC = "file_spec";
	protected static final String PARAMETER_FILE_REGEX = "file_regex";
	protected static final String PARAMETER_FILE_SPECIFICATION = "file_specification";
	protected static final String PARAMETER_FILE_PATH = "file_path";
	protected static final String PARAMETERMAX_FILE_SIZE = "max_file_size";
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
	protected static final String ALL = "all";
	protected static final String TRUE = "true";
	protected static final String PARAMETER_REPLACEMENT = "replacement";
	protected static final String PARAMETER_REPLACING = "replacing";
	protected static final String PARAMETER_RAISE_ERROR_IF_RESULT_SET_IS = "Raise_Error_If_Result_Set_Is";
	protected static final String PARAMETER_EXPECTED_SIZE_OF_RESULT_SET = "Expected_Size_Of_Result_Set";
	protected static final String PARAMETER_RESULT_LIST_FILE = "Result_List_File";
	protected static final String PARAMETER_RECURSIVE = "recursive";
	protected static final String PARAMETER_CREATE_DIR = "create_dir";
	protected static final String PARAMETER_CREATE_FILE = "create_file";
	protected static final String PARAMETER_CREATE_FILES = "create_files";
	protected static final String PARAMETER_SORT_CRITERIA = "sort_criteria";
	protected static final String PARAMETER_SORT_ORDER = "sort_order";
	protected static final String PARAMETER_REMOVE_DIR = "remove_dir";
	protected static final String CLASS_NAME = "JobSchedulerFileOperationBase";
	protected static final String YES = "yes";
	private static final String ORDER_PARAMETER_SCHEDULER_FILE_PATH = "scheduler_file_path";
	private static final String ORDER_PARAMETER_SCHEDULER_FILE_PARENT = "scheduler_file_parent";
	private static final String ORDER_PARAMETER_SCHEDULER_FILE_NAME = "scheduler_file_name";
	private static final String PARAMETER_CREATE_ORDERS_FOR_ALL_FILES = "create_orders_for_all_files";
	private static final String PARAMETER_NEXT_STATE = "next_state";
	private static final String PARAMETER_ORDER_JOBCHAIN_NAME = "order_jobchain_name";
	private static final String PARAMETER_CREATE_ORDER = "create_order";
	private static final String PARAMETER_MERGE_ORDER_PARAMETER = "merge_order_parameter";
	private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerFileOperationBase.class);

	protected final int isCaseInsensitive = Pattern.CASE_INSENSITIVE;
	protected String filePath = System.getProperty(PROPERTY_JAVA_IO_TMPDIR);
	protected long lngFileAge = 86400000;
	protected int warningFileLimit = 0;
	protected SOSSchedulerLogger objSOSLogger = null;
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
	protected String onEmptyResultSet = null;
	protected String resultList2File = null;
	protected int expectedSizeOfResultSet = 0;
	protected String raiseErrorIfResultSetIs = null;
	protected List<File> lstResultList = new Vector<File>();
	protected boolean flgCreateOrder = false;
	protected boolean flgMergeOrderParameter = false;
	protected boolean flgCreateOrders4AllFiles = false;
	protected String orderJobChainName = null;
	protected String nextState = null;
	protected boolean flgCheckSteadyStateOfFiles = false;
	protected long steadyCount = 30;
	protected long checkSteadyStateInterval = 1000;
	protected boolean flgUseNIOLock = false;
	protected SOSFileSystemOperations sosFileOperations = null;
	private final String fileSpecDefault = ".*";
	private final String fileSizeDefault = "-1";
	private final String JSJ_F_0110 = "JSJ_F_0110";
	private SOSOptionFileAge sosOptionFileAge = null;
	private SOSOptionTime sosOptionTime = null;
	public int noOfHitsInResultSet = 0;
	HashMap<String, String> params = null;
	String source = null;
	String target = null;
	int flags = 0;
	String replacing = null;
	String replacement = null;
	boolean countFiles = false;
	protected String sortCriteria;
	protected String sortOrder;

	public JobSchedulerFileOperationBase() {
		super();
	}

	@Override
	public boolean spooler_init() {
		final String methodName = "JobSchedulerFileOperationBase::spooler_init";
		boolean flgReturn = super.spooler_init();
		try {
			try {
				objSOSLogger = new SOSSchedulerLogger(spooler_log);
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new JobSchedulerException(JSJ_F_0015.params("SOSSchedulerLogger ", e.getMessage()), e);
			}
			return flgReturn;
		} catch (Exception e) {
			try {
				if (isNotNull(objSOSLogger)) {
					objSOSLogger.error(JSJ_F_0016.params(methodName, e.getMessage()));
				}
			} catch (Exception x) {
			}
			return false;
		}
	}

	private void resetVariables() {
		flgOperationWasSuccessful = false;
		name = null;
		file = null;
		fileSpec = fileSpecDefault;
		minFileAge = "0";
		maxFileAge = "0";
		minFileSize = fileSizeDefault;
		maxFileSize = fileSizeDefault;
		skipFirstFiles = 0;
		skipLastFiles = 0;
		strGracious = "false";
		source = null;
		target = null;
		flags = 0;
		replacing = null;
		replacement = EMPTY_STRING;
		countFiles = false;
		noOfHitsInResultSet = 0;
		onEmptyResultSet = null;
		resultList2File = null;
		expectedSizeOfResultSet = 0;
		raiseErrorIfResultSetIs = null;
		lstResultList = null;
		flgCreateOrder = false;
		flgMergeOrderParameter = false;
		flgCreateOrders4AllFiles = false;
		orderJobChainName = null;
		sortOrder = null;
		sortCriteria = null;
		nextState = null;
		sosOptionFileAge = new SOSOptionFileAge(null, "file_age", "file_age", "0", "0", false);
		sosOptionTime = new SOSOptionTime(null, PARAMETER_CHECK_STEADY_STATE_INTERVAL,
				PARAMETER_CHECK_STEADY_STATE_INTERVAL, "1", "1", false);
		flgCheckSteadyStateOfFiles = false;
		steadyCount = 30;
		checkSteadyStateInterval = 1000;
	}

	protected boolean getParamBoolean(final String pstrParamName, final boolean pflgDefaultValue) {
		String strReturnValue = getParamValue(pstrParamName);
		boolean flgReturnValue = pflgDefaultValue;
		if (isNotNull(strReturnValue)) {
			try {
				flgReturnValue = toBoolean(strReturnValue);
			} catch (Exception e) {
			}
		}
		return flgReturnValue;
	}

	protected long getParamLong(final String pstrParamName, final long plngDefaultValue) {
		long lngReturnValue = getParamInteger(pstrParamName);
		if (lngReturnValue == 0) {
			lngReturnValue = plngDefaultValue;
		}
		return lngReturnValue;
	}

	protected int getParamInteger(final String pstrParamName, final int pintDefaultValue) {
		int intReturnValue = getParamInteger(pstrParamName);
		if (intReturnValue == 0) {
			intReturnValue = pintDefaultValue;
		}
		return intReturnValue;
	}

	protected int getParamInteger(final String pstrParamName) {
		String strT = getParamValue(pstrParamName);
		int intRetVal = 0;
		try {
			if (isNotEmpty(strT)) {
				intRetVal = Integer.parseInt(strT);
			}
		} catch (Exception ex) {
			throw new JobSchedulerException(JSJ_E_0130.get(pstrParamName, ex.getMessage()), ex);
		}
		return intRetVal;
	}

	protected String getParamValue(final String[] pstrKeys, final String pstrDefaultValue) {
		String strT = pstrDefaultValue;
		for (String strKey : pstrKeys) {
			String strK = getParamValue(strKey);
			if (isNotEmpty(strK)) {
				strT = strK;
				break;
			}
		}
		return strT;
	}

	protected String getParamValue(final String pstrParamName, final String pstrDefaultValue) {
		String strT = getParamValue(pstrParamName);
		if (isNull(strT)) {
			strT = pstrDefaultValue;
		}
		return strT;
	}

	protected String getParamValue(final String pstrParamName) {
		name = pstrParamName;
		String strT = params.get(pstrParamName);
		if (isNull(strT)) {
			strT = params.get(pstrParamName.replaceAll("_", EMPTY_STRING).toLowerCase());
		}
		if (isNotNull(strT)) {
			strT = strT.trim();
			if (!strT.isEmpty()) {
				if (pstrParamName.contains("password")) {
					LOGGER.info(JSJ_I_0040.params(pstrParamName, "*****"));
				} else {
					LOGGER.info(JSJ_I_0040.params(pstrParamName, strT));
				}
			}
		}
		return strT;
	}

	public boolean initialize() {
		try {
			super.spooler_process();
			params = null;
			params = getSchedulerParameterAsProperties();
			getParametersFromHashMap();
		} catch (Exception e) {
			LOGGER.error("Error during initializing", e);
			throw new JobSchedulerException(JSJ_E_0042.get(e), e);
		}
		return true;
	}

	protected void getParametersFromHashMap() throws Exception {
		resetVariables();
		flgCreateOrders4AllFiles = getParamBoolean(PARAMETER_CREATE_ORDERS_FOR_ALL_FILES, false);
		flgCreateOrder = getParamBoolean(PARAMETER_CREATE_ORDER, false) | flgCreateOrders4AllFiles;
		flgMergeOrderParameter = getParamBoolean(PARAMETER_MERGE_ORDER_PARAMETER, false);
		if (flgCreateOrder) {
			orderJobChainName = getParamValue(PARAMETER_ORDER_JOBCHAIN_NAME);
			if (isNull(orderJobChainName)) {
				throw new JobSchedulerException(JSJ_E_0020.params(PARAMETER_ORDER_JOBCHAIN_NAME));
			}
			if (!spooler.job_chain_exists(orderJobChainName)) {
				throw new JobSchedulerException(JSJ_E_0041.params(orderJobChainName));
			}
			nextState = getParamValue(PARAMETER_NEXT_STATE);
		} else {
			orderJobChainName = null;
			nextState = null;
			flgCreateOrders4AllFiles = false;
			flgMergeOrderParameter = false;
		}
		lngFileAge = calculateFileAge(getParamValue(PARAMETER_FILE_AGE, "0"));
		warningFileLimit = getParamInteger(PARAMETER_WARNING_FILE_LIMIT, 0);
		expectedSizeOfResultSet = getParamInteger(PARAMETER_EXPECTED_SIZE_OF_RESULT_SET, 0);
		raiseErrorIfResultSetIs = getParamValue(PARAMETER_RAISE_ERROR_IF_RESULT_SET_IS, EMPTY_STRING);
		resultList2File = getParamValue(PARAMETER_RESULT_LIST_FILE, EMPTY_STRING);
		onEmptyResultSet = getParamValue(PARAMETER_ON_EMPTY_RESULT_SET, EMPTY_STRING);
		source = file = filePath = getParamValue(
				new String[] { PARAMETER_SOURCE_FILE, PARAMETER_FILE, PARAMETER_FILE_PATH }, EMPTY_STRING);
		String spec = getParamValue(PARAMETER_FILE_SPEC);
		fileSpec = getParamValue(new String[] { PARAMETER_FILE_SPEC, PARAMETER_FILE_SPECIFICATION }, fileSpecDefault);
		if (!(isNotEmpty(spec) || isNotEmpty(source))) {
			source = file = filePath = getParamValue(new String[] { ORDER_PARAMETER_SCHEDULER_FILE_PATH },
					EMPTY_STRING);
		}
		target = getParamValue(PARAMETER_TARGET_FILE, null);
		minFileAge = getParamValue(PARAMETER_MIN_FILE_AGE, "0");
		sosOptionFileAge.setValue(minFileAge);
		minFileAge = String.valueOf(sosOptionFileAge.getAgeAsSeconds());
		maxFileAge = getParamValue(PARAMETER_MAX_FILE_AGE, "0");
		sosOptionFileAge.setValue(maxFileAge);
		maxFileAge = String.valueOf(sosOptionFileAge.getAgeAsSeconds());
		minFileSize = getParamValue(PARAMETER_MIN_FILE_SIZE, fileSizeDefault);
		maxFileSize = getParamValue(PARAMETERMAX_FILE_SIZE, fileSizeDefault);
		flgUseNIOLock = getParamBoolean("use_nio_lock", false);
		strGracious = getParamValue(PARAMTER_GRACIOUS, "false");
		skipFirstFiles = getParamInteger(PARAMETER_SKIP_FIRST_FILES, 0);
		skipLastFiles = getParamInteger(PARAMETER_SKIP_LAST_FILES, 0);
		sortCriteria = getParamValue(PARAMETER_SORT_CRITERIA, "name");
		sortOrder = getParamValue(PARAMETER_SORT_ORDER, "asc");

		flags = 0;
		String strCreateWhat = getParamValue(
				new String[] { PARAMETER_CREATE_DIR, PARAMETER_CREATE_FILE, PARAMETER_CREATE_FILES }, "false");
		if (toBoolean(strCreateWhat)) {
			flags |= SOSFileSystemOperations.CREATE_DIR;
		}
		if (getParamBoolean(PARAMTER_GRACIOUS, false)) {
			flags |= SOSFileSystemOperations.GRACIOUS;
		}
		if (!getParamBoolean(PARAMETER_OVERWRITE, true)) {
			flags |= SOSFileSystemOperations.NOT_OVERWRITE;
		}
		if (getParamBoolean(PARAMETER_RECURSIVE, false)) {
			flags |= SOSFileSystemOperations.RECURSIVE;
		}
		if (getParamBoolean(PARAMETER_REMOVE_DIR, false)) {
			flags |= SOSFileSystemOperations.REMOVE_DIR;
		}
		countFiles = getParamBoolean(PARAMETER_COUNT_FILES, false);
		if (countFiles && !isJobchain()) {
			JSJ_E_0120.toLog(PARAMETER_COUNT_FILES);
		}
		replacing = getParamValue(PARAMETER_REPLACING, EMPTY_STRING);
		replacement = getParamValue(PARAMETER_REPLACEMENT, EMPTY_STRING);
		String strM = JSJ_E_0110.get(PARAMETER_REPLACEMENT, PARAMETER_REPLACING);
		if (isNotNull(replacing) && isNull(replacement)) {
			replacement = EMPTY_STRING;
		} else if (isNull(replacing) && isNotNull(replacement)) {
			throw new JobSchedulerException(strM);
		}
		flgCheckSteadyStateOfFiles = getParamBoolean(PARAMETER_CHECK_STEADY_STATE_OF_FILE, false);
		steadyCount = getParamLong(PARAMETER_STEADY_STATE_COUNT, 30);
		sosOptionTime.setValue(getParamValue(PARAMETER_CHECK_STEADY_STATE_INTERVAL, "1"));
		checkSteadyStateInterval = sosOptionTime.getTimeAsSeconds() * 1000;
	}

	@Override
	public void spooler_exit() {
		try {
			super.spooler_exit();
		} catch (Exception e) {
			// no error processing at job level
		}
	}

	public boolean isGraciousAll() {
		return strGracious != null && strGracious.equalsIgnoreCase(ALL);
	}

	public boolean isGraciousTrue() {
		boolean flgResult = false;
		try {
			if (isNotEmpty(strGracious)) {
				flgResult = toBoolean(strGracious);
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		return flgResult;
	}

	public void saveResultList() {
		if (isNull(lstResultList)) {
			lstResultList = new Vector<File>();
		}
		List<File> lstR = sosFileOperations.lstResultList;
		if (isNotNull(lstR) && !lstR.isEmpty()) {
			lstResultList.addAll(lstR);
		}
	}

	public boolean createResultListParam(final boolean pflgOperationWasSuccessful) {
		String strFirstFile = EMPTY_STRING;
		String strResultSetFileList = EMPTY_STRING;
		if (isNull(lstResultList)) {
			saveResultList();
		}
		noOfHitsInResultSet = 0;
		if (isNotNull(lstResultList) && !lstResultList.isEmpty()) {
			noOfHitsInResultSet = lstResultList.size();
			strFirstFile = lstResultList.get(0).getAbsolutePath();
			for (File objFile : lstResultList) {
				strResultSetFileList += objFile.getAbsolutePath() + ";";
			}
		}
		if (isJobchain()) {
			if (countFiles) {
				setOrderParameter(ORDER_PARAMETER_SCHEDULER_SOS_FILE_OPERATIONS_FILE_COUNT,
						String.valueOf(noOfHitsInResultSet));
			}
			Variable_set objP = spooler_task.order().params();
			if (isNotNull(objP)) {
				setOrderParameter(ORDER_PARAMETER_SCHEDULER_SOS_FILE_OPERATIONS_RESULT_SET, strResultSetFileList);
				setOrderParameter(ORDEDR_PARAMETER_SCHEDULER_SOS_FILE_OPERATIONS_RESULT_SET_SIZE,
						String.valueOf(noOfHitsInResultSet));
			}
			if (isNotEmpty(onEmptyResultSet) && noOfHitsInResultSet <= 0) {
				JSJ_I_0090.toLog(onEmptyResultSet);
				setNextNodeState(onEmptyResultSet);
			}
		}
		if (isNotEmpty(resultList2File) && isNotEmpty(strResultSetFileList)) {
			JSTextFile objResultListFile = new JSTextFile(resultList2File);
			try {
				if (objResultListFile.canWrite()) {
					objResultListFile.write(strResultSetFileList);
					objResultListFile.close();
				} else {
					throw new JobSchedulerException(JSJ_F_0090.get(PARAMETER_RESULT_LIST_FILE, resultList2File));
				}
			} catch (Exception e) {
				JSJ_F_0080.toLog(resultList2File, PARAMETER_RESULT_LIST_FILE);
				throw new JobSchedulerException(e);
			}
		}
		if (isNotEmpty(raiseErrorIfResultSetIs)) {
			boolean flgR = compareIntValues(raiseErrorIfResultSetIs, noOfHitsInResultSet, expectedSizeOfResultSet);
			if (flgR) {
				LOGGER.info(JSJ_E_0040.params(noOfHitsInResultSet, raiseErrorIfResultSetIs, expectedSizeOfResultSet));
				return false;
			}
		}
		if (noOfHitsInResultSet > 0 && flgCreateOrder && pflgOperationWasSuccessful) {
			if (flgCreateOrders4AllFiles) {
				for (File objFile : lstResultList) {
					createOrder(objFile.getAbsolutePath(), orderJobChainName);
				}
			} else {
				createOrder(strFirstFile, orderJobChainName);
			}
		}
		return pflgOperationWasSuccessful;
	}

	public boolean toBoolean(final String value) throws Exception {
		Hashtable<String, String> boolValues = new Hashtable<String, String>();
		boolValues.put("true", "true");
		boolValues.put("false", "false");
		boolValues.put("j", "true");
		boolValues.put("ja", "true");
		boolValues.put("y", "true");
		boolValues.put("yes", "true");
		boolValues.put("n", "false");
		boolValues.put("nein", "false");
		boolValues.put("no", "false");
		boolValues.put("on", "true");
		boolValues.put("off", "false");
		boolValues.put("1", "true");
		boolValues.put("0", "false");
		boolValues.put("all", "true");
		boolValues.put("none", "false");

		try {
			if (value == null) {
				throw new JobSchedulerException("null");
			}
			String v = value.toLowerCase();
			String bool = boolValues.get(v);
			if (bool == null) {
				throw new JobSchedulerException("\"" + value + "\"");
			}
			return "true".equals(bool);
		} catch (Exception e) {
			throw new JobSchedulerException("cannot evaluate to boolean: " + e.getMessage());
		}
	}

	public boolean compareIntValues(final String pstrComparator, final int pintValue1, final int pintValue2) {
		HashMap<String, Integer> objRelOp = new HashMap<String, Integer>();
		objRelOp.put("eq", 1);
		objRelOp.put("equal", 1);
		objRelOp.put("==", 1);
		objRelOp.put("=", 1);
		objRelOp.put("ne", 2);
		objRelOp.put("not equal", 2);
		objRelOp.put("!=", 2);
		objRelOp.put("<>", 2);
		objRelOp.put("lt", 3);
		objRelOp.put("less than", 3);
		objRelOp.put("<", 3);
		objRelOp.put("le", 4);
		objRelOp.put("less or equal", 4);
		objRelOp.put("<=", 4);
		objRelOp.put("ge", 5);
		objRelOp.put(JSJ_T_0010.get(), 5);
		objRelOp.put(">=", 5);
		objRelOp.put("gt", 6);
		objRelOp.put("greater than", 6);
		objRelOp.put(">", 6);
		boolean flgR = false;
		String strT1 = pstrComparator;
		Integer iOp = objRelOp.get(strT1.toLowerCase());
		if (isNotNull(iOp)) {
			switch (iOp) {
			case 1:
				flgR = pintValue1 == pintValue2;
				break;
			case 2:
				flgR = pintValue1 != pintValue2;
				break;
			case 3:
				flgR = pintValue1 < pintValue2;
				break;
			case 4:
				flgR = pintValue1 <= pintValue2;
				break;
			case 5:
				flgR = pintValue1 >= pintValue2;
				break;
			case 6:
				flgR = pintValue1 > pintValue2;
				break;
			default:
				break;
			}
		} else {
			throw new JobSchedulerException(JSJ_E_0017.get(pstrComparator));
		}
		return flgR;
	}

	private void createOrder(final String pstrOrder4FileName, final String pstrOrderJobChainName) {
		final String methodName = "JobSchedulerFileOperationBase::createOrder";
		Order objOrder = spooler.create_order();
		Variable_set objOrderParams = spooler.create_variable_set();
		if (flgMergeOrderParameter && isOrderJob()) {
			objOrderParams.merge(getOrderParams());
		}
		objOrderParams.set_value(ORDER_PARAMETER_SCHEDULER_FILE_PATH, pstrOrder4FileName);
		objOrderParams.set_value(ORDER_PARAMETER_SCHEDULER_FILE_PARENT, new File(pstrOrder4FileName).getParent());
		objOrderParams.set_value(ORDER_PARAMETER_SCHEDULER_FILE_NAME, new File(pstrOrder4FileName).getName());
		if (isNotEmpty(nextState)) {
			objOrder.set_state(nextState);
		}
		objOrder.set_params(objOrderParams);
		objOrder.set_id(pstrOrder4FileName);
		objOrder.set_title(JSJ_I_0017.get(methodName));
		Job_chain objJobchain = spooler.job_chain(pstrOrderJobChainName);
		objJobchain.add_order(objOrder);
		String strT = JSJ_I_0018.get(pstrOrder4FileName, pstrOrderJobChainName);
		if (isNotEmpty(nextState)) {
			strT += " " + JSJ_I_0019.get(nextState);
		}
		LOGGER.info(strT);
	}

	public String replaceVars4(String pstrReplaceIn) {
		String strParamNameEnclosedInPercentSigns = "^.*%([^%]+)%.*$";
		if (isNotNull(pstrReplaceIn)) {
			while (pstrReplaceIn.matches(strParamNameEnclosedInPercentSigns)) {
				String p = pstrReplaceIn.replaceFirst(strParamNameEnclosedInPercentSigns, "$1");
				String strPP = "%" + p + "%";
				String s = params.get(p);
				if (isNotNull(s)) {
					s = s.replace('\\', '/');
					pstrReplaceIn = pstrReplaceIn.replaceAll(strPP, s);
					JSJ_D_0044.toLog(name, strPP, s);
				} else {
					pstrReplaceIn = pstrReplaceIn.replaceAll(strPP, "?" + p + "?");
					JSJ_W_0043.toLog(p);
				}
			}
		}
		return pstrReplaceIn;
	}

	public void setParams(final HashMap<String, String> pobjparams) {
		params = pobjparams;
	}

	public boolean setReturnResult(final boolean pflgResult) {
		boolean rc1 = pflgResult;
		rc1 = createResultListParam(pflgResult);
		if (!rc1 && isGraciousAll()) {
			return signalSuccess();
		} else {
			if (!rc1 && isGraciousTrue()) {
				return signalFailureNoLog();
			} else {
				if (rc1) {
					return signalSuccess();
				} else {
					return signalFailure();
				}
			}
		}
	}

	public void checkMandatoryFile() {
		if (isNull(file)) {
			throw new JobSchedulerException(JSJ_E_0020.params(PARAMETER_FILE));
		}
	}

	public void checkMandatorySource() {
		if (isNull(source)) {
			throw new JobSchedulerException(JSJ_E_0020.params(PARAMETER_SOURCE_FILE));
		}
	}

	public void checkMandatoryTarget() {
		if (isNull(target)) {
			throw new JobSchedulerException(JSJ_E_0020.params(PARAMETER_TARGET_FILE));
		}
	}

	public long calculateFileAge(final String hoursMinSec) {
		long age = 0;
		if (isNotEmpty(hoursMinSec)) {
			if (hoursMinSec.indexOf(":") > -1) {
				String[] timeArray = hoursMinSec.split(":");
				long hours = Long.parseLong(timeArray[0]);
				long minutes = Long.parseLong(timeArray[1]);
				long seconds = 0;
				if (timeArray.length > 2) {
					seconds = Long.parseLong(timeArray[2]);
				}
				age = hours * 3600000 + minutes * 60000 + seconds * 1000;
			} else {
				age = Long.parseLong(hoursMinSec);
			}
		}
		return age;
	}

	public Variable_set getParams() {
		final String methodName = "JSFileOperationBase::getParams";
		throw new JobSchedulerException(Messages.getMsg(JSJ_F_0110, methodName));
	}

	public void setParams(final Variable_set params1) {
		final String methodName = "JSFileOperationBase::setParams";
		throw new JobSchedulerException(Messages.getMsg(JSJ_F_0110, methodName));
	}

	class FileDescriptor {

		public long lastModificationDate = 0;
		public long lastFileLength = 0;
		public String FileName = "";
		public boolean flgIsSteady = false;

		FileDescriptor() {
			//
		}
	}

	public boolean checkSteadyStateOfFiles() {
		if (isNull(lstResultList)) {
			saveResultList();
		}
		boolean flgAllFilesAreSteady = flgOperationWasSuccessful;
		if (flgOperationWasSuccessful && flgCheckSteadyStateOfFiles && !lstResultList.isEmpty()) {
			LOGGER.debug("checking file(s) for steady state");
			Vector<FileDescriptor> lstFD = new Vector<FileDescriptor>();
			for (File objFile : lstResultList) {
				FileDescriptor objFD = new FileDescriptor();
				objFD.lastFileLength = objFile.length();
				objFD.lastModificationDate = objFile.lastModified();
				objFD.FileName = objFile.getAbsolutePath();
				LOGGER.debug("filedescriptor is : " + objFD.lastModificationDate + ", " + objFD.lastFileLength);
				lstFD.add(objFD);
			}
			try {
				Thread.sleep(checkSteadyStateInterval);
			} catch (InterruptedException e1) {
				LOGGER.error(e1.getMessage(), e1);
			}
			for (int i = 0; i < steadyCount; i++) {
				flgAllFilesAreSteady = true;
				for (FileDescriptor objFD : lstFD) {
					File objActFile = new File(objFD.FileName);
					LOGGER.debug("result is : " + objActFile.lastModified() + ", " + objFD.lastModificationDate + ", "
							+ objActFile.length() + ", " + objFD.lastFileLength);
					if (flgUseNIOLock) {
						try {
							RandomAccessFile objRAFile = new RandomAccessFile(objActFile, "rw");
							FileChannel channel = objRAFile.getChannel();
							FileLock lock = channel.lock();
							try {
								lock = channel.tryLock();
								LOGGER.debug(String.format("lock for file '%1$s' ok", objActFile.getAbsolutePath()));
								break;
							} catch (OverlappingFileLockException e) {
								flgAllFilesAreSteady = false;
								LOGGER.info(String.format("File '%1$s' is open by someone else",
										objActFile.getAbsolutePath()));
								break;
							} finally {
								lock.release();
								LOGGER.debug(String.format("release lock for '%1$s'", objActFile.getAbsolutePath()));
								if (objRAFile != null) {
									channel.close();
									objRAFile.close();
									objRAFile = null;
								}
							}
						} catch (FileNotFoundException e) {
							LOGGER.error(e.getMessage(), e);
						} catch (IOException e) {
							LOGGER.error(e.getMessage(), e);
						}
					}
					if (objActFile.lastModified() != objFD.lastModificationDate
							|| objActFile.length() != objFD.lastFileLength) {
						flgAllFilesAreSteady = false;
						objFD.lastModificationDate = objActFile.lastModified();
						objFD.lastFileLength = objActFile.length();
						objFD.flgIsSteady = false;
						LOGGER.info(String.format("File '%1$s' changed during checking steady state",
								objActFile.getAbsolutePath()));
						break;
					} else {
						objFD.flgIsSteady = true;
					}
				}
				if (!flgAllFilesAreSteady) {
					try {
						Thread.sleep(checkSteadyStateInterval);
					} catch (InterruptedException e) {
						LOGGER.error(e.getMessage(), e);
					}
				} else {
					break;
				}
			}
			if (!flgAllFilesAreSteady) {
				LOGGER.error("not all files are steady");
				for (FileDescriptor objFD : lstFD) {
					if (!objFD.flgIsSteady) {
						LOGGER.info(String.format("File '%1$s' is not steady", objFD.FileName));
					}
				}
				throw new JobSchedulerException("not all files are steady");
			}
		}
		return flgAllFilesAreSteady;
	}

}
