package sos.scheduler.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.JSHelper.io.SOSFileSystemOperations;
import com.sos.JSHelper.io.SOSFileSystemOperationsCopy;
import com.sos.JSHelper.io.Files.JSTextFile;

public class CopyFileJUnitTest extends JSToolBox {

	protected JSExistsFileOptions objOptions = null;
	private static final int NUMBER_OF_FILE_2_SKIP = 2;
	private static final int NUMBER_OF_TEST_FILES = 10;
	private static final String conTestRegularExpression1 = "^.*\\.cpy$";
	private static final String TEST_BASE_FOLDER_NAME = "j:/e/java/development-testdata/com.sos.scheduler/";

	public CopyFileJUnitTest() {
		//
	}

	@Before
	public void setUp() throws Exception {
	}

	private void createTestFiles() throws Exception {
		String strT = "Testfile Testfile Testfile";
		long lngTimeLag = 3600000;
		for (int i = 0; i < NUMBER_OF_TEST_FILES; i++) {
			JSTextFile jsTextFile = new JSTextFile(TEST_BASE_FOLDER_NAME + "testfile" + i + ".cpy");
			if (jsTextFile.exists()) {
				break;
			}
			jsTextFile.write(strT);
			strT = strT + strT;
			jsTextFile.deleteOnExit();
			Date lastModified = new Date(jsTextFile.lastModified());
			jsTextFile.close();
			jsTextFile.setLastModified(lastModified.getTime() - lngTimeLag);
			lngTimeLag += 3600000;
		}
	}

	@Test
//	@Ignore("Test set to Ignore for later examination")
	public void testFileSpec() throws Exception {
		createTestFiles();
		String sourceFolder = TEST_BASE_FOLDER_NAME;
		String targetFolder = TEST_BASE_FOLDER_NAME + "/out";
		String fileSpec = ".*";
		int flags = 0;
		String replacing = "";
		String replacement = "";
		String minFileAge = "";
		String maxFileAge = "";
		String minFileSize = "";
		String maxFileSize = "";
		int skipFirstFiles = 1;
		int skipLastFiles = 2;
		int isCaseInsensitive = Pattern.CASE_INSENSITIVE;
		String sortCriteria = "name";
		String sortOrder = "asc";

		SOSFileSystemOperations sosFileOperations = new SOSFileSystemOperationsCopy();
		long noOfHitsInResultSet = sosFileOperations.copyFileCnt(sourceFolder, targetFolder, fileSpec, flags,
				isCaseInsensitive, replacing, replacement, minFileAge, maxFileAge, minFileSize, maxFileSize,
				skipFirstFiles, skipLastFiles, sortCriteria, sortOrder);

	}

}