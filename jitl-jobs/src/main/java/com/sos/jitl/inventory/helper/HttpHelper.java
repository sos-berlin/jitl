package com.sos.jitl.inventory.helper;

public class HttpHelper {

    public static String getHttpHost(String httpPort, String defaultHost) {
        String httpHost = defaultHost;
        if (httpPort != null) {
            if (httpPort.indexOf(":") > -1) {
                httpHost = httpPort.split(":")[0];
                if ("0.0.0.0".equals(httpHost)) {
                    httpHost = defaultHost;
                }
            }
        }
        return httpHost;
    }

    public static Integer getHttpPort(String httpPort) {
        if (httpPort != null) {
            if (httpPort.indexOf(":") > -1) {
                httpPort = httpPort.split(":")[1];
            }
            return Integer.parseInt(httpPort);
        }
        return null;
    }
    

}
