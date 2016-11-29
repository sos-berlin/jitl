package com.sos.jitl.restclient;

import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.sos.exception.BadRequestException;
import com.sos.exception.ConnectionRefusedException;
import com.sos.exception.NoResponseException;
import com.sos.exception.SOSException;

public class JobSchedulerRestApiClient {

    private String accept = "application/json";
    private HashMap<String, String> headers = new HashMap<String, String>();
    private HashMap<String, String> responseHeaders = new HashMap<String, String>();
    private RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
    private HttpResponse httpResponse;
    private CloseableHttpClient httpClient = null;
    private boolean forcedClosingHttpClient = false;
    private boolean autoCloseHttpClient = true;
    
    
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

    public String getResponseHeader(String key) {
        if (responseHeaders != null) {
            return responseHeaders.get(key);
        }
        return "";
    }

    /*
     * the time (in milliseconds) to establish the connection with the remote
     * host
     */
    public void setConnectionTimeout(int connectionTimeout) {
        requestConfigBuilder.setConnectTimeout(connectionTimeout);
    }

    /*
     * the time (in milliseconds) waiting for data after the connection was
     * established; maximum time of inactivity between two data packets
     */
    public void setSocketTimeout(int socketTimeout) {
        requestConfigBuilder.setSocketTimeout(socketTimeout);
    }
    
    public void createHttpClient() {
        if (httpClient == null) {
            httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfigBuilder.build()).build();
        }
    }
    
    public void setHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }
    
    public void closeHttpClient() {
        try {
            forcedClosingHttpClient = false;
            httpClient.close();
        } catch (Exception e) {}
    }
    
    public void forcedClosingHttpClient() {
        try {
            forcedClosingHttpClient = true;
            httpClient.close();
        } catch (Exception e) {}
    }

    public boolean isForcedClosingHttpClient() {
        return forcedClosingHttpClient;
    }

    public boolean isAutoCloseHttpClient() {
        return autoCloseHttpClient;
    }

    public void setAutoCloseHttpClient(boolean autoCloseHttpClient) {
        this.autoCloseHttpClient = autoCloseHttpClient;
    }

    public String executeRestServiceCommand(String restCommand, String urlParam) throws SOSException, SocketException {
        String s = urlParam.replaceFirst("^([^:]*)://.*$", "$1");
        if (s.equals(urlParam)) {
            urlParam = "http://" + urlParam;
        }
        URL url;
        try {
            url = new URL(urlParam);
        } catch (Exception e) {
            throw new SOSException(e);
        }
        return executeRestServiceCommand(restCommand, url);
    }

    public String executeRestServiceCommand(String restCommand, URL url) throws SOSException, SocketException {
        return executeRestServiceCommand(restCommand, url, null);
    }
    
    public String executeRestServiceCommand(String restCommand, URI uri) throws SOSException, SocketException {
        return executeRestServiceCommand(restCommand, uri, null);
    }

    public String executeRestServiceCommand(String restCommand, URL url, String body) throws SOSException, SocketException {

        String result = "";
        if (body == null) {
            body = JobSchedulerRestClient.getParameter(restCommand);
        }
        String path = url.getPath();
        String query = url.getQuery();
        if (query != null && !query.isEmpty()) {
            path = path + "?" + query;
        }
        HttpHost httpHost = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
        if (restCommand.toLowerCase().startsWith("post")) {
            result = postRestService(httpHost, path, body);
        } else if ("get".equalsIgnoreCase(restCommand)) {
            result = getRestService(httpHost, path);
        } else if ("delete".equalsIgnoreCase(restCommand)) {
            result = deleteRestService(restCommand, url);
        } else if (restCommand.toLowerCase().startsWith("put")) {
            result = putRestService(httpHost, path, body);
        } else {
            throw new SOSException(String.format("Unknown rest command method: %s (usage: get|post(body)|delete|put(body))", restCommand));
        }
        return result;
    }
    
    public String executeRestServiceCommand(String restCommand, URI uri, String body) throws SOSException, SocketException {

        String result = "";
        if (body == null) {
            body = JobSchedulerRestClient.getParameter(restCommand);
        }
        if (restCommand.toLowerCase().startsWith("post")) {
            result = postRestService(uri, body);
        } else if ("get".equalsIgnoreCase(restCommand)) {
            result = getRestService(uri);
        } else if ("delete".equalsIgnoreCase(restCommand)) {
            result = deleteRestService(restCommand, uri);
        } else if (restCommand.toLowerCase().startsWith("put")) {
            result = putRestService(uri, body);
        } else {
            throw new SOSException(String.format("Unknown rest command method: %s (usage: get|post(body)|delete|put(body))", restCommand));
        }
        return result;
    }

    public String executeRestService(String urlParam) throws SOSException, SocketException {
        return executeRestServiceCommand("get", urlParam);
    }

    public void addHeader(String header, String value) {
        headers.put(header, value);
    }

    public String deleteRestService(String command, URL url) throws SOSException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(command.toUpperCase());
            try {
                return String.valueOf(connection.getResponseCode());
            } catch (Exception e) {
                throw new NoResponseException(url.toString(), e);
            }
        } catch (NoResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new ConnectionRefusedException(url.toString(), e);
        } finally {
            try {
                connection.disconnect();
            } catch (Exception e) {}
        }
    }
    
    public String deleteRestService(String command, URI uri) throws SOSException {
        try {
            return deleteRestService(command, uri.toURL());
        } catch (SOSException e) {
            throw e;
        } catch (Exception e) {
            throw new SOSException(e);
        }
    }

    public String getRestService(HttpHost target, String path) throws SOSException, SocketException {
        return executeRequest(target, new HttpGet(path));
    }
    
    public String getRestService(URI uri) throws SOSException, SocketException {
        return executeRequest(new HttpGet(uri));
    }

    public String postRestService(HttpHost target, String path, String body) throws SOSException, SocketException {
        HttpPost requestPost = new HttpPost(path);
        try {
            if (body != null && !body.isEmpty()) {
                StringEntity entity = new StringEntity(body);
                requestPost.setEntity(entity);
            }
        } catch (Exception e) {
            throw new BadRequestException(body, e);
        }
        return executeRequest(target, requestPost);
    }
    
    public String postRestService(URI uri, String body) throws SOSException, SocketException {
        HttpPost requestPost = new HttpPost(uri);
        try {
            if (body != null && !body.isEmpty()) {
                StringEntity entity = new StringEntity(body);
                requestPost.setEntity(entity);
            }
        } catch (Exception e) {
            throw new BadRequestException(body, e);
        }
        return executeRequest(requestPost);
    }

    public String putRestService(HttpHost target, String path, String body) throws SOSException, SocketException {
        HttpPut requestPut = new HttpPut(path);
        try {
            if (body != null && !body.isEmpty()) {
                StringEntity entity = new StringEntity(body);
                requestPut.setEntity(entity);
            }
        } catch (Exception e) {
            throw new BadRequestException(body, e);
        }
        return executeRequest(target, requestPut);
    }
    
    public String putRestService(URI uri, String body) throws SOSException, SocketException {
        HttpPut requestPut = new HttpPut(uri);
        try {
            if (body != null && !body.isEmpty()) {
                StringEntity entity = new StringEntity(body);
                requestPut.setEntity(entity);
            }
        } catch (Exception e) {
            throw new BadRequestException(body, e);
        }
        return executeRequest(requestPut);
    }
    
    private String executeRequest(HttpHost target, HttpRequest request) throws SOSException, SocketException {
        httpResponse = null;
        createHttpClient();
        setHttpRequestHeaders(request);
        try {
            httpResponse = httpClient.execute(target, request);
            return getResponse();
        } catch (SOSException e) {
            throw e;
        } catch (SocketTimeoutException | IllegalStateException e) {
            throw new NoResponseException(e);
        } catch (SocketException e) {
            throw e;
        } catch (Exception e) {
            throw new ConnectionRefusedException(e);
        } finally {
            try {
                if (isAutoCloseHttpClient()) {
                    httpClient.close(); 
                }
            } catch (Exception e) {}
        }
    }
    
    private String executeRequest(HttpUriRequest request) throws SOSException, SocketException {
        httpResponse = null;
        createHttpClient();
        setHttpRequestHeaders(request);
        try {
            httpResponse = httpClient.execute(request);
            return getResponse();
        } catch (SOSException e) {
            throw e;
        } catch (SocketTimeoutException | IllegalStateException e) {
            throw new NoResponseException(e);
        } catch (SocketException e) {
            throw e;
        } catch (Exception e) {
            throw new ConnectionRefusedException(e);
        } finally {
            try {
                if (isAutoCloseHttpClient()) {
                    httpClient.close(); 
                }
            } catch (Exception e) {}
        }
    }
    
    private String getResponse() throws NoResponseException {
        try {
            String s = "";
            setHttpResponseHeaders();
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                s = EntityUtils.toString(entity);
            }
            return s;
        } catch (Exception e) {
            throw new NoResponseException(e);
        } 
    }
    
    private void setHttpRequestHeaders(HttpRequest request) {
        request.setHeader("Accept", accept);
        for (Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            request.setHeader(key, value);
        }
    }

    private void setHttpResponseHeaders() {
        if (httpResponse != null) {
            Header[] headers = httpResponse.getAllHeaders();
            for (Header header : headers) {
                responseHeaders.put(header.getName(), header.getValue());
            }
        }
    }
}
