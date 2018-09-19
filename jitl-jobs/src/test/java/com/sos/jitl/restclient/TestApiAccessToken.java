package com.sos.jitl.restclient;

import static org.junit.Assert.*;
import org.junit.Test;

import sos.util.SOSPrivateConf;

public class TestApiAccessToken {

	@Test
	public void testIsValidAccessToken() throws Exception {
		ApiAccessToken apiAccesToken = new ApiAccessToken("http://localhost:4446/joc/api");
		String userAccount = null;
		SOSPrivateConf sosPrivateConf = new SOSPrivateConf(
				"src/test/resources/private.conf");

		userAccount = sosPrivateConf.getDecodedValue("joc.webservice.jitl", "joc.account");
		String xAccessToken = null;
		xAccessToken = apiAccesToken.login(userAccount);
		boolean x = apiAccesToken.isValidAccessToken(xAccessToken);
        assertEquals("Test testIsValidAccessToken failed...", true, x);

	}
	
	 

}
