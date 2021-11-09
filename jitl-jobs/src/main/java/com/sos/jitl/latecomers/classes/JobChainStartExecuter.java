package com.sos.jitl.latecomers.classes;

import java.net.URI;
import java.net.URISyntaxException;
import javax.json.JsonObject;
import com.sos.exception.SOSAccessDeniedException;
import com.sos.exception.SOSException;
import com.sos.jitl.restclient.WebserviceCredentials;
import com.sos.jitl.restclient.WebserviceExecuter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobChainStartExecuter extends WebserviceExecuter {

	private static final String API_CALL = "/orders/start";
	private static final String JOB_CHAIN_ORDER_START_STRING_FOR_WEBSERVICE = "{'orders':[{'orderId':'%s','jobChain':'%s','at':'now'}],'jobschedulerId':'%s'}";
	private static final Logger LOGGER = LoggerFactory.getLogger(JobStartExecuter.class);
 
	 
	public JobChainStartExecuter(WebserviceCredentials webserviceCredentials) {
		super(webserviceCredentials);
	}

	private boolean json2PlanList(String answer) throws SOSAccessDeniedException {
		JsonObject jobstart = jsonFromString(answer);
		boolean ok = jobstart.getBoolean("ok",false);

		if (!ok && !jobstart.getBoolean("isPermitted",true)) {
			throw new SOSAccessDeniedException("User is not allowed to execute restservice " + API_CALL);
		}
		return ok;
	}

	public boolean startJobChain(String jobChain, String orderId) throws SOSException, URISyntaxException {
		if (accessToken.isEmpty()) {
			throw new SOSAccessDeniedException("AccessToken is empty. Login not executed");
		}

		String body = String.format(JOB_CHAIN_ORDER_START_STRING_FOR_WEBSERVICE, orderId, jobChain, schedulerId);
		body = body.replace("'", "\"");

		String answer = jobSchedulerRestApiClient.postRestService(new URI(webserviceCredentials.getJocUrl() + API_CALL), body);
		boolean o = json2PlanList(answer);
		return o;
	}

}
