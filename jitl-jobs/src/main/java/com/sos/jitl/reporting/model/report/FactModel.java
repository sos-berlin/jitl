package com.sos.jitl.reporting.model.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

import org.hibernate.Criteria;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.util.SOSString;

import com.sos.hibernate.classes.SOSHibernateBatchProcessor;
import com.sos.hibernate.classes.SOSHibernateConnection;
import com.sos.jitl.reporting.db.DBItemReportExecution;
import com.sos.jitl.reporting.db.DBItemReportTrigger;
import com.sos.jitl.reporting.db.DBItemSchedulerHistoryOrderStepReporting;
import com.sos.jitl.reporting.db.DBItemSchedulerVariableReporting;
import com.sos.jitl.reporting.helper.CounterRemove;
import com.sos.jitl.reporting.helper.CounterSynchronize;
import com.sos.jitl.reporting.helper.ReportUtil;
import com.sos.jitl.reporting.job.report.FactJobOptions;
import com.sos.jitl.reporting.model.IReportingModel;
import com.sos.jitl.reporting.model.ReportingModel;

public class FactModel extends ReportingModel implements IReportingModel {

	private Logger logger = LoggerFactory.getLogger(FactModel.class);

	private FactJobOptions options;
	private SOSHibernateConnection schedulerConnection;
	private DBItemSchedulerVariableReporting schedulerVariable;

	private CounterRemove counterRemove;
	private CounterSynchronize counterSynchronizeNew;
	private CounterSynchronize counterSynchronizeOld;

	private int maxHistoryAge;
	private int maxUncompletedAge;

	private Optional<Integer> largeResultFetchSizeReporting = Optional.empty();
	private Optional<Integer> largeResultFetchSizeScheduler = Optional.empty();

	public FactModel(SOSHibernateConnection reportingConn,
			SOSHibernateConnection schedulerConn, FactJobOptions opt)
			throws Exception {

		super(reportingConn);

		if (schedulerConn == null) {
			throw new Exception("schedulerConn is NULL");
		}
		schedulerConnection = schedulerConn;
		options = opt;

		try {
			int fetchSize = options.large_result_fetch_size.value();
			if (fetchSize != -1) {
				largeResultFetchSizeReporting = Optional.of(fetchSize);
			}
		} catch (Exception ex) {
		}
		
		try {
			int fetchSize = options.large_result_fetch_size_scheduler.value();
			if (fetchSize != -1) {
				largeResultFetchSizeScheduler = Optional.of(fetchSize);
			}
		} catch (Exception ex) {
		}

		maxHistoryAge = ReportUtil.resolveAge2Minutes(options.max_history_age
				.Value());
		maxUncompletedAge = ReportUtil
				.resolveAge2Minutes(options.max_uncompleted_age.Value());

	}

	/**
	 * @TODO if the scheduler connection is the same connection - open only 1
	 *       connection
	 */
	@Override
	public void process() throws Exception {
		String method = "process";

		Date dateTo = ReportUtil.getCurrentDateTime();
		Date dateFrom = null;
		DateTime start = new DateTime();
		ArrayList<String> schedulerIds = null;
		try {
			logger.info(String.format(
					"%s: batch_size = %s, large_result_fetch_size = %s",
					method, options.batch_size.value(),
					options.large_result_fetch_size.Value()));

			initCounters();
			initSynchronizing();

			dateFrom = getReportingDateFrom(dateTo);
			schedulerIds = getSchedulerSchedulerIds();

			removeReportingEntries(schedulerIds, dateFrom, dateTo);
			synchronizeSyncUncompletedEntries(schedulerIds, dateTo);
			synchronizeNewEntries(dateFrom, dateTo);

			finishSynchronizing(dateTo);
			logSummary(dateFrom, dateTo, start);

		} catch (Exception ex) {
			throw new Exception(String.format("%s: %s", method, ex.toString()),
					ex);
		}
	}

