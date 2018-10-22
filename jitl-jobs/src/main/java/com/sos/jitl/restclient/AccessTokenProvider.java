package com.sos.jitl.restclient;

import java.net.URISyntaxException;

import com.sos.exception.SOSException;
import sos.spooler.Job_chain;
import sos.spooler.Order;
import sos.spooler.Spooler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccessTokenProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenProvider.class);
	private static final String X_ACCESS_TOKEN = "X-Access-Token";
	private static final int MAX_WAIT_TIME_FOR_ACCESS_TOKEN = 30;
	private static final String SOS_REST_CREATE_API_ACCESS_TOKEN = "/sos/rest/createApiAccessToken";

	public WebserviceCredentials getAccessToken(Spooler spooler)
			throws   URISyntaxException, InterruptedException, SOSException {
 
		String schedulerId = "";
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
			return null;
		}

		schedulerId = spooler.id();


		WebserviceCredentials webserviceCredentials = new WebserviceCredentials();
		webserviceCredentials.setSchedulerId(schedulerId);
		webserviceCredentials.setAccessToken(xAccessToken);
		return webserviceCredentials;

	}

}
