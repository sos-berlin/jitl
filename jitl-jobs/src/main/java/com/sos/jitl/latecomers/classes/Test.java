package com.sos.jitl.latecomers.classes;

import java.net.URISyntaxException;
import java.util.ArrayList;

import com.sos.exception.SOSException;
import com.sos.jitl.restclient.WebserviceCredentials;
import com.sos.joc.model.plan.PlanItem;

public class Test {

	public void x() throws SOSException, URISyntaxException {
		DailyPlanExecuter dailyPlanExecuter = null;
		JobStartExecuter jobStartExecuter = null;
		JobChainStartExecuter jobChainStartExecuter = null;
		String jocUrl = "http://localhost:4446/joc/api";
		WebserviceCredentials webserviceCredentials = new WebserviceCredentials();
		webserviceCredentials.setPassword("api");
		webserviceCredentials.setSchedulerId("scheduler_joc_cockpit");
		webserviceCredentials.setUser("api_user");

		if (!webserviceCredentials.account().isEmpty()) {
			dailyPlanExecuter = new DailyPlanExecuter(jocUrl, webserviceCredentials.account());
		} else {
			dailyPlanExecuter = new DailyPlanExecuter(jocUrl);
		}

		dailyPlanExecuter.setSchedulerId(webserviceCredentials.getSchedulerId());

		dailyPlanExecuter.login(webserviceCredentials.getAccessToken());
	
		jobStartExecuter = new JobStartExecuter(jocUrl, webserviceCredentials.account());
		jobStartExecuter.setSchedulerId(webserviceCredentials.getSchedulerId());
		jobStartExecuter.login(dailyPlanExecuter.getAccessToken());

		jobChainStartExecuter = new JobChainStartExecuter(jocUrl, webserviceCredentials.account());
		jobChainStartExecuter.setSchedulerId(webserviceCredentials.getSchedulerId());
		jobChainStartExecuter.login(dailyPlanExecuter.getAccessToken());

		ArrayList<PlanItem> listOfPlanItems = dailyPlanExecuter.getDailyPlan();
		if (listOfPlanItems != null) {
			for (PlanItem plan : listOfPlanItems) {
				System.out.println("job:" + plan.getJob());
				jobStartExecuter.startJob(plan.getJob());
				System.out.println("jobChain:" + plan.getJobChain());
				System.out.println("orderId:" + plan.getOrderId());
				jobChainStartExecuter.startJobChain(plan.getJobChain(), plan.getOrderId());
			}
		}
	}

	public static void main(String[] args) {
		Test test = new Test();
		try {
			test.x();
		} catch (SOSException | URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
