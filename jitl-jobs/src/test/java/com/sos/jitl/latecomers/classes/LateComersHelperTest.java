package com.sos.jitl.latecomers.classes;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.jitl.latecomers.JobSchedulerStartLatecomersOptions;

public class LateComersHelperTest {
	JobSchedulerStartLatecomersOptions jobSchedulerStartLatecomersOptions;
	LateComersHelper lateComersHelper;

	@Before
	public void setUp() throws Exception {
		jobSchedulerStartLatecomersOptions = new JobSchedulerStartLatecomersOptions();
		lateComersHelper = new LateComersHelper(jobSchedulerStartLatecomersOptions);
	}

	@Test
	public void testGetParent() {
		String s = "/a/b/c/d";
		String p = lateComersHelper.getParent(s);
		assertEquals("testGetParent", "/a/b/c", p);

	}

	@Test
	public void testIgnoreOrder() {
		jobSchedulerStartLatecomersOptions.ignoreOrderList.setValue("/a.*,1;.*/c/test,2;/a/b/c/fest.*");
		boolean a = lateComersHelper.ignoreOrder("/a/b/c/test", "1");
		boolean b = lateComersHelper.ignoreOrder("/a/b/c/test", "2");
		boolean c = lateComersHelper.ignoreOrder("/a/b/c/fest", "1");
		boolean d = lateComersHelper.ignoreOrder("/a/b/c/rest", "3");
		assertEquals("testIgnoreOrder", true, a);
		assertEquals("testIgnoreOrder", true, b);
		assertEquals("testIgnoreOrder", true, c);
		assertEquals("testIgnoreOrder", false, d);
	}

	@Test
	public void testIgnoreJob() {
		jobSchedulerStartLatecomersOptions.ignoreJobList.setValue("/a.*/b/c/test;.*/c/test;/a/b/c/fest.*");
		boolean a = lateComersHelper.ignoreJob("/a/xxx/b/c/test");
		boolean b = lateComersHelper.ignoreJob("/a/c/d/rest");
		boolean c = lateComersHelper.ignoreJob("/q/b/c/test");
		boolean d = lateComersHelper.ignoreJob("/a/b/c/fest");
		assertEquals("testIgnoreJob", true, a);
		assertEquals("testIgnoreJob", false, b);
		assertEquals("testIgnoreJob", true, c);
		assertEquals("testIgnoreJob", true, d);
	}

	@Test
	public void testIgnoreFolder() {
		jobSchedulerStartLatecomersOptions.ignoreFolderList.setValue("/a/b*;/c;/a/d/c/");
		boolean a = lateComersHelper.ignoreFolder(lateComersHelper.getParent("/a/b/b/c/test"));
		boolean b = lateComersHelper.ignoreFolder(lateComersHelper.getParent("/c/rest"));
		boolean c = lateComersHelper.ignoreFolder(lateComersHelper.getParent("/a/d/c/test"));
		boolean d = lateComersHelper.ignoreFolder(lateComersHelper.getParent("/a/d/c/c/fest"));
		assertEquals("testIgnoreFolder", true, a);
		assertEquals("testIgnoreFolder", true, b);
		assertEquals("testIgnoreFolder", true, c);
		assertEquals("testIgnoreFolder", false, d);
	}
	
	@Test
	public void testConsiderJob() {
		jobSchedulerStartLatecomersOptions.jobs.setValue("/a.*/b/c/test;.*/c/test;/a/b/c/fest.*");
		boolean a = lateComersHelper.considerJob("/a/xxx/b/c/test");
		boolean b = lateComersHelper.considerJob("/a/c/d/rest");
		boolean c = lateComersHelper.considerJob("/q/b/c/test");
		boolean d = lateComersHelper.considerJob("/a/b/c/fest");
		assertEquals("testConsiderJob", true, a);
		assertEquals("testConsiderJob", false, b);
		assertEquals("testConsiderJob", true, c);
		assertEquals("testConsiderJob", true, d);
	}
	
	@Test
	public void testConsiderOrder() {
		jobSchedulerStartLatecomersOptions.orders.setValue("/a.*,1;.*/c/test,2;/a/b/c/fest.*");
		boolean a = lateComersHelper.considerOrder("/a/b/c/test", "1");
		boolean b = lateComersHelper.considerOrder("/a/b/c/test", "2");
		boolean c = lateComersHelper.considerOrder("/a/b/c/fest", "1");
		boolean d = lateComersHelper.considerOrder("/a/b/c/rest", "3");
		assertEquals("testConsiderOrder", true, a);
		assertEquals("testConsiderOrder", true, b);
		assertEquals("testConsiderOrder", true, c);
		assertEquals("testConsiderOrder", false, d);
	}


}
