package com.sos.jitl.restclient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class JobSchedulerRestApiClient {

    private final Logger LOGGER = Logger.getLogger(JobSchedulerRestApiClient.class);
    private  String accept = "application/json";
    private  HashMap<String, String> headers = new HashMap<String, String>();
    private  HttpResponse httpResponse;

    private  String getParameter(String p) {
        String[] pParts = p.replaceFirst("\\)\\s*$", "").split("\\(", 2);
        String s = (pParts.length == 2) ? pParts[1] : "";
        return s.trim();
    }

    public  String executeRestServiceCommand(String restCommand, String urlParam) throws Exception {
        String s = urlParam.replaceFirst("^([^:]*)://.*$", "$1");
        if (s.equals(urlParam)) {
            urlParam = "http://" + urlParam;
        }
        java.net.URL url = new java.net.URL(urlParam);
        return executeRestServiceCommand(restCommand,url); 
    }

    public  String executeRestServiceCommand(String restCommand, java.net.URL  url) throws Exception {
        return executeRestServiceCommand(restCommand, url, null);
    }
    
    public  String executeRestServiceCommand(String restCommand, java.net.URL  url, String body) throws Exception {
        
        String result = "";
        String protocol = "";
        if (body == null) {
            body = getParameter(restCommand);
        }

        String host = url.getHost();
        int port = url.getPort();
        String path = url.getPath();
        protocol = url.getProtocol();
        String query = url.getQuery();
        if ("delete".equalsIgnoreCase(restCommand)) {
            result = String.valueOf(execute(restCommand, url));
        } else {
            if (restCommand.toLowerCase().startsWith("put")) {
                result = putRestService(host, port, path, protocol, body);
            } else {
                if ("get".equalsIgnoreCase(restCommand)) {
                    result = getRestService(host, port, path, protocol, query);
                } else {
                    if (restCommand.toLowerCase().startsWith("post")) {
                        result = postRestService(host, port, path, protocol, body);
                    } else {
                        throw new Exception(String.format("Unknown rest command: %s (usage: get|post(body)|delete|put(body))", restCommand));
                    }
                }
            }
        }
        return result;
    }
    
    public  String executeRestService(String urlParam) throws Exception {
        return executeRestServiceCommand("get", urlParam);
    }

    public void addHeader(String header, String value) {
        headers.put(header, value);
    }

    private  int execute(String command, java.net.URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(command.toUpperCase());
        return connection.getResponseCode();
    }

    public  String getRestService(String host, int port, String path, String protocol, String query) throws ClientProtocolException,
            IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        String s = "";
        HttpHost target = new HttpHost(host, port, protocol);
        HttpGet getRequestGet;
        if ("".equals(query)) {
            getRequestGet = new HttpGet(path);
        } else {
            getRequestGet = new HttpGet(path + "?" + query);
        }
        getRequestGet.setHeader("Accept", accept);
        httpResponse = null;
        for (Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            getRequestGet.setHeader(key, value);
        }
        httpResponse = httpClient.execute(target, getRequestGet);
        HttpEntity entity = httpResponse.getEntity();
        if (entity != null) {
            s = EntityUtils.toString(entity);
        }
        httpClient.close();
        return s;
    }

    public  String postRestService(String host, int port, String path, String protocol, String body) throws ClientProtocolException,
            IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        String s = "";
        HttpHost target = new HttpHost(host, port, protocol);
        HttpPost requestPost = new HttpPost(path);
        requestPost.setHeader("Accept", accept);
        httpResponse = null;
        for (Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            requestPost.setHeader(key, value);
        }
        StringEntity entity = new StringEntity(body);
        requestPost.setEntity(entity);
        requestPost.setEntity(entity);
        httpResponse = httpClient.execute(target, requestPost);
        HttpEntity httpEntity = httpResponse.getEntity();
        if (httpEntity != null) {
            s = EntityUtils.toString(httpEntity);
        }
        httpClient.close();
        return s;
    }

    public  String putRestService(String host, int port, String path, String protocol, String body) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        String s = "";
        try {
            HttpHost target = new HttpHost(host, port, protocol);
            HttpPut requestPut = new HttpPut(path);
            requestPut.setHeader("Accept", accept);
            httpResponse = null;
            for (Entry<String, String> entry : headers.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                requestPut.setHeader(key, value);
            }
            StringEntity entity = new StringEntity(body);
            requestPut.setEntity(entity);
            requestPut.setEntity(entity);
            httpResponse = httpClient.execute(target, requestPut);
            HttpEntity httpEntity = httpResponse.getEntity();
            if (httpEntity != null) {
                s = EntityUtils.toString(httpEntity);
            }
            httpClient.close();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return s;
    }
    
 

    public HttpResponse getHttpResponse() {
        return httpResponse;
    }

    public void setAccept(String accept) {
        this.accept = accept;
    }

    
    public int statusCode() {
        return httpResponse.getStatusLine().getStatusCode();
    }

    public void clearHeaders() {
        headers = new HashMap<String, String>();
    }
}
