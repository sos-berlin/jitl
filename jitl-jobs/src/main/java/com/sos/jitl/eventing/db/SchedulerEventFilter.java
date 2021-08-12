package com.sos.jitl.eventing.db;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.sos.hibernate.classes.DbItem;
import com.sos.hibernate.classes.SOSHibernateIntervalFilter;
import com.sos.hibernate.classes.UtcTimeHelper;
import com.sos.hibernate.interfaces.ISOSHibernateFilter;
import com.sos.jitl.reporting.helper.ReportUtil;
import com.sos.joc.model.job.JobPath;
import com.sos.joc.model.order.OrderPath;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.util.SOSDate;

public class SchedulerEventFilter extends SOSHibernateIntervalFilter implements ISOSHibernateFilter {

	private static final String EXPIRES_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private static final String NEVER_DATE = "2999-01-01 00:00:00";
	private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerEventFilter.class);
	private int limit = 0;
	private String filterTimezone;

	public void setFilterTimezone(String filterTimezone) {
		this.filterTimezone = filterTimezone;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	private String remoteUrl;
	private String remoteSchedulerHost;
	private String conditon;
	private Integer remoteSchedulerPort;
	private String jobChain;
	private String orderId;
	private String jobName;
	private String eventClass;
	private String eventId;
	private Integer exitCode;
	private Calendar expirationDate;
	private Date created;
	private String expirationPeriod = "24:00:00";
	private String expirationCycle = "";
	private Date expiresFrom;
	private Date expiresTo;
	private String parametersAsString;
	private String schedulerId = "";
	private boolean schedulerIdEmpty = false;
	private List<String> listOfEventClasses;
	private List<String> listOfEventIds;
	private List<Integer> listOfExitCodes;
	private List<OrderPath> listOfOrders;
	private List<JobPath> listOfJobs;
	private List<Long> listOfIds;

	public SchedulerEventFilter() {
		super();
		resetFilter();
	}

	public boolean hasEventClasses() {
		return listOfEventClasses != null && !listOfEventClasses.isEmpty();
	}

	public boolean hasIds() {
		return listOfIds != null && !listOfIds.isEmpty();
	}

	public boolean hasEventIds() {
		return listOfEventIds != null && !listOfEventIds.isEmpty();
	}

	public boolean hasExitCodes() {
		return listOfExitCodes != null && !listOfExitCodes.isEmpty();
	}

	public boolean hasOrders() {
		return listOfOrders != null && !listOfOrders.isEmpty();
	}

	public boolean hasJobs() {
		return listOfJobs != null && !listOfJobs.isEmpty();
	}

	@Override
	public String getTitle() {
		String s = "";
		if (remoteSchedulerHost != null && !"".equals(remoteSchedulerHost)) {
			s += String.format("RemoteScheduler: %s:%s ", remoteSchedulerHost, remoteSchedulerPort);
		}
		if (schedulerId != null && !"".equals(schedulerId)) {
			s += String.format("Scheduler Id: %s ", schedulerId);
		}
		if (jobChain != null && !"".equals(jobChain)) {
			s += String.format("JobChain: %s ", jobChain);
		}
		if (jobName != null && !"".equals(jobName)) {
			s += String.format("JobName: %s ", jobName);
		}
		if (eventClass != null && !"".equals(eventClass)) {
			s += String.format("Class: %s ", eventClass);
		}
		if (eventId != null && !"".equals(eventId)) {
			s += String.format("Id: %s ", eventId);
		}
		if (exitCode != null) {
			s += String.format("Exit: %s ", exitCode);
		}
		return String.format("%1s", s);
	}

	public String getUtcTimeAsString(String expiresLocal) {
		if ("UTC".equalsIgnoreCase(this.filterTimezone)) {
			return expiresLocal;
		} else {

			String fromTimeZoneString = DateTimeZone.getDefault().getID();
			String toTimeZoneString = "UTC";

			DateTimeFormatter formatter = DateTimeFormat.forPattern(EXPIRES_DATE_FORMAT);
			DateTime now = formatter.parseDateTime(expiresLocal);

			DateTimeZone fromZone = DateTimeZone.forID(fromTimeZoneString);
			String expiresUtc = UtcTimeHelper.convertTimeZonesToString(EXPIRES_DATE_FORMAT, fromTimeZoneString,
					toTimeZoneString, now.withZone(fromZone));
			return expiresUtc;
		}
	}

	public Calendar calculateExpirationDate() throws Exception {
		Calendar cal = Calendar.getInstance();
		try {
			String nowLocal = SOSDate.getCurrentTimeAsString();
			String nowUtc = getUtcTimeAsString(nowLocal);
			DateFormat format = new SimpleDateFormat(EXPIRES_DATE_FORMAT);
			Date now = format.parse(nowUtc);
			cal.setTime(now);
			if (expirationCycle.indexOf(":") > -1) {
				String[] timeArray = expirationCycle.split(":");
				int hours = Integer.parseInt(timeArray[0]);
				int minutes = Integer.parseInt(timeArray[1]);
				int seconds = 0;
				if (timeArray.length > 2) {
					seconds = Integer.parseInt(timeArray[2]);
				}
				cal.set(Calendar.HOUR_OF_DAY, hours);
				cal.set(Calendar.MINUTE, minutes);
				cal.set(Calendar.SECOND, seconds);
				Calendar calNow = Calendar.getInstance();
				calNow.setTime(now);
				if (calNow.after(cal)) {
					cal.add(Calendar.DAY_OF_MONTH, 1);
				}
			} else if (expirationPeriod.indexOf(":") > -1) {
				String[] timeArray = expirationPeriod.split(":");
				int hours = Integer.parseInt(timeArray[0]);
				int minutes = Integer.parseInt(timeArray[1]);
				int seconds = 0;
				if (timeArray.length > 2) {
					seconds = Integer.parseInt(timeArray[2]);
				}
				if (hours > 0) {
					cal.add(Calendar.HOUR_OF_DAY, hours);
				}
				if (minutes > 0) {
					cal.add(Calendar.MINUTE, minutes);
				}
				if (seconds > 0) {
					cal.add(Calendar.SECOND, seconds);
				}
			} else if (!expirationPeriod.isEmpty()) {
				cal.add(Calendar.SECOND, Integer.parseInt(expirationPeriod));
			}
		} catch (Exception e) {
			throw e;
		}
		return cal;
	}

	@Override
	public void setIntervalFromDate(Date d) {
		this.expiresFrom = d;
	}

	public void setExpiresFrom(Date d) {
		this.expiresFrom = d;
	}

	@Override
	public void setIntervalToDate(Date d) {
		this.expiresTo = d;
	}

	public void setExpiresTo(Date d) {
		this.expiresTo = d;
	}

	public Date getExpiresTo() {
		return this.expiresTo;
	}

	public Date getExpires() {
		if (this.expiresTo == null) {
			this.expiresTo = expirationDate.getTime();
		}
		return this.expiresTo;
	}

	public Date getExpiresFrom() {
		return this.expiresFrom;
	}

	@Override
	public void setIntervalFromDateIso(String s) {
	}

	@Override
	public void setIntervalToDateIso(String s) {
	}

	public void setExpires(Date d) {
		this.expiresTo = d;
		this.expiresFrom = null;
	}

	public String getRemoteSchedulerHost() {
		return remoteSchedulerHost;
	}

	public void setRemoteSchedulerHost(String remoteSchedulerHost) {
		this.remoteSchedulerHost = remoteSchedulerHost;
	}

	public Integer getRemoteSchedulerPort() {
		return remoteSchedulerPort;
	}

	public void setRemoteSchedulerPort(Integer remoteSchedulerPort) {
		this.remoteSchedulerPort = remoteSchedulerPort;
	}

	public void setRemoteSchedulerPort(String remoteSchedulerPort) {
		try {
			this.remoteSchedulerPort = Integer.parseInt(remoteSchedulerPort);
		} catch (NumberFormatException e) {
			this.remoteSchedulerPort = null;
		}
	}

	public String getJobChain() {
		return jobChain;
	}

	public void setJobChain(String jobChain) {
		this.jobChain = jobChain;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getEventClass() {
		return eventClass;
	}

	public void setEventClass(String eventClass) {
		this.eventClass = eventClass;
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public Integer getExitCode() {
		return exitCode;
	}

	public void setExitCode(Integer exitCode) {
		this.exitCode = exitCode;
	}

	public String getSchedulerId() {
		return schedulerId;
	}

	public void setSchedulerId(String schedulerId) {
		this.schedulerId = schedulerId;
	}

	public String getConditon() {
		return conditon;
	}

	public void setConditon(String conditon) {
		this.conditon = conditon;
	}

	public String getRemoteUrl() {
		return remoteUrl;
	}

	public void setRemoteUrl(String remoteUrl) {
		this.remoteUrl = remoteUrl;
	}

	public void setSchedulerIdEmpty(boolean schedulerIdEmpty) {
		this.schedulerIdEmpty = schedulerIdEmpty;
	}

	public boolean isSchedulerIdEmpty() {
		return schedulerIdEmpty;
	}

	public void setEventClasses(List<String> eventClasses) {
		listOfEventClasses = eventClasses;
	}

	public void setExitCodes(List<Integer> exitCodes) {
		listOfExitCodes = exitCodes;
	}

	public void setEventIds(List<String> eventIds) {
		listOfEventIds = eventIds;
	}

	public void setOrders(List<OrderPath> orders) {
		listOfOrders = orders;
	}

	public void setJobs(List<JobPath> jobs) {
		listOfJobs = jobs;
	}

	public List<String> getListOfEventClasses() {
		return listOfEventClasses;
	}

	public List<String> getListOfEventIds() {
		return listOfEventIds;
	}

	public List<Integer> getListOfExitCodes() {
		return listOfExitCodes;
	}

	public List<OrderPath> getListOfOrders() {
		return listOfOrders;
	}

	public List<JobPath> getListOfJobs() {
		return listOfJobs;
	}

	public List<String> getListOfJobNames() {
		ArrayList <String> l = new ArrayList<String>();
		for (JobPath job: listOfJobs) {
			l.add(job.getJob());
		};
		return l;
	}

	
	public List<Long> getListOfIds() {
		return listOfIds;
	}

	public void setIds(List<Long> ids) {
		this.listOfIds = ids;
	}

	@Override
	public boolean isFiltered(DbItem h) {
		return false;
	}

	public Calendar getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationPeriod(String expirationPeriod) throws Exception {
		this.expirationPeriod = expirationPeriod;
		expirationDate = calculateExpirationDate();
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public void setExpirationCycle(String expirationCycle) throws Exception {
		this.expirationCycle = expirationCycle;
		expirationDate = calculateExpirationDate();
	}

	public String getParametersAsString() {
		return parametersAsString;
	}

	public void setParametersAsString(String parametersAsString) {
		this.parametersAsString = parametersAsString;
	}

	public void setExitCode(String exitCode) {
		try {
			this.exitCode = Integer.parseInt(exitCode);
		} catch (NumberFormatException e) {
			LOGGER.warn("could not set exitCode: " + exitCode);
		}

	}

	public void setExpires(String expires) throws Exception {
		if (expires != null && !expires.isEmpty()) {
			if ("never".equalsIgnoreCase(expires)) {
				setExpires(ReportUtil.getDateFromString(NEVER_DATE));
			} else {
				if ("now_utc".equalsIgnoreCase(expires)) {
					String nowLocal = SOSDate.getCurrentTimeAsString();
					String nowUtc = getUtcTimeAsString(nowLocal);
					DateFormat format = new SimpleDateFormat(EXPIRES_DATE_FORMAT);
					Date now = format.parse(nowUtc);
					setExpires(now);
				} else {
			        expires = expires.replaceAll("T"," ").replaceAll(".000","");
					Date utcDate = SOSDate.getTime(expires, EXPIRES_DATE_FORMAT);
					setExpires(utcDate);
				}
			}
			LOGGER.debug(".. parameter [expires]: " + expires);
			LOGGER.debug(".. --> eventExpires:" + getExpires());
		}
	}

	public void resetFilter() {
		this.setDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
		this.setOrderCriteria("id");
		this.setSortMode("desc");
		this.limit = 0;
	}
}