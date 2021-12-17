package com.sos.jitl.checkhistory.classes;

import java.math.BigInteger;
import java.net.URI;
import javax.json.JsonObject;
import com.sos.jitl.checkhistory.HistoryHelper;
import com.sos.jitl.restclient.WebserviceCredentials;
import com.sos.jitl.restclient.WebserviceExecuter;
import com.sos.joc.model.common.HistoryState;
import com.sos.joc.model.common.HistoryStateText;
import com.sos.joc.model.order.OrderHistoryItem;
import com.sos.scheduler.model.answers.HistoryEntry;
import com.sos.scheduler.model.answers.JobChain.OrderHistory.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class HistoryDataSource extends WebserviceExecuter {

	
	private static final Logger LOGGER = LoggerFactory.getLogger(HistoryDataSource.class);
	protected HistoryHelper historyHelper;
	protected String schedulerId;
	protected String timeLimit = "";
	protected String jobName;
	protected String jobChainName;
	protected String orderId;
	
    protected abstract HistoryEntry getJobHistoryEntry(String state) throws Exception;
    protected abstract Order getJobChainHistoryEntry(String state) throws Exception;
    public abstract OrderHistoryItem getJobChainOrderHistoryEntry(BigInteger orderHistoryId) throws Exception;
         


	public HistoryDataSource(WebserviceCredentials webserviceCredentials) {
		super(webserviceCredentials);
		historyHelper = new HistoryHelper();
	}

 

	protected HistoryEntry json2HistoryEntry(String answer) throws Exception {
		HistoryEntry historyEntry = new HistoryEntry();
		LOGGER.debug("Get json from " + answer);
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
			if (history.getJsonArray("history") == null  && !history.getBoolean("isPermitted",true)) {
				throw new Exception("User is not allowed to execute restservice /tasks/history");
			}
		}
		return historyEntry;
	}

	protected Order json2JobChainHistoryEntry(String answer) throws Exception {
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

	protected OrderHistoryItem json2JobChainOrderHistoryEntry(String answer) throws Exception {
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
