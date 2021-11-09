package com.sos.jitl.restclient;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import sos.util.SOSPrivateConf;

public class TestApiAccessToken {

    
    

    @Test
    public void testIsValidAccessToken() throws Exception {

        WebserviceCredentials webserviceCredentials = new WebserviceCredentials();
                                                           //http://localhost:8081/rest/security/login
     //    ApiAccessToken apiAccesToken = new ApiAccessToken("https://joc-1-13-primary.sos:7443/joc/api");
//        ApiAccessToken apiAccesToken = new ApiAccessToken("http://localhost:8081/rest");
         ApiAccessToken apiAccesToken = new ApiAccessToken("https://joc-1-13-secondary.sos:17543/joc/api");
        String userAccount = null;
        SOSPrivateConf sosPrivateConf = new SOSPrivateConf("src/test/resources/private.conf");

        userAccount = sosPrivateConf.getDecodedValue("joc.webservice.jitl", "joc.account");
        webserviceCredentials.setUserDecodedAccount(userAccount);
        webserviceCredentials.setKeyStorePassword("");
        webserviceCredentials.setKeyStoreType("PKCS12");
        webserviceCredentials.setKeyPassword("");
        webserviceCredentials.setTrustStoreType("PKCS12");
        webserviceCredentials.setKeyStorePath("C:/temp/laptop-7rsacscv.p12");
        webserviceCredentials.setTrustStorePassword("123456");    
        webserviceCredentials.setTrustStorePath("C:/temp/https-truststore.p12");
 
        String xAccessToken = null;
        xAccessToken = apiAccesToken.login(webserviceCredentials);
        boolean x = apiAccesToken.isValidAccessToken(xAccessToken,webserviceCredentials);
        assertEquals("Test testIsValidAccessToken failed...", true, x);

    }

}
