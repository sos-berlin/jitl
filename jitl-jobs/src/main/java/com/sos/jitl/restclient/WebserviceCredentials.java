package com.sos.jitl.restclient;

import org.apache.commons.codec.binary.Base64;

public class WebserviceCredentials {

    private String schedulerId = "";
    private String user = "";
    private String password = "";
    private String accessToken = "";
	private String userDecodedAccount="";
    private String jocUrl;

    public String getUserDecodedAccount() {
		return userDecodedAccount;
	}

	public void setUserDecodedAccount(String userDecodedAccount) {
		this.userDecodedAccount = userDecodedAccount;
	}

	public String getJocUrl() {
		return jocUrl;
	}

	public void setJocUrl(String jocUrl) {
		this.jocUrl = jocUrl;
	}

    public WebserviceCredentials(String schedulerId, String user, String password, String accessToken) {
        super();
        this.schedulerId = schedulerId;
        this.user = user;
        this.password = password;
        this.accessToken = accessToken;
    }

    public WebserviceCredentials() {
        super();
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getSchedulerId() {
        return schedulerId;
    }

    public void setSchedulerId(String schedulerId) {
        this.schedulerId = schedulerId;
    }

    public String account() {
        if (!user.isEmpty() && !password.isEmpty()) {
            return user + ":" + password;
        } else {
            return "";

        }
    }
    
    public String getUserEncodedAccount() {
    	byte[] authEncBytes = Base64.encodeBase64(userDecodedAccount.getBytes());
		return new String(authEncBytes);
    }

	public String getUser() {
		return user;
	}
 
}
