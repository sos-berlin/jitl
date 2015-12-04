package com.sos.jitl.checkrunhistory;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import sos.util.SOSDate;

public class JobHistoryHelper {

	public String getDuration(LocalDateTime parStart, LocalDateTime parEnd) {
		
 		Instant instant = parStart.toInstant(ZoneOffset.UTC);
		Date start = Date.from(instant);
		instant = parEnd.toInstant(ZoneOffset.UTC);
 		Date end = Date.from(instant);
		
		if (start == null || end == null) {
			return "";
		} else {
			Calendar cal_1 = new GregorianCalendar();
			Calendar cal_2 = new GregorianCalendar();
 
			cal_1.setTime(start);
			cal_2.setTime(end);

			long time = cal_2.getTime().getTime() - cal_1.getTime().getTime();

			long millis = time % 1000;
			time /= 1000;
			long seconds = time % 60;
			time /= 60;
			long minutes = time % 60;
			time /= 60;
			long hours = time % 24;
			time /= 24;
			long days = time;

			Calendar calendar = GregorianCalendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, (int) hours);
			calendar.set(Calendar.MINUTE, (int) minutes);
			calendar.set(Calendar.SECOND, (int) seconds);

			SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
			String d = "";

			if (days > 0) {
				d = String.format(
						"%sd " + formatter.format(calendar.getTime()), days);
			} else {
				d = formatter.format(calendar.getTime());
			}

			return d;
		}
	}

	public String getOrderId(String jobChainAndOrder) {
		return getParameter(jobChainAndOrder);
	}

	public String getJobChainName(String jobChainAndOrder) {
		return getMethodName(jobChainAndOrder);
	}

	public String getParameter(String p) {
		p = p.trim();
		String s = "";

		Pattern pattern = Pattern.compile("^.*\\(([^\\)]*)\\)$", Pattern.DOTALL
				+ Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(p);

		if (matcher.find()) {
			s = matcher.group(1).trim();
		}
		return s;
	}

	public String getMethodName(String p) {
		p = p.trim();
		String s = p;

		Pattern pattern = Pattern.compile("^([^\\(]*)\\(.*\\)$", Pattern.DOTALL
				+ Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(p);

		if (matcher.find()) {
			s = matcher.group(1).trim();
		}

		return s.trim();
	}

	protected boolean isAfter(LocalDateTime timeToTest, String time) {
		if (time.length() == 8) {
			time = "0:" + time;
		}

		if (timeToTest == null) {
			return false;
		}

		JobSchedulerCheckRunHistoryOptions options = new JobSchedulerCheckRunHistoryOptions();
		options.start_time.Value(time);

		ZonedDateTime zdt = ZonedDateTime.of(timeToTest, ZoneId.systemDefault());
		GregorianCalendar cal = GregorianCalendar.from(zdt);
 		
		DateTime limit = new DateTime(options.start_time.getDateObject());
		DateTime ended = new DateTime(cal.getTime());
		return limit.toLocalDateTime().isBefore(ended.toLocalDateTime());
	}

	protected boolean isToday(LocalDateTime d) {
		LocalDateTime today = LocalDateTime.now();
		if (d == null) {
			return false;
		} else {
 			return (today.getDayOfYear() == d.getDayOfYear());
		}
	}

	public String getTime(String defaultValue, String p) {
		String param = getParameter(p);
		if (param.length() == 0) {
			param = defaultValue;
		}
		return param;
	}

	protected int big2int(BigInteger b) {
		if (b == null) {
			return -1;
		} else {
			return b.intValue();
		}
	}

	protected LocalDateTime getDateFromString(String inDateTime) throws Exception {
		LocalDateTime dateResult = null;
		Date date=null;
		
		if (inDateTime != null) {
			if (inDateTime.endsWith("Z")) {
				DateTimeFormatter dateTimeFormatter = DateTimeFormat
						.forPattern("yyyy-MM-dd'T'H:mm:ss.SSSZ");
				DateTime dateTime = dateTimeFormatter.parseDateTime(inDateTime
						.replaceFirst("Z", "+00:00"));
				date = dateTime.toDate();
			} else {
				date = SOSDate
						.getDate(inDateTime, SOSDate.dateTimeFormat);
			}
		}
		dateResult = LocalDateTime.ofInstant(date.toInstant(), java.time.ZoneId.systemDefault() );
		return dateResult;
	}

	public boolean isInTimeLimit(String timeLimit, String endTime) {
		if (timeLimit.equals("")) {
			return true;
		}

		String localTimeLimit = timeLimit;
		if (!timeLimit.contains("..")) {
			localTimeLimit = ".." + localTimeLimit;
		}

		String from = localTimeLimit.replaceAll("^(.*)\\.\\..*$", "$1");
		String to = localTimeLimit.replaceAll("^.*\\.\\.(.*)$", "$1");

		if (from.equals("")) {
			from = "00:00:00";
		}

		if (from.length() == 8) {
			from = "0:" + from;
		}
		if (to.length() == 8) {
			to = "0:" + to;
		}

		JobSchedulerCheckRunHistoryOptions options = new JobSchedulerCheckRunHistoryOptions();
		options.start_time.Value(from);
		options.end_time.Value(to);

		if (to.equals("")) {
			DateTime fromDate = new DateTime(options.start_time.getDateObject());
			DateTime ended = new DateTime(endTime);
			DateTime toDate = ended;
			return ((ended.toLocalDateTime().isEqual(toDate.toLocalDateTime()) || ended
					.toLocalDateTime().isBefore(toDate.toLocalDateTime())) && (ended
					.toLocalDateTime().isEqual(fromDate.toLocalDateTime()) || ended
					.toLocalDateTime().isAfter(fromDate.toLocalDateTime())));
		} else {
			DateTime fromDate = new DateTime(options.start_time.getDateObject());
			DateTime ended = new DateTime(endTime);
			DateTime toDate = new DateTime(options.end_time.getDateObject());
			return ((ended.toLocalDateTime().isEqual(toDate.toLocalDateTime()) || ended
					.toLocalDateTime().isBefore(toDate.toLocalDateTime())) && (ended
					.toLocalDateTime().isEqual(fromDate.toLocalDateTime()) || ended
					.toLocalDateTime().isAfter(fromDate.toLocalDateTime())));
		}
	}

}