	private void removeReportingEntries(ArrayList<String> schedulerIds,
			Date dateFrom, Date dateTo) throws Exception {
		String method = "removeReportingEntries";
		try {
			logger.info(String.format("%s: dateFrom = %s, dateTo = %s", method,
					ReportUtil.getDateAsString(dateFrom),
					ReportUtil.getDateAsString(dateTo)));

			DateTime start = new DateTime();

			getDbLayer().getConnection().beginTransaction();
			int markedAsRemoved = getDbLayer().setReportingTriggersAsRemoved(
					schedulerIds, dateFrom, dateTo);
			getDbLayer().getConnection().commit();
			logger.info(String.format("%s: marked to remove triggers = %s",
					method, markedAsRemoved));

			getDbLayer().getConnection().beginTransaction();
			markedAsRemoved = getDbLayer().setReportingExecutionsAsRemoved();
			getDbLayer().getConnection().commit();
			logger.info(String.format("%s: marked to remove executions = %s ",
					method, markedAsRemoved));

			getDbLayer().getConnection().beginTransaction();
			counterRemove.setTriggerResults(getDbLayer()
					.removeReportingTriggerResults());
			getDbLayer().getConnection().commit();
			logger.info(String.format("%s: removed results = %s", method,
					counterRemove.getTriggerResults()));

			getDbLayer().getConnection().beginTransaction();
			counterRemove.setExecutionDates(getDbLayer()
					.removeReportingExecutionDates());
			getDbLayer().getConnection().commit();
			logger.info(String.format("%s: removed execution dates = %s",
					method, counterRemove.getExecutionDates()));

			getDbLayer().getConnection().beginTransaction();
			counterRemove.setTriggers(getDbLayer().removeReportingTriggers());
			getDbLayer().getConnection().commit();
			logger.info(String.format("%s: removed triggers = %s", method,
					counterRemove.getTriggers()));

			getDbLayer().getConnection().beginTransaction();
			counterRemove.setExecutions(getDbLayer()
					.removeReportingExecutions());
			getDbLayer().getConnection().commit();
			logger.info(String.format("%s: removed executions = %s", method,
					counterRemove.getExecutions()));

			logger.info(String.format("%s: duration = %s", method,
					ReportUtil.getDuration(start, new DateTime())));
		} catch (Exception ex) {
			getDbLayer().getConnection().rollback();
			throw new Exception(String.format("%s: %s", method, ex.toString()),
					ex);
		}
	}

	private void removeSyncUncompletedReportingEntries(ArrayList<Long> ids)
			throws Exception {
		String method = "removeSyncUncompletedReportingEntries";
		try {
			DateTime start = new DateTime();

			getDbLayer().getConnection().beginTransaction();

			int markedAsRemoved = getDbLayer().setReportingTriggersAsRemoved(
					ids);
			logger.info(String.format("%s: marked to remove triggers = %s",
					method, markedAsRemoved));

			markedAsRemoved = getDbLayer().setReportingExecutionsAsRemoved();
			logger.info(String.format("%s: marked to remove executions = %s ",
					method, markedAsRemoved));

			counterRemove.setTriggerResults(getDbLayer()
					.removeReportingTriggerResults());
			logger.info(String.format("%s: removed results = %s", method,
					counterRemove.getTriggerResults()));

			counterRemove.setExecutionDates(getDbLayer()
					.removeReportingExecutionDates());
			logger.info(String.format("%s: removed execution dates = %s",
					method, counterRemove.getExecutionDates()));

			counterRemove.setTriggers(getDbLayer().removeReportingTriggers());
			logger.info(String.format("%s: removed triggers = %s", method,
					counterRemove.getTriggers()));

			counterRemove.setExecutions(getDbLayer()
					.removeReportingExecutions());
			getDbLayer().getConnection().commit();

			logger.info(String.format("%s: removed executions = %s", method,
					counterRemove.getExecutions()));
			logger.info(String.format("%s: duration = %s", method,
					ReportUtil.getDuration(start, new DateTime())));
		} catch (Exception ex) {
			getDbLayer().getConnection().rollback();
			throw new Exception(String.format("%s: %s", method, ex.toString()),
					ex);
		}
	}

	private void finishSynchronizing(Date dateTo) throws Exception {
		String method = "finishSynchronizing";
		try {
			logger.info(String.format("%s: dateTo = %s", method,
					ReportUtil.getDateAsString(dateTo)));

			schedulerConnection.beginTransaction();
			schedulerVariable.setNumericValue(new Long(maxHistoryAge));
			schedulerVariable.setTextValue(ReportUtil.getDateAsString(dateTo));
			getDbLayer().updateSchedulerVariable(schedulerConnection,
					schedulerVariable);
			schedulerConnection.commit();

		} catch (Exception ex) {
			schedulerConnection.rollback();
			throw new Exception(String.format("%s: %s", method, ex.toString()),
					ex);
		}
	}

