package com.sos.jitl.reporting;

import java.util.Date;

import org.joda.time.DateTime;

import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.reporting.db.DBItemReportExecution;
import com.sos.jitl.reporting.db.DBItemReportExecutionDate;
import com.sos.jitl.reporting.db.DBItemReportTrigger;
import com.sos.jitl.reporting.db.DBItemReportTriggerResult;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jitl.reporting.db.DBLayerReporting;
import com.sos.jitl.reporting.helper.EReferenceType;
import com.sos.jitl.reporting.helper.ReportUtil;

public class ReportInsertTest {
	private SOSHibernateFactory factory;
	private SOSHibernateSession session;

	public void connect(String hibernate) throws Exception {
		factory = new SOSHibernateFactory(hibernate);
		factory.setIdentifier("reporting");
		factory.setAutoCommit(false);
		factory.addClassMapping(DBLayer.getReportingClassMapping());
		factory.addClassMapping(DBLayer.getInventoryClassMapping());
		factory.build();
		
		session = factory.openStatelessSession();
	}

	public void disconnect() {
		if (session != null) {
		    session.close();
		}
		if (factory != null) {
			factory.close();
		}
	}

	private DBItemReportExecutionDate insertReportingExecutionDate(EReferenceType type, String schedulerId,
			Long historyId, Long id, Date startDate, Date endDate) throws Exception {

		String method = "insertReportingExecutionDate";

		if (startDate == null) {
			throw new Exception(
					String.format("%s: startDate is NULL (type = %s, schedulerId = %s, historyId = %s, id = %s) ",
							method, type.value(), schedulerId, historyId, id));
		}

		DateTime startDateTime = new DateTime(startDate);
		Long startDay = ReportUtil.getDayOfMonth(startDateTime);
		Long startWeek = ReportUtil.getWeekOfWeekyear(startDateTime);
		Long startQuarter = ReportUtil.getQuarterOfYear(startDateTime);
		Long startMonth = ReportUtil.getMonthOfYear(startDateTime);
		Long startYear = ReportUtil.getYear(startDateTime);

		Long endDay = new Long(0);
		Long endWeek = new Long(0);
		Long endQuarter = new Long(0);
		Long endMonth = new Long(0);
		Long endYear = new Long(0);
		if (endDate != null) {
			DateTime endDateTime = new DateTime(endDate);

			endDay = ReportUtil.getDayOfMonth(endDateTime);
			endWeek = ReportUtil.getWeekOfWeekyear(endDateTime);
			endQuarter = ReportUtil.getQuarterOfYear(endDateTime);
			endMonth = ReportUtil.getMonthOfYear(endDateTime);
			endYear = ReportUtil.getYear(endDateTime);
		}

		DBItemReportExecutionDate item = createReportExecutionDate(schedulerId, historyId, type.value(), id, startDay,
				startWeek, startQuarter, startMonth, startYear, endDay, endWeek, endQuarter, endMonth, endYear);

		this.session.save(item);
		return item;
	}

	private DBItemReportExecutionDate createReportExecutionDate(String schedulerId, Long historyId, Long referenceType,
			Long referenceId, Long startDay, Long startWeek, Long startQuarter, Long startMonth, Long startYear,
			Long endDay, Long endWeek, Long endQuarter, Long endMonth, Long endYear) throws Exception {

		DBItemReportExecutionDate item = new DBItemReportExecutionDate();

		item.setSchedulerId(schedulerId);
		item.setHistoryId(historyId);
		item.setReferenceType(referenceType);
		item.setReferenceId(referenceId);
		item.setStartDay(startDay);
		item.setStartWeek(startWeek);
		item.setStartMonth(startMonth);
		item.setStartQuarter(startQuarter);
		item.setStartYear(startYear);
		item.setEndDay(endDay);
		item.setEndWeek(endWeek);
		item.setEndMonth(endMonth);
		item.setEndQuarter(endQuarter);
		item.setEndYear(endYear);

		item.setCreated(ReportUtil.getCurrentDateTime());
		item.setModified(ReportUtil.getCurrentDateTime());

		return item;
	}

	public void insert(DBLayerReporting layer, int i, String schedulerId) throws Exception {
		Date startTime = new Date();
		DBItemReportTrigger rt = layer.createReportTrigger(schedulerId, new Long(i), "name", "title", "parentFolder",
				"parentName", "parentBasename", "parentTitle", "state", "stateText", startTime, new Date(), false,
				false);

		insertReportingExecutionDate(EReferenceType.TRIGGER, rt.getSchedulerId(), rt.getHistoryId(), rt.getId(),
				rt.getStartTime(), rt.getEndTime());

		DBItemReportExecution re = layer.createReportExecution(schedulerId, new Long(i), rt.getId(), null,
				new Integer(1), new Long(1), "folder", "name", "basename", "title", startTime, new Date(), "state",
				"cause", 0, false, null, null, null, true, false);
		this.session.save(re);

		insertReportingExecutionDate(EReferenceType.EXECUTION, re.getSchedulerId(), re.getHistoryId(), re.getId(),
				re.getStartTime(), re.getEndTime());

		DBItemReportTriggerResult tr = layer.createReportTriggerResults(schedulerId, new Long(i), rt.getId(),
				"startCause", new Long(1), false, null, null);
		this.session.save(tr);
		System.out.println("----- "+i+" end ");

	}

	public static void main(String[] args) {
		String schedulerId = "re-dell_4444_jobscheduler.1.11x64-snapshot";
		String config = "D:/Arbeit/scheduler/jobscheduler_data/" + schedulerId + "/config";
		String hibernate = config + "/hibernate.cfg.xml";

		ReportInsertTest t = new ReportInsertTest();
		try {
			t.connect(hibernate);

			DBLayerReporting layer = new DBLayerReporting(t.session);

			t.session.beginTransaction();

			for (int i = 0; i < 100000; i++) {
				t.insert(layer, i, schedulerId);
			}

			t.session.commit();
		} catch (Exception e) {
			try {
				t.session.rollback();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		} finally {
			t.disconnect();
		}

	}

}
