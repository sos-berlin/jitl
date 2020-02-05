package sos.scheduler.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;

public class JSExistFileOptionsJUnitTest extends JSToolBox {

	private final String conClassName = "JSExistFileOptionsJUnitTest"; //$NON-NLS-1$
	private JSExistsFile objE = null;

	protected JSExistsFileOptions objOptions = null;

	public JSExistFileOptionsJUnitTest() {
		//
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		objE = new JSExistsFile();
		objE.registerMessageListener(this);
		objOptions = objE.getOptions();
		objOptions.registerMessageListener(this);

		JSListenerClass.bolLogDebugInformation = true;
		JSListenerClass.intMaxDebugLevel = 9;
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testcount_files() { // SOSOptionBoolean
		objOptions.count_files.value(true);
		assertTrue("Return the size of resultset If this parameter is set true ", objOptions.count_files.value());
		objOptions.count_files.value(false);
		assertFalse("Return the size of resultset If this parameter is set true ", objOptions.count_files.value());

	}

	@Test
	public void testcreate_order() { // SOSOptionBoolean
		objOptions.create_order.setValue("true");
		assertTrue("Activate file-order creation With this parameter it is possible to specif",
				objOptions.create_order.value());
		objOptions.create_order.setValue("false");
		assertFalse("Activate file-order creation With this parameter it is possible to specif",
				objOptions.create_order.value());

	}

	@Test
	public void testcreate_orders_for_all_files() { // SOSOptionBoolean
		objOptions.create_orders_for_all_files.setValue("true");
		assertTrue("Create a file-order for every file in the result-list",
				objOptions.create_orders_for_all_files.value());
		objOptions.create_orders_for_all_files.setValue("false");
		assertFalse("Create a file-order for every file in the result-list",
				objOptions.create_orders_for_all_files.value());

	}

	@Test
	public void testexpected_size_of_result_set() { // SOSOptionInteger
		objOptions.expected_size_of_result_set.setValue("12345");
		assertEquals("number of expected hits in result-list", objOptions.expected_size_of_result_set.getValue(),
				"12345");
		assertEquals("number of expected hits in result-list", objOptions.expected_size_of_result_set.value(), 12345);
		objOptions.expected_size_of_result_set.value(12345);
		assertEquals("number of expected hits in result-list", objOptions.expected_size_of_result_set.getValue(),
				"12345");
		assertEquals("number of expected hits in result-list", objOptions.expected_size_of_result_set.value(), 12345);

	}

	@Test
	public void testfile() { // SOSOptionString
		objOptions.file.setValue(".");
		assertEquals("File or Folder to watch for Checked file or directory Supports", objOptions.file.getValue(), ".");

	}

	@Test
	public void testfile_spec() { // SOSOptionRegExp
		objOptions.file_spec.setValue("++----++");
		assertEquals("Regular Expression for filename filtering Regular Expression for file fi",
				objOptions.file_spec.getValue(), "++----++");

	}

	@Test
	public void testgracious() { // SOSOptionGracious
		objOptions.gracious.setValue("false");
		assertEquals("Specify error message tolerance Enables or disables error messages that",
				objOptions.gracious.getValue(), "false");

	}

	@Test
	public void testmax_file_age() { // SOSOptionTime
		objOptions.max_file_age.setValue("30");
		assertEquals("maximum age of a file Specifies the maximum age of a file. If a file",
				objOptions.max_file_age.getValue(), "30");
		assertEquals("maximum age of a file Specifies the maximum age of a file. If a file",
				objOptions.max_file_age.getTimeAsSeconds(), 30);
		objOptions.max_file_age.setValue("1:30");
		assertEquals("maximum age of a file Specifies the maximum age of a file. If a file",
				objOptions.max_file_age.getValue(), "1:30");
		assertEquals("maximum age of a file Specifies the maximum age of a file. If a file",
				objOptions.max_file_age.getTimeAsSeconds(), 90);
		objOptions.max_file_age.setValue("1:10:30");
		assertEquals("maximum age of a file Specifies the maximum age of a file. If a file",
				objOptions.max_file_age.getValue(), "1:10:30");
		assertEquals("maximum age of a file Specifies the maximum age of a file. If a file",
				objOptions.max_file_age.getTimeAsSeconds(), 30 + 10 * 60 + 60 * 60);

	}

	@Test
	public void testmax_file_size() { // SOSOptionFileSize
		objOptions.max_file_size.setValue("25KB");
		assertEquals("maximum size of a file Specifies the maximum size of a file in",
				objOptions.max_file_size.getValue(), "25KB");
		objOptions.max_file_size.setValue("25MB");
		assertEquals("maximum size of a file Specifies the maximum size of a file in",
				objOptions.max_file_size.getValue(), "25MB");
		objOptions.max_file_size.setValue("25GB");
		assertEquals("maximum size of a file Specifies the maximum size of a file in",
				objOptions.max_file_size.getValue(), "25GB");

	}

	@Test
	public void testmin_file_age() { // SOSOptionTime
		objOptions.min_file_age.setValue("30");
		assertEquals("minimum age of a file Specifies the minimum age of a files. If the fi",
				objOptions.min_file_age.getValue(), "30");
		assertEquals("minimum age of a file Specifies the minimum age of a files. If the fi",
				objOptions.min_file_age.getTimeAsSeconds(), 30);
		objOptions.min_file_age.setValue("1:30");
		assertEquals("minimum age of a file Specifies the minimum age of a files. If the fi",
				objOptions.min_file_age.getValue(), "1:30");
		assertEquals("minimum age of a file Specifies the minimum age of a files. If the fi",
				objOptions.min_file_age.getTimeAsSeconds(), 90);
		objOptions.min_file_age.setValue("1:10:30");
		assertEquals("minimum age of a file Specifies the minimum age of a files. If the fi",
				objOptions.min_file_age.getValue(), "1:10:30");
		assertEquals("minimum age of a file Specifies the minimum age of a files. If the fi",
				objOptions.min_file_age.getTimeAsSeconds(), 30 + 10 * 60 + 60 * 60);

	}

	@Test
	public void testmin_file_size() { // SOSOptionFileSize
		objOptions.min_file_size.setValue("25KB");
		assertEquals("minimum size of one or multiple files Specifies the minimum size of one",
				objOptions.min_file_size.getValue(), "25KB");
		objOptions.min_file_size.setValue("25MB");
		assertEquals("minimum size of one or multiple files Specifies the minimum size of one",
				objOptions.min_file_size.getValue(), "25MB");
		objOptions.min_file_size.setValue("25GB");
		assertEquals("minimum size of one or multiple files Specifies the minimum size of one",
				objOptions.min_file_size.getValue(), "25GB");

	}

	@Test
	public void testnext_state() { // SOSOptionJobChainNode
		objOptions.next_state.setValue("++----++");
		assertEquals("The first node to execute in a jobchain The name of the node of a jobchai",
				objOptions.next_state.getValue(), "++----++");

	}

	@Test
	public void teston_empty_result_set() { // SOSOptionJobChainNode
		objOptions.on_empty_result_set.setValue("++empty++");
		assertEquals("Set next node on empty result set The next Node (Step, Job) to execute i",
				objOptions.on_empty_result_set.getValue(), "++empty++");

	}

	@Test
	public void testorder_jobchain_name() { // SOSOptionString
		objOptions.order_jobchain_name.setValue("++----++");
		assertEquals("The name of the jobchain which belongs to the order The name of the jobch",
				objOptions.order_jobchain_name.getValue(), "++----++");

	}

	@Test
	@Ignore("Test set to Ignore for later examination")
	public void testraise_error_if_result_set_is() { // SOSOptionRelOp
		objOptions.raise_error_if_result_set_is.setValue("++0++");
		assertEquals("raise error on expected size of result-set With this parameter it is poss",
				objOptions.raise_error_if_result_set_is.getValue(), "++0++");

	}

	@Test
	public void testresult_list_file() { // SOSOptionFileName
		objOptions.result_list_file.setValue("++empty++");
		assertEquals("Name of the result-list file If the value of this parameter specifies a v",
				objOptions.result_list_file.getValue(), "++empty++");

	}

	@Test
	public void testscheduler_file_name() { // SOSOptionFileName
		objOptions.scheduler_file_name.setValue("++empty++");
		assertEquals("Name of the file to process for a file-order", objOptions.scheduler_file_name.getValue(),
				"++empty++");

	}

	@Test
	public void testscheduler_file_parent() { // SOSOptionFileName
		objOptions.scheduler_file_parent.setValue("++empty++");
		assertEquals("pathanme of the file to process for a file-order", objOptions.scheduler_file_parent.getValue(),
				"++empty++");

	}

	@Test
	public void testscheduler_file_path() { // SOSOptionFileName
		objOptions.scheduler_file_path.setValue("++empty++");
		assertEquals("file to process for a file-order Using Directory Monitoring with",
				objOptions.scheduler_file_path.getValue(), "++empty++");

	}

	@Test
	public void testscheduler_sosfileoperations_file_count() { // SOSOptionInteger
		objOptions.scheduler_sosfileoperations_file_count.setValue("12345");
		assertEquals("Return the size of the result set after a file operation",
				objOptions.scheduler_sosfileoperations_file_count.getValue(), "12345");
		assertEquals("Return the size of the result set after a file operation",
				objOptions.scheduler_sosfileoperations_file_count.value(), 12345);
		objOptions.scheduler_sosfileoperations_file_count.value(12345);
		assertEquals("Return the size of the result set after a file operation",
				objOptions.scheduler_sosfileoperations_file_count.getValue(), "12345");
		assertEquals("Return the size of the result set after a file operation",
				objOptions.scheduler_sosfileoperations_file_count.value(), 12345);

	}

	@Test
	public void testscheduler_sosfileoperations_resultset() { // SOSOptionstring
		objOptions.scheduler_sosfileoperations_resultset.setValue("++empty++");
		assertEquals("The result of the operation as a list of items",
				objOptions.scheduler_sosfileoperations_resultset.getValue(), "++empty++");

	}

	@Test
	public void testscheduler_sosfileoperations_resultsetsize() { // SOSOptionsInteger
		objOptions.scheduler_sosfileoperations_resultsetsize.setValue("++empty++");
		assertEquals("The amount of hits in the result set of the operation",
				objOptions.scheduler_sosfileoperations_resultsetsize.getValue(), "++empty++");

	}

	@Test
	public void testskip_first_files() { // SOSOptionInteger
		objOptions.skip_first_files.setValue("12345");
		assertEquals("number of files to remove from the top of the result-set The numbe",
				objOptions.skip_first_files.getValue(), "12345");
		assertEquals("number of files to remove from the top of the result-set The numbe",
				objOptions.skip_first_files.value(), 12345);
		objOptions.skip_first_files.value(12345);
		assertEquals("number of files to remove from the top of the result-set The numbe",
				objOptions.skip_first_files.getValue(), "12345");
		assertEquals("number of files to remove from the top of the result-set The numbe",
				objOptions.skip_first_files.value(), 12345);

	}

	@Test
	public void testskip_last_files() { // SOSOptionInteger
		objOptions.skip_last_files.setValue("12345");
		assertEquals("number of files to remove from the bottom of the result-set The numbe",
				objOptions.skip_last_files.getValue(), "12345");
		assertEquals("number of files to remove from the bottom of the result-set The numbe",
				objOptions.skip_last_files.value(), 12345);
		objOptions.skip_last_files.value(12345);
		assertEquals("number of files to remove from the bottom of the result-set The numbe",
				objOptions.skip_last_files.getValue(), "12345");
		assertEquals("number of files to remove from the bottom of the result-set The numbe",
				objOptions.skip_last_files.value(), 12345);

	}

} // public class JSExistFileOptionsJUnitTest