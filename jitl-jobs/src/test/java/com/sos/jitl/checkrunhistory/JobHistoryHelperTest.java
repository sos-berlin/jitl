package com.sos.jitl.checkrunhistory;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.scheduler.model.answers.HistoryEntry;

public class JobHistoryHelperTest {
	private  JobHistoryHelper jobHistoryHelper;


	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		 jobHistoryHelper = new JobHistoryHelper();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetOrderId() {
	    assertEquals ("testGetOrderId", "",jobHistoryHelper.getOrderId("jobchain"));
	    assertEquals ("testGetOrderId", "",jobHistoryHelper.getOrderId("jobchain()"));
	    assertEquals ("testGetOrderId", "test",jobHistoryHelper.getOrderId("jobchain(test)"));
	}

	@Test
	public void testGetJobChainName() {
	    assertEquals ("testGetOrderId", "jobchain",jobHistoryHelper.getJobChainName("jobchain"));
	    assertEquals ("testGetOrderId", "jobchain",jobHistoryHelper.getJobChainName("jobchain()"));
	    assertEquals ("testGetOrderId", "jobchain",jobHistoryHelper.getJobChainName("jobchain(test)"));
	}
	
	@Test
	public void testIsInTimeLimit() throws Exception{
		HistoryEntry historyItem = new HistoryEntry();
		historyItem.setEndTime("2015-10-29T12:10:41+02:00");
		boolean result = jobHistoryHelper.isInTimeLimit("10:00:01-22:00:02",historyItem.getEndTime());

		result = jobHistoryHelper.isInTimeLimit("-22:00:02",historyItem.getEndTime());
		result = jobHistoryHelper.isInTimeLimit("-22:00:02",historyItem.getEndTime());
		result = jobHistoryHelper.isInTimeLimit("10:00:01-",historyItem.getEndTime());
	}
	
	@Test 
	public void testGetTime() throws Exception {
		String s="";
		s=jobHistoryHelper.getTime("2:00:00:00","abcd(3:00:00:00)");
		assertEquals ("testGetTime", "3:00:00:00",s);
		s=jobHistoryHelper.getTime("2:00:00:00","abcd()");
		assertEquals ("testGetTime", "2:00:00:00",s);
		s=jobHistoryHelper.getTime("2:00:00:00","abcd");
		assertEquals ("testGetTime", "2:00:00:00",s);
	}

	@Test 
	public void testGetMethodName() throws Exception {
		String s = "";
		s = jobHistoryHelper.getMethodName("abcd(3:00:00:00)");
		assertEquals ("testGetMethodName", "abcd",s);
		s = jobHistoryHelper.getMethodName("abcd()");
		assertEquals ("testGetMethodName", "abcd",s);
		s = jobHistoryHelper.getMethodName("abcd");
		assertEquals ("testGetMethodName", "abcd",s);	}

	@Test 
	public void testGetParameter() throws Exception {
		String s = "";
		s = jobHistoryHelper.getParameter("abcd(3:00:00:00)");
		assertEquals ("testGetParameter", "3:00:00:00",s);
		s = jobHistoryHelper.getParameter("abcd()");
		assertEquals ("testGetParameter", "",s);
		s = jobHistoryHelper.getParameter("abcd");
		assertEquals ("testGetParameter", "",s);
	}

}
