package com.sos.jitl.latecomers.classes;

import java.net.URI;
import java.net.URISyntaxException;
import javax.json.JsonArray;
import javax.json.JsonObject;
import com.sos.exception.SOSAccessDeniedException;
import com.sos.exception.SOSException;
import com.sos.jitl.restclient.WebserviceCredentials;
import com.sos.jitl.restclient.WebserviceExecuter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobStartExecuter extends WebserviceExecuter {

	private static final String API_CALL = "/jobs/start";
	private static final String JOB_START_STRING_FOR_WEBSERVICE = "{'jobs':[{'job':'%s','at':'now'}],'jobschedulerId':'%s'}";
	private static final Logger LOGGER = LoggerFactory.getLogger(JobStartExecuter.class);
 
 
	public JobStartExecuter(WebserviceCredentials webserviceCredentials) {
		super(webserviceCredentials);
	}

	private boolean json2PlanList(String answer) throws SOSAccessDeniedException {
 		JsonObject jobstart = jsonFromString(answer);
 		boolean ok = jobstart.getBoolean("ok",false);
		JsonArray tasksArray = jobstart.getJsonArray("tasks");
		if (tasksArray != null && tasksArray.size() > 0) {
			for (int i = 0; i < tasksArray.size(); i++) {
				JsonObject entry = tasksArray.getJsonObject(i);
				if (entry != null) {
					LOGGER.debug(String.format("job:%s, task ID: %s",entry.getString("job",""), entry.getString("taskId","")));
				}
			}
		} else {
			if (jobstart.getJsonArray("tasks") == null && !jobstart.getBoolean("isPermitted",true)) {
				throw new SOSAccessDeniedException("User is not allowed to execute restservice " + API_CALL);
			}
		}
		return ok;
	}

	public boolean startJob(String job) throws SOSException, URISyntaxException {
		if (accessToken.isEmpty()) {
			throw new SOSAccessDeniedException("AccessToken is empty. Login not executed");
		}

		String body = String.format(JOB_START_STRING_FOR_WEBSERVICE, job,schedulerId);
		body = body.replace("'", "\"");

		String answer = jobSchedulerRestApiClient.postRestService(new URI(webserviceCredentials.getJocUrl() + API_CALL), body);
		boolean o = json2PlanList(answer);
		return o;
	}

}