	private void initSynchronizing() throws Exception {
		String method = "initSynchronizing";
		try {
			logger.info(String.format("%s", method));

			schedulerConnection.beginTransaction();
			schedulerVariable = getDbLayer().getSchedulerVariabe(
					schedulerConnection);
			if (schedulerVariable == null) {
				schedulerVariable = getDbLayer().createSchedulerVariable(
						schedulerConnection, new Long(maxHistoryAge), null);
			}
			schedulerConnection.commit();

		} catch (Exception ex) {
			schedulerConnection.rollback();
			throw new Exception(String.format("%s: %s", method, ex.toString()),
					ex);
		}
	}

	private void synchronizeSyncUncompletedEntries(
			ArrayList<String> schedulerIds, Date dateTo) throws Exception {
		String method = "synchronizeSyncUncompletedEntries";

		ScrollableResults sr = null;
		try {
			logger.info(String.format("%s", method));

			if (schedulerIds != null && schedulerIds.size() > 0) {
				ArrayList<Long> ids = new ArrayList<Long>();
				ArrayList<Long> historyIds = new ArrayList<Long>();
				Criteria cr = getDbLayer()
						.getSyncUncomplitedReportTriggerAndHistoryIds(
								largeResultFetchSizeReporting, schedulerIds);
				sr = cr.scroll(ScrollMode.FORWARD_ONLY);
				while (sr.next()) {
					ids.add((Long) sr.get(0));
					historyIds.add((Long) sr.get(1));
				}
				sr.close();
				sr = null;

				if (ids != null && ids.size() > 0) {
					removeSyncUncompletedReportingEntries(ids);

					cr = getDbLayer().getSchedulerHistorySteps(
							schedulerConnection, largeResultFetchSizeScheduler,
							null, null, historyIds);
					synchronize(cr, "uncompleted", dateTo);
				}
			}
		} catch (Exception ex) {
			throw new Exception(String.format("%s: %s", method, ex.toString()),
					ex);
		} finally {
			if (sr != null) {
				try {
					sr.close();
				} catch (Exception ex) {
				}
			}
		}
	}

	private ArrayList<String> getSchedulerSchedulerIds() throws Exception {
		String method = "getSchedulerSchedulerIds";
		ScrollableResults sr = null;
		try {
			logger.info(String.format("%s", method));

			ArrayList<String> result = new ArrayList<String>();
			Criteria cr = getDbLayer().getSchedulerInstancesSchedulerIds(
					schedulerConnection, largeResultFetchSizeScheduler);
			sr = cr.scroll(ScrollMode.FORWARD_ONLY);
			while (sr.next()) {
				result.add((String) sr.get(0));
			}
			sr.close();
			sr = null;

			return result;
		} catch (Exception ex) {
			Throwable e = SOSHibernateConnection.getException(ex);
			throw new Exception(String.format("%s: %s", method, e.toString()),
					e);
		} finally {
			if (sr != null) {
				try {
					sr.close();
				} catch (Exception ex) {
				}
			}
		}
	}

	private ArrayList<Long> getReportingSyncUncomplitedHistoryIds(
			ArrayList<String> schedulerIds) throws Exception {
		String method = "getReportingSyncUncomplitedHistoryIds";
		ScrollableResults sr = null;
		try {
			logger.debug(String.format("%s", method));

			ArrayList<Long> result = new ArrayList<Long>();
			Criteria cr = getDbLayer()
					.getSyncUncomplitedReportTriggerHistoryIds(
							largeResultFetchSizeReporting, schedulerIds);
			sr = cr.scroll(ScrollMode.FORWARD_ONLY);
			while (sr.next()) {
				result.add((Long) sr.get(0));
			}
			sr.close();
			sr = null;

			return result;
		} catch (Exception ex) {
			Throwable e = SOSHibernateConnection.getException(ex);
			throw new Exception(String.format("%s: %s", method, e.toString()),
					e);
		} finally {
			if (sr != null) {
				try {
					sr.close();
				} catch (Exception ex) {
				}
			}
		}
	}

