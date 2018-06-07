package com.sos.jitl.eventing.db;

import static org.junit.Assert.*;

import org.junit.Test;

public class SchedulerEventFilterTest {

	@Test
	public void test() throws Exception {
		SchedulerEventFilter filter = new SchedulerEventFilter();
		filter.setExpires("2018-06-07 11:15:00");
	}

}
