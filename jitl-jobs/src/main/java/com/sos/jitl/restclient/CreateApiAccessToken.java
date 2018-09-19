package com.sos.jitl.restclient;

import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.exception.SOSException;
import com.typesafe.config.ConfigException;

import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Job_chain;
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.util.SOSPrivateConf;

public class CreateApiAccessToken extends JobSchedulerJobAdapter {

	private static final String X_ACCESS_TOKEN = "X-Access-Token";
	private static final int MAX_WAIT_TIME_FOR_ACCESS_TOKEN = 30;
	private static final String SOS_REST_CREATE_API_ACCESS_TOKEN = "/sos/rest/createApiAccessToken";

	private static final Logger LOGGER = LoggerFactory.getLogger(CreateApiAccessToken.class);

	@Override
	public boolean spooler_process() throws Exception {
		try {
			super.spooler_process();
			doProcessing();
		} catch (Exception e) {
			return false;
		}
		return this.signalSuccess();
	}

	@Override
	public boolean spooler_process_before() throws SOSException, URISyntaxException, InterruptedException {
		LOGGER.debug("Starting spooler_process_before");
		String xAccessToken = spooler.variables().value(X_ACCESS_TOKEN);
		String jocUrl = spooler.variables().value("joc_url");

		if (jocUrl == null) {
			jocUrl = "";
		}

		ApiAccessToken apiAccessToken = new ApiAccessToken(jocUrl);

		LOGGER.debug("Check whether accessToken is valid");
		if (!apiAccessToken.isValidAccessToken(xAccessToken)) {
			Job_chain j = spooler.job_chain(SOS_REST_CREATE_API_ACCESS_TOKEN);
			Order o = spooler.create_order();
			j.add_or_replace_order(o);

			int cnt = 0;
			while (cnt < MAX_WAIT_TIME_FOR_ACCESS_TOKEN && !apiAccessToken.isValidAccessToken(xAccessToken)) {
				java.lang.Thread.sleep(1000);
				xAccessToken = spooler.variables().value(X_ACCESS_TOKEN);
				jocUrl = spooler.variables().value("joc_url");
				apiAccessToken.setJocUrl(jocUrl);
				if (!apiAccessToken.isValidAccessToken(xAccessToken)) {
					LOGGER.info("Waiting for access token.....");
				}
				cnt = cnt + 1;
			}
		}
		if (!apiAccessToken.isValidAccessToken(xAccessToken)) {
			LOGGER.warn("Could not renew the access token for JOC Server:" + jocUrl);
			return !continue_with_spooler_process;
		}
		return continue_with_spooler_process;

	}

	public void doProcessing() throws Exception {
		LOGGER.debug("Starting doProcessing");

		Variable_set params = spooler.create_variable_set();
		params.merge(spooler_task.params());

		if (this.isJobchain()) {
			params.merge(spooler_task.order().params());
		}

		SOSPrivateConf sosPrivateConf = new SOSPrivateConf("config/private/private.conf");

		String jocUrl;
		try {
			jocUrl = sosPrivateConf.getValue("joc.webservice.jitl", "joc.url");
		} catch (ConfigException e) {
			jocUrl = sosPrivateConf.getValue("joc.url");
		}
		jocUrl = jocUrl + "/joc/api";
		LOGGER.debug("jocUrl: " + jocUrl);
		spooler.variables().set_value("joc_url", jocUrl);

		String userEncodedAccount;
		try {
			userEncodedAccount = sosPrivateConf.getDecodedValue("joc.webservice.jitl", "joc.account");
		} catch (ConfigException e) {
			userEncodedAccount = sosPrivateConf.getDecodedValue("joc.account");
		}

		String xAccessToken = spooler.variables().value(X_ACCESS_TOKEN);

		ApiAccessToken apiAccessToken = new ApiAccessToken(jocUrl);
		boolean sessionIsValid;

		int cnt = 0;
		while (cnt < MAX_WAIT_TIME_FOR_ACCESS_TOKEN && !apiAccessToken.isValidAccessToken(xAccessToken)) {
			LOGGER.debug("check session");

			try {
				sessionIsValid = apiAccessToken.isValidUserAccount(userEncodedAccount);
			} catch (Exception e) {
				sessionIsValid = false;
			}
			if (!sessionIsValid || xAccessToken.isEmpty()) {
				LOGGER.debug("... execute login");
				try {
					xAccessToken = apiAccessToken.login(userEncodedAccount);
				} catch (Exception e) {
					LOGGER.warn("... login failed with "
							+ sosPrivateConf.getDecodedValue("joc.webservice.jitl", "joc.account") + " at " + jocUrl);
				}
				if (xAccessToken != null && !xAccessToken.isEmpty()) {
					LOGGER.debug("... set accessToken:" + xAccessToken);
					spooler.variables().set_value(X_ACCESS_TOKEN, xAccessToken);
				} else {
					LOGGER.debug("AccessToken " + xAccessToken + " is not valid. Trying to renew it...");
					java.lang.Thread.sleep(1000);
				}
			}
		}
		if (cnt == MAX_WAIT_TIME_FOR_ACCESS_TOKEN) {
			LOGGER.warn("Could not renew the access token for JOC Server:" + jocUrl);
		}
	}
}