	private void synchronizeNewEntries(Date dateFrom, Date dateTo)
			throws Exception {
		String method = "synchronizeNewEntries";
		try {
			logger.info(String.format("%s: dateFrom = %s, dateTo = %s", method,
					ReportUtil.getDateAsString(dateFrom),
					ReportUtil.getDateAsString(dateTo)));

			Criteria cr = getDbLayer().getSchedulerHistorySteps(
					schedulerConnection, largeResultFetchSizeScheduler,
					dateFrom, dateTo, null);
			synchronize(cr, "new_entries", dateTo);
		} catch (Exception ex) {
			throw new Exception(String.format("%s: %s", method, ex.toString()),
					ex);
		}
	}

	private void synchronize(Criteria criteria, String range, Date dateTo)
			throws Exception {
		String method = "synchronize";

		ScrollableResults sr = null;
		SOSHibernateBatchProcessor bp = new SOSHibernateBatchProcessor(
				getDbLayer().getConnection());
		try {
			logger.debug(String.format("%s: range = %s", method, range));
			DateTime start = new DateTime();

			bp.createInsertBatch(DBItemReportExecution.class);

			HashMap<Long, Long> inserted = new HashMap<Long, Long>();

			int countTotal = 0;
			int countSkip = 0;
			int countTriggers = 0;
			int countExecutions = 0;
			int countBatchExecutions = 0;

			getDbLayer().getConnection().beginTransaction();

			sr = criteria.scroll(ScrollMode.FORWARD_ONLY);
			while (sr.next()) {
				countTotal++;
				DBItemSchedulerHistoryOrderStepReporting step = (DBItemSchedulerHistoryOrderStepReporting) sr
						.get(0);

				if (step.getOrderHistoryId() == null
						&& step.getOrderId() == null
						&& step.getOrderStartTime() == null) {
					countSkip++;
					logger.debug(String
							.format("%s: %s) order object is null. step = %s, historyId = %s ",
									method, countTotal, step.getStepState(),
									step.getStepHistoryId()));

					continue;
				}
				if (step.getTaskId() == null && step.getTaskJobName() == null
						&& step.getTaskCause() == null) {
					countSkip++;
					logger.debug(String
							.format("%s: %s) task object is null. jobChain = %s, order = %s, step = %s, taskId = %s ",
									method, countTotal,
									step.getOrderJobChain(), step.getOrderId(),
									step.getStepState(), step.getStepTaskId()));
					continue;
				}

				logger.debug(String
						.format("%s: %s) schedulerId = %s, orderHistoryId = %s, jobChain = %s, order id = %s, step = %s, step state = %s",
								method, countTotal, step.getOrderSchedulerId(),
								step.getOrderHistoryId(),
								step.getOrderJobChain(), step.getOrderId(),
								step.getStepStep(), step.getStepState()));

				Long triggerId = new Long(0);
				try {
					// getDbLayer().getConnection().beginTransaction();

					if (countTotal % options.batch_size.value() == 0) {
						countBatchExecutions += ReportUtil.getBatchSize(bp
								.executeBatch());
					}

					if (inserted.containsKey(step.getOrderHistoryId())) {
						triggerId = inserted.get(step.getOrderHistoryId());
					} else {
						boolean syncCompleted = false;
						if (step.getOrderEndTime() == null) {
							if (maxUncompletedAge > 0) {
								Long startTimeMinutes = step
										.getOrderStartTime().getTime() / 1000 / 60;
								Long endTimeMinutes = dateTo.getTime() / 1000 / 60;
								Long diffMinutes = endTimeMinutes
										- startTimeMinutes;
								if (diffMinutes > maxUncompletedAge) {
									syncCompleted = true;
								}
							}

						} else {
							syncCompleted = true;
						}

						DBItemReportTrigger rt = getDbLayer()
								.createReportTrigger(
										step.getOrderSchedulerId(),
										step.getOrderHistoryId(),
										step.getOrderId(),
										null,
										step.getOrderJobChain(),
										ReportUtil.getBasenameFromName(step
												.getOrderJobChain()), null,
										step.getOrderState(),
										step.getOrderStateText(),
										step.getOrderStartTime(),
										step.getOrderEndTime(), syncCompleted);

						countTriggers++;
						triggerId = rt.getId();
						inserted.put(step.getOrderHistoryId(), triggerId);
					}

					DBItemReportExecution re = createReportExecution(
							step.getOrderSchedulerId(),
							step.getOrderHistoryId(), triggerId,
							step.getStepStep(), step.getTaskJobName(),
							ReportUtil.getBasenameFromName(step
									.getTaskJobName()), null,
							step.getStepStartTime(), step.getStepEndTime(),
							step.getStepState(), step.getTaskCause(),
							step.isStepError(), step.getStepErrorCode(),
							step.getStepErrorText(), step.getAgentUrl());

					bp.addBatch(re);

					// getDbLayer().getConnection().commit();
					countExecutions++;
				} catch (Exception e) {
					// getDbLayer().getConnection().rollback();
					throw new Exception(SOSHibernateConnection.getException(e));
				}

				if (countTotal % options.log_info_step.value() == 0) {
					logger.info(String.format(
							"%s: %s history steps processed ...", method,
							options.log_info_step.value()));
				}

			}

			countBatchExecutions += ReportUtil.getBatchSize(bp.executeBatch());

			getDbLayer().getConnection().commit();

			logger.info(String.format("%s: duration = %s", method,
					ReportUtil.getDuration(start, new DateTime())));

			if (range.equals("uncompleted")) {
				counterSynchronizeOld.setTotal(countTotal);
				counterSynchronizeOld.setSkip(countSkip);
				counterSynchronizeOld.setTriggers(countTriggers);
				counterSynchronizeOld.setExecutions(countExecutions);
				counterSynchronizeOld.setExecutionsBatch(countBatchExecutions);

				logger.info(String
						.format("%s: total = %s, triggers = %s, executions = %s of %s, skip = %s ",
								method, counterSynchronizeOld.getTotal(),
								counterSynchronizeOld.getTriggers(),
								counterSynchronizeOld.getExecutionsBatch(),
								counterSynchronizeOld.getExecutions(),
								counterSynchronizeOld.getSkip()));
			} else if (range.equals("new_entries")) {
				counterSynchronizeNew.setTotal(countTotal);
				counterSynchronizeNew.setSkip(countSkip);
				counterSynchronizeNew.setTriggers(countTriggers);
				counterSynchronizeNew.setExecutions(countExecutions);
				counterSynchronizeNew.setExecutionsBatch(countBatchExecutions);

				logger.info(String
						.format("%s: total history steps = %s, triggers = %s, executions = %s of %s, skip = %s ",
								method, counterSynchronizeNew.getTotal(),
								counterSynchronizeNew.getTriggers(),
								counterSynchronizeNew.getExecutionsBatch(),
								counterSynchronizeNew.getExecutions(),
								counterSynchronizeNew.getSkip()));

			}
			// schedulerConnection.commit();

		} catch (Exception ex) {
			getDbLayer().getConnection().rollback();
			// schedulerConnection.rollback();
			Throwable e = SOSHibernateConnection.getException(ex);
			throw new Exception(String.format("%s: %s", method, e.toString()),
					e);
		} finally {
			bp.close();

			try {
				if (sr != null) {
					sr.close();
				}
			} catch (Exception ex) {
			}
		}

	}

