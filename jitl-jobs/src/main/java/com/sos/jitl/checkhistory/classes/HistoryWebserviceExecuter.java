package com.sos.jitl.checkhistory.classes;

import java.math.BigInteger;
import java.net.URI;
import javax.json.JsonObject;
import com.sos.jitl.checkhistory.HistoryHelper;
import com.sos.jitl.restclient.WebserviceExecuter;
import com.sos.joc.model.common.HistoryState;
import com.sos.joc.model.common.HistoryStateText;
import com.sos.joc.model.order.OrderHistoryItem;
import com.sos.scheduler.model.answers.HistoryEntry;
import com.sos.scheduler.model.answers.JobChain.OrderHistory.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HistoryWebserviceExecuter extends WebserviceExecuter {

	private static final String JOB_STRING_FOR_WEBSERVICE = "{'jobschedulerId':'%s','limit':1,'jobs':[{'job':'%s'}],'historyStates':";
	private static final String JOB_CHAIN_ORDER_STRING_FOR_WEBSERVICE = "{'jobschedulerId':'%s','limit':1,'orders':[{'jobChain':'%s','orderId':'%s'}],'historyStates':";
	private static final String JOB_CHAIN_STRING_FOR_WEBSERVICE = "{'jobschedulerId':'%s','limit':1,'orders':[{'jobChain':'%s'}],'historyStates':";
	private static final String JOB_CHAIN_ORDER_HISTORY_STRING_FOR_WEBSERVICE = "{'jobschedulerId':'%s','historyIds':['%s']}";

	private static final Logger LOGGER = LoggerFactory.getLogger(HistoryWebserviceExecuter.class);
	private HistoryHelper historyHelper;
	private String schedulerId;
	private String timeLimit = "";
	private String jobName;
	private String jobChainName;
	private String orderId;

	public HistoryWebserviceExecuter(String jocUrl, String jocAccount) {
		super(jocUrl, jocAccount);
		historyHelper = new HistoryHelper();
	}

	public HistoryWebserviceExecuter(String jocUrl) {
		super(jocUrl);
		historyHelper = new HistoryHelper();
	}

	private HistoryEntry json2HistoryEntry(String answer) throws Exception {
		HistoryEntry historyEntry = new HistoryEntry();
		JsonObject history = jsonFromString(answer);
		if (history.getJsonArray("history") != null && history.getJsonArray("history").size() > 0) {
			JsonObject entry = history.getJsonArray("history").getJsonObject(0);
			if (entry != null) {
				JsonObject error = entry.getJsonObject("error");

				if (error != null) {
					historyEntry.setError(BigInteger.valueOf(1));
					historyEntry.setErrorCode(error.getString("code", ""));
					historyEntry.setErrorText(error.getString("message", ""));
				} else {
					historyEntry.setError(BigInteger.valueOf(0));
				}

				historyEntry.setExitCode(BigInteger.valueOf(entry.getInt("exitCode", 0)));

				historyEntry.setTaskId(string2BigInteger(entry.getString("taskId", "")));
				historyEntry.setId(string2BigInteger(entry.getString("taskId", "")));
				historyEntry.setJobName(entry.getString("job", ""));
				historyEntry.setStartTime(entry.getString("startTime", ""));
				historyEntry.setEndTime(entry.getString("endTime", ""));
			}
		} else {
			if (history.getJsonArray("history") == null && !history.getBoolean("isPermitted")) {
				throw new Exception("User is not allowed to execute restservice /tasks/history");
			}
		}
		return historyEntry;
	}

	private Order json2JobChainHistoryEntry(String answer) throws Exception {
		Order order = new Order();
		JsonObject history = jsonFromString(answer);
		if (history.getJsonArray("history") != null && history.getJsonArray("history").size() > 0) {
			JsonObject entry = history.getJsonArray("history").getJsonObject(0);
			if (entry != null) {
				order.setId(entry.getString("orderId", ""));
				order.setState(entry.getString("node", ""));
				order.setOrder(entry.getString("orderId", ""));
				order.setHistoryId(string2BigInteger(entry.getString("historyId", "")));
				order.setJobChain(entry.getString("jobChain", ""));
				order.setStartTime(entry.getString("startTime", ""));
				order.setEndTime(entry.getString("endTime", ""));
			}
		} else {
			if (history.getJsonArray("history") == null && !history.getBoolean("isPermitted")) {
				throw new Exception("User is not allowed to execute restservice /orders/history");
			}
		}
		return order;
	}

	private OrderHistoryItem json2JobChainOrderHistoryEntry(String answer) throws Exception {
		JsonObject history = jsonFromString(answer);

		OrderHistoryItem orderHistory = new OrderHistoryItem();
		if (history.getJsonArray("history") != null && history.getJsonArray("history").size() > 0) {
			JsonObject entry = history.getJsonArray("history").getJsonObject(0);
			if (entry != null) {
				orderHistory.setOrderId(entry.getString("orderId", ""));
				orderHistory.setNode(entry.getString("node", ""));
				orderHistory.setHistoryId(entry.getString("historyId", ""));
				orderHistory.setJobChain(entry.getString("jobChain", ""));
				orderHistory.setStartTime(string2Date(entry.getString("startTime", "")));
				orderHistory.setEndTime(string2Date(entry.getString("endTime", "")));
				orderHistory.setPath(entry.getString("path", ""));
				JsonObject stateEntry = entry.getJsonObject("state");
				HistoryState historyState = new HistoryState();
				try {
					historyState.set_text(HistoryStateText.fromValue(stateEntry.getString("_text", "")));
				} catch (IllegalArgumentException e) {
					historyState.set_text(null);
				}
				historyState.setSeverity(stateEntry.getInt("severity"));
				orderHistory.setState(historyState);
			}
		} else {
			if (history.getJsonArray("history") == null && !history.getBoolean("isPermitted")) {
				throw new Exception("User is not allowed to execute restservice /orders/history");
			}
		}
		return orderHistory;
	}

	public HistoryEntry getJobHistoryEntry(String state) throws Exception {
		if (accessToken.isEmpty()) {
			throw new Exception("AccessToken is empty. Login not executed");
		}

		String body;
		if (!"".equals(timeLimit)) {
			HistoryInterval historyInterval = historyHelper.getUTCIntervalFromTimeLimit(timeLimit);
			historyInterval.getUtcFrom();
			body = String.format(JOB_STRING_FOR_WEBSERVICE, schedulerId, jobName) + "[" + state + "],'dateFrom':'"
					+ historyInterval.getUtcFrom() + "','dateTo':'" + historyInterval.getUtcTo() + "'}";
		} else {
			body = String.format(JOB_STRING_FOR_WEBSERVICE, schedulerId, jobName) + "[" + state + "]}";
		}

		body = body.replace("'", "\"");

		String answer = jobSchedulerRestApiClient.postRestService(new URI(jocUrl + "/tasks/history"), body);
		HistoryEntry h = json2HistoryEntry(answer);
		if (h.getId() == null) {
			return null;
		} else {
			return json2HistoryEntry(answer);
		}
	}

	public HistoryEntry getLastCompletedSuccessfullJobHistoryEntry() throws Exception {
		return getJobHistoryEntry("'SUCCESSFUL'");
	}

	public HistoryEntry getLastCompletedJobHistoryEntry() throws Exception {
		return getJobHistoryEntry("'SUCCESSFUL','FAILED'");
	}

	public HistoryEntry getLastCompletedWithErrorJobHistoryEntry() throws Exception {
		return getJobHistoryEntry("'FAILED'");
	}

	public HistoryEntry getLastRunningJobHistoryEntry() throws Exception {
		return getJobHistoryEntry("'INCOMPLETE'");
	}

	public Order getJobChainHistoryEntry(String state) throws Exception {
		if (accessToken.isEmpty()) {
			throw new Exception("AccessToken is empty. Login not executed");
		}

		String body;

		if (!"".equals(timeLimit)) {
			HistoryInterval historyInterval = historyHelper.getUTCIntervalFromTimeLimit(timeLimit);
			historyInterval.getUtcFrom();
			if (orderId == null || orderId.isEmpty()) {
				body = String.format(JOB_CHAIN_STRING_FOR_WEBSERVICE, schedulerId, jobChainName) + "[" + state
						+ "],'dateFrom':'" + historyInterval.getUtcFrom() + "','dateTo':'" + historyInterval.getUtcTo()
						+ "'}";
			} else {
				body = String.format(JOB_CHAIN_ORDER_STRING_FOR_WEBSERVICE, schedulerId, jobChainName, orderId) + "["
						+ state + "],'dateFrom':'" + historyInterval.getUtcFrom() + "','dateTo':'"
						+ historyInterval.getUtcTo() + "'}";
			}
		} else {
			if (orderId == null || orderId.isEmpty()) {
				body = String.format(JOB_CHAIN_STRING_FOR_WEBSERVICE, schedulerId, jobChainName) + "[" + state + "]}";
			} else {
				body = String.format(JOB_CHAIN_ORDER_STRING_FOR_WEBSERVICE, schedulerId, jobChainName, orderId) + "["
						+ state + "]}";
			}
		}

		body = body.replace("'", "\"");

		String answer = jobSchedulerRestApiClient.postRestService(new URI(jocUrl + "/orders/history"), body);
		Order o = json2JobChainHistoryEntry(answer);
		if (o.getHistoryId() == null) {
			return null;
		} else {
			return o;
		}
	}

	public OrderHistoryItem getJobChainOrderHistoryEntry(BigInteger orderHistoryId) throws Exception {
		if (accessToken.isEmpty()) {
			throw new Exception("AccessToken is empty. Login not executed");
		}

		String body = String.format(JOB_CHAIN_ORDER_HISTORY_STRING_FOR_WEBSERVICE, schedulerId, orderHistoryId);
		body = body.replace("'", "\"");

		String answer = jobSchedulerRestApiClient.postRestService(new URI(jocUrl + "/orders/history"), body);
		OrderHistoryItem o = json2JobChainOrderHistoryEntry(answer);
		if (o.getHistoryId() == null) {
			return null;
		} else {
			return o;
		}
	}

	public Order getLastCompletedSuccessfullJobChainHistoryEntry() throws Exception {
		return getJobChainHistoryEntry("'SUCCESSFUL'");
	}

	public Order getLastCompletedJobChainHistoryEntry() throws Exception {
		return getJobChainHistoryEntry("'SUCCESSFUL','FAILED'");
	}

	public Order getLastCompletedWithErrorJobChainHistoryEntry() throws Exception {
		return getJobChainHistoryEntry("'FAILED'");
	}

	public Order getLastRunningJobChainHistoryEntry() throws Exception {
		return getJobChainHistoryEntry("'INCOMPLETE'");
	}

	public void setTimeLimit(String timeLimit) {
		this.timeLimit = timeLimit;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getJobName() {
		return jobName;
	}

	public void setSchedulerId(String schedulerId) {
		this.schedulerId = schedulerId;
	}

	public void setJobChainName(String jobChainName) {
		this.jobChainName = jobChainName;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

}
