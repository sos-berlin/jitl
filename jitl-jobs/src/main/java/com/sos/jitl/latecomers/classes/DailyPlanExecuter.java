package com.sos.jitl.latecomers.classes;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import javax.json.JsonArray;
import javax.json.JsonObject;
import com.sos.exception.SOSAccessDeniedException;
import com.sos.exception.SOSException;
import com.sos.jitl.restclient.WebserviceExecuter;
import com.sos.joc.model.plan.PlanItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DailyPlanExecuter extends WebserviceExecuter {

	private static final String API_CALL = "/plan";
	private static final String DAILY_PLAN_STRING_FOR_WEBSERVICE = "{'jobschedulerId':'%s','states':['PLANNED'],'late':true,'dateFrom':'%s','dateTo':'%s','timeZone':'%s'}";
	private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanExecuter.class);

	public DailyPlanExecuter(String jocUrl, String jocAccount) {
		super(jocUrl, jocAccount);
	}

	public DailyPlanExecuter(String jocUrl) {
		super(jocUrl);
	}


	private ArrayList<PlanItem> json2PlanList(String answer) throws SOSAccessDeniedException {
		ArrayList<PlanItem> result = new ArrayList<PlanItem>();
		PlanItem planItem = new PlanItem();
		JsonObject plan = jsonFromString(answer);
		JsonArray planArray = plan.getJsonArray("planItems");
		if (planArray != null && planArray.size() > 0) {
			for (int i = 0; i < planArray.size(); i++) {
				JsonObject entry = planArray.getJsonObject(i);
				if (entry != null) {
					planItem.setJob(entry.getString("job",""));
					planItem.setLate(entry.getBoolean("late"));
					planItem.setJobChain(entry.getString("jobChain", ""));
					planItem.setOrderId(entry.getString("orderId", ""));
					result.add(planItem);
				}
			}
		} else {
			if (plan.getJsonArray("planItems") == null && !plan.getBoolean("isPermitted",true)) {
				throw new SOSAccessDeniedException("User is not allowed to execute restservice " +  API_CALL);
			}
		}
		return result;
	}

	public ArrayList<PlanItem> getDailyPlan(String dayOffset) throws SOSException, URISyntaxException {
		if (accessToken.isEmpty()) {
			throw new SOSAccessDeniedException("AccessToken is empty. Login not executed");
		}

		String body = String.format(DAILY_PLAN_STRING_FOR_WEBSERVICE, schedulerId, dayOffset,dayOffset, "Europe/Berlin");
		body = body.replace("'", "\"");

		String answer = jobSchedulerRestApiClient.postRestService(new URI(jocUrl + API_CALL), body);
		ArrayList<PlanItem> o = json2PlanList(answer);
		if (o.isEmpty()) {
			return null;
		} else {
			return o;
		}
	}
 }
