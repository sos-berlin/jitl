package com.sos.jitl.latecomers.classes;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sos.jitl.latecomers.JobSchedulerStartLatecomersOptions;

public class LateComersHelper {
	JobSchedulerStartLatecomersOptions jobSchedulerStartLatecomersOptions;

	public LateComersHelper(JobSchedulerStartLatecomersOptions jobSchedulerStartLatecomersOptions) {
		super();
		this.jobSchedulerStartLatecomersOptions = jobSchedulerStartLatecomersOptions;
	}

	public String getParent(String path) {
		Path p = Paths.get(path).getParent();
		if (p == null) {
			return null;
		} else {
			return p.toString().replace('\\', '/');
		}
	}

	public boolean ignoreOrder(String jobChain, String orderId) {
		Matcher regExOrderMatcher = null;
		boolean ignore = false;
		String s;
		if (orderId != null && !orderId.isEmpty() && jobChain != null && !jobChain.isEmpty()) {
			s = jobChain + "," + orderId;
		} else {
			s = jobChain;
		}

		if (s != null && !s.isEmpty() && jobSchedulerStartLatecomersOptions.ignoreOrderList.isDirty()) {
			String[] ignoreOrders = jobSchedulerStartLatecomersOptions.getIgnoreOrderList().getValue().split(";");
			for (String o : ignoreOrders) {
				regExOrderMatcher = Pattern.compile(o).matcher("");
				if (!ignore) {
					ignore = regExOrderMatcher.reset(s).find();
					if (ignore) {
						break;
					}
				}
			}
		}
		return ignore;
	}

	public boolean ignoreJob(String job) {
		Matcher regExJobMatcher = null;
		boolean ignore = false;

		if (job != null && !job.isEmpty() && jobSchedulerStartLatecomersOptions.ignoreJobList.isDirty()) {
			String[] ignoreJobs = jobSchedulerStartLatecomersOptions.getIgnoreJobList().getValue().split(";");
			for (String j : ignoreJobs) {
				regExJobMatcher = Pattern.compile(j).matcher("");
				if (!ignore) {
					ignore = regExJobMatcher.reset(job).find();
					if (ignore) {
						break;
					}
				}
			}
		}
		return ignore;

	}

	public boolean ignoreFolder(String folderPath) {
		boolean ignore = false;

		if (folderPath != null && !folderPath.isEmpty()
				&& jobSchedulerStartLatecomersOptions.ignoreFolderList.isDirty()) {
			String[] ignoreFolders = jobSchedulerStartLatecomersOptions.getIgnoreFolderList().getValue().split(";");
			for (String f : ignoreFolders) {
				if (f.endsWith("/")) {
					f = f.substring(0, f.length() - 1);
				}

				if (f.endsWith("*")) {
					f = f.substring(0, f.length() - 1);
					ignore = folderPath.startsWith(f);
				} else {
					ignore = folderPath.equals(f);
				}
				if (ignore) {
					break;
				}
			}
		}
		return ignore;
	}

	public boolean considerJob(String job) {
		if (jobSchedulerStartLatecomersOptions.jobs.isDirty()) {
			Matcher regExJobMatcher = null;
			boolean consider = false;

			if (job != null && !job.isEmpty()) {
				String[] considerJobs = jobSchedulerStartLatecomersOptions.getJobs().getValue().split(";");
				for (String j : considerJobs) {
					regExJobMatcher = Pattern.compile(j).matcher("");
					if (!consider) {
						consider = regExJobMatcher.reset(job).find();
						if (consider) {
							break;
						}
					}
				}
			}
			return consider;
		} else {
			return (job != null && !job.isEmpty()) ;
		}
	}

	public boolean considerOrder(String jobChain, String orderId) {
		if (jobSchedulerStartLatecomersOptions.orders.isDirty()) {
			Matcher regExOrderMatcher = null;
			boolean consider = false;
			
			String s;
			if (orderId != null && !orderId.isEmpty() && jobChain != null && !jobChain.isEmpty()) {
				s = jobChain + "," + orderId;
			} else {
				s = jobChain;
			}
			
			if (s != null && !s.isEmpty()) {
				String[] considerOrders = jobSchedulerStartLatecomersOptions.getOrders().getValue().split(";");
				for (String o : considerOrders) {
					regExOrderMatcher = Pattern.compile(o).matcher("");
					if (!consider) {
						consider = regExOrderMatcher.reset(s).find();
						if (consider) {
							break;
						}
					}
				}
			}
			return consider;
		} else {
			return (jobChain != null && !jobChain.isEmpty());
		}
	}
}
