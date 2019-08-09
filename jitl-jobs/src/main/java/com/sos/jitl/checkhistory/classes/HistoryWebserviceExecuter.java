package com.sos.jitl.checkhistory.classes;

import java.math.BigInteger;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.model.order.OrderHistoryItem;
import com.sos.scheduler.model.answers.HistoryEntry;
import com.sos.scheduler.model.answers.JobChain.OrderHistory.Order;

public class HistoryWebserviceExecuter extends HistoryDataSource {

	private static final String JOB_STRING_FOR_WEBSERVICE = "{'jobschedulerId':'%s','limit':1,'jobs':[{'job':'%s'}],'historyStates':";
	private static final String JOB_CHAIN_ORDER_STRING_FOR_WEBSERVICE = "{'jobschedulerId':'%s','limit':1,'orders':[{'jobChain':'%s','orderId':'%s'}],'historyStates':";
	private static final String JOB_CHAIN_STRING_FOR_WEBSERVICE = "{'jobschedulerId':'%s','limit':1,'orders':[{'jobChain':'%s'}],'historyStates':";
	private static final String JOB_CHAIN_ORDER_HISTORY_STRING_FOR_WEBSERVICE = "{'jobschedulerId':'%s','historyIds':['%s']}";

	private static final Logger LOGGER = LoggerFactory.getLogger(HistoryWebserviceExecuter.class);
 

	public HistoryWebserviceExecuter(String jocUrl, String jocAccount) {
		super(jocUrl, jocAccount);
	}

	public HistoryWebserviceExecuter(String jocUrl) {
		super(jocUrl);
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

	 
}
