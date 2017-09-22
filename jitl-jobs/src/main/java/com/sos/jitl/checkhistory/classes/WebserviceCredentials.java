package com.sos.jitl.checkhistory.classes;


public class WebserviceCredentials {

    private String schedulerId = "";
    private String user = "";
    private String password = "";
    private String accessToken = "";

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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
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
    
 
}