	private void initCounters() throws Exception {
		counterSynchronizeNew = new CounterSynchronize();
		counterSynchronizeOld = new CounterSynchronize();
		counterRemove = new CounterRemove();
	}

	private void logSummary(Date dateFrom, Date dateTo, DateTime start)
			throws Exception {
		String method = "logSummary";

		String from = ReportUtil.getDateAsString(dateFrom);
		String to = ReportUtil.getDateAsString(dateTo);

		logger.info(String
				.format("%s: removed entries (%s to %s): triggers = %s, executions = %s",
						method, from, to, counterRemove.getTriggers(),
						counterRemove.getExecutions()));

		logger.info(String.format(
				"%s: removed results: results = %s, execution dates = %s",
				method, counterRemove.getTriggerResults(),
				counterRemove.getExecutionDates()));

		logger.info(String
				.format("%s: synchronized new entries (%s to %s): total history steps = %s, triggers = %s, executions = %s of %s, skip = %s ",
						method, from, to, counterSynchronizeNew.getTotal(),
						counterSynchronizeNew.getTriggers(),
						counterSynchronizeNew.getExecutionsBatch(),
						counterSynchronizeNew.getExecutions(),
						counterSynchronizeNew.getSkip()));

		logger.info(String
				.format("%s: synchronized old entries: total uncompleted triggers = %s, triggers = %s, executions = %s of %s, skip = %s ",
						method, counterSynchronizeOld.getTotal(),
						counterSynchronizeOld.getTriggers(),
						counterSynchronizeOld.getExecutionsBatch(),
						counterSynchronizeOld.getExecutions(),
						counterSynchronizeOld.getSkip()));

		logger.info(String.format("%s: duration = %s", method,
				ReportUtil.getDuration(start, new DateTime())));
	}

	private Date getReportingDateFrom(Date dateTo) throws Exception {
		String method = "getReportingDateFrom";

		Long currentMaxAge = new Long(maxHistoryAge);
		Long storedMaxAge = (schedulerVariable.getNumericValue() == null) ? new Long(
				0) : schedulerVariable.getNumericValue();
		Date dateFrom = SOSString.isEmpty(schedulerVariable.getTextValue()) ? null
				: ReportUtil
						.getDateFromString(schedulerVariable.getTextValue());

		logger.debug(String.format(
				"%s: currentMaxAge = %s, storedMaxAge = %s, dateFrom = %s",
				method, currentMaxAge, storedMaxAge,
				ReportUtil.getDateAsString(dateFrom)));

		if (dateFrom != null) {
			// if(!currentMaxAge.equals(storedMaxAge)){
			if (options.force_max_history_age.value()) {
				dateFrom = null;
			} else {
				if (currentMaxAge > 0) {
					Long startTimeMinutes = dateFrom.getTime() / 1000 / 60;
					Long endTimeMinutes = dateTo.getTime() / 1000 / 60;
					Long diffMinutes = endTimeMinutes - startTimeMinutes;
					if (diffMinutes > currentMaxAge) {
						dateFrom = null;
					}
					logger.debug(String
							.format("%s: diffMinutes = %s, currentMaxAge = %s, dateFrom = %s",
									method, diffMinutes, currentMaxAge,
									ReportUtil.getDateAsString(dateFrom)));

				}
			}
		}

		if (dateFrom == null && currentMaxAge > 0) {
			dateFrom = ReportUtil
					.getDateTimeMinusMinutes(dateTo, currentMaxAge);
			logger.debug(String.format(
					"%s: dateTo = %s - currentMaxAge = %s, dateFrom = %s",
					method, ReportUtil.getDateAsString(dateTo), currentMaxAge,
					ReportUtil.getDateAsString(dateFrom)));
		}

		logger.info(String
				.format("%s: dateFrom = %s (storedDateFrom = %s, max_history_age = %s (%s minutes), storedMaxAge = %s minutes)",
						method, ReportUtil.getDateAsString(dateFrom),
						schedulerVariable.getTextValue(),
						options.max_history_age.Value(), currentMaxAge,
						storedMaxAge));
		return dateFrom;
	}

	private DBItemReportExecution createReportExecution(String schedulerId,
			Long historyId, Long triggerId, Long step, String name,
			String basename, String title, Date startTime, Date endTime,
			String state, String cause, boolean error, String errorCode,
			String errorText, String agentUrl) throws Exception {

		DBItemReportExecution item = new DBItemReportExecution();
		item.setSchedulerId(schedulerId);
		item.setHistoryId(historyId);
		item.setTriggerId(triggerId);
		item.setStep(step);
		item.setName(name);
		item.setBasename(basename);
		item.setTitle(title);
		item.setStartTime(startTime);
		item.setEndTime(endTime);
		item.setState(state);
		item.setCause(cause);
		item.setError(error);
		item.setErrorCode(errorCode);
		item.setErrorText(errorText);
		item.setAgentUrl(agentUrl);
		item.setIsRuntimeDefined(false);
		item.setSuspended(false);

		item.setCreated(ReportUtil.getCurrentDateTime());
		item.setModified(ReportUtil.getCurrentDateTime());
		return item;
	}

	public CounterSynchronize getCounterSynchronizeNew() {
		return counterSynchronizeNew;
	}

	public CounterSynchronize getCounterSynchronizeOld() {
		return counterSynchronizeOld;
	}

}
