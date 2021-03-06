package com.sos.jitl.restclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.HostnameVerifier;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.sos.exception.SOSBadRequestException;
import com.sos.exception.SOSConnectionRefusedException;
import com.sos.exception.SOSConnectionResetException;
import com.sos.exception.SOSException;
import com.sos.exception.SOSNoResponseException;

public class JobSchedulerRestApiClient {

    private String accept = "application/json";
    private String basicAuthorization = null;
    private HashMap<String, String> headers = new HashMap<String, String>();
    private HashMap<String, String> responseHeaders = new HashMap<String, String>();
    private RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
    private CredentialsProvider credentialsProvider = null;
    private HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
    private HttpResponse httpResponse;
    private HttpRequestRetryHandler httpRequestRetryHandler;
    private CloseableHttpClient httpClient = null;
    private boolean forcedClosingHttpClient = false;
    private boolean autoCloseHttpClient = true;

    public HttpResponse getHttpResponse() {
        return httpResponse;
    }

    public void setAccept(String accept) {
        this.accept = accept;
    }

    public void setBasicAuthorization(String basicAuthorization) {
        this.basicAuthorization = basicAuthorization;
    }

    public String getBasicAuthorization() {
        return basicAuthorization;
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
     * the time (in milliseconds) to establish the connection with the remote host
     */
    public void setConnectionTimeout(int connectionTimeout) {
        requestConfigBuilder.setConnectTimeout(connectionTimeout);
    }

    /*
     * the timeout in milliseconds used when requesting a connection from the connection manager.
     */
    public void setConnectionRequestTimeout(int connectionTimeout) {
        requestConfigBuilder.setConnectionRequestTimeout(connectionTimeout);
    }

    /*
     * the time (in milliseconds) waiting for data after the connection was established; maximum time of inactivity between two data packets
     */
    public void setSocketTimeout(int socketTimeout) {
        requestConfigBuilder.setSocketTimeout(socketTimeout);
    }

    public void setAllowAllHostnameVerifier(boolean flag) {
        if (flag) {
            this.hostnameVerifier = NoopHostnameVerifier.INSTANCE;
        } else {
            // null = SSLConnectionSocketFactory.getDefaultHostnameVerifier()
            this.hostnameVerifier = null;
        }
    }

    public void setHttpRequestRetryHandler(HttpRequestRetryHandler handler) {
        httpRequestRetryHandler = handler;
    }

    public void setProxy(String proxyHost, Integer proxyPort) {
        setProxy(proxyHost, proxyPort, null, null);
    }

    public void setProxy(String proxyHost, Integer proxyPort, String proxyUser, String proxyPassword) {
        requestConfigBuilder.setProxy(new HttpHost(proxyHost, proxyPort));
        if (proxyUser != null && !proxyUser.isEmpty()) {
            credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(new AuthScope(proxyHost, proxyPort), new UsernamePasswordCredentials(proxyUser, proxyPassword));
        }
    }

    public void createHttpClient() {
        if (httpClient == null) {
            HttpClientBuilder builder = HttpClientBuilder.create();
            if (httpRequestRetryHandler != null) {
                builder.setRetryHandler(httpRequestRetryHandler);
            } else {
                builder.setRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
            }
            if (credentialsProvider != null) {
                builder.setDefaultCredentialsProvider(credentialsProvider);
            }
            if (hostnameVerifier != null) {
                builder.setSSLHostnameVerifier(hostnameVerifier);
            }
            httpClient = builder.setDefaultRequestConfig(requestConfigBuilder.build()).build();
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
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (Exception e) {
        } finally {
            httpClient = null;
        }
    }

    public void forcedClosingHttpClient() {
        forcedClosingHttpClient = true;
        try {
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (Exception e) {
        } finally {
            httpClient = null;
        }
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

    public String executeRestServiceCommand(String restCommand, String urlParam) throws SOSException {
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

    public String executeRestServiceCommand(String restCommand, URL url) throws SOSException {
        return executeRestServiceCommand(restCommand, url, null);
    }

    public String executeRestServiceCommand(String restCommand, URI uri) throws SOSException {
        return executeRestServiceCommand(restCommand, uri, null);
    }

    public String executeRestServiceCommand(String restCommand, URL url, String body) throws SOSException {

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

    public String executeRestServiceCommand(String restCommand, URI uri, String body) throws SOSException {

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

    public String executeRestService(String urlParam) throws SOSException {
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
                throw new SOSNoResponseException(url.toString(), e);
            }
        } catch (SOSNoResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new SOSConnectionRefusedException(url.toString(), e);
        } finally {
            try {
                connection.disconnect();
            } catch (Exception e) {
            }
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
    
    public String deleteRestService(URI uri) throws SOSException {
        return getStringResponse(new HttpDelete(uri));
    }

    public String getRestService(HttpHost target, String path) throws SOSException {
        return getStringResponse(target, new HttpGet(path));
    }

    public String getRestService(URI uri) throws SOSException {
        return getStringResponse(new HttpGet(uri));
    }

    public byte[] getByteArrayByRestService(URI uri) throws SOSException {
        return getByteArrayResponse(new HttpGet(uri));
    }

    public Path getFilePathByRestService(URI uri, String prefix, boolean withGzipEncoding) throws SOSException {
        return getFilePathResponse(new HttpGet(uri), prefix, withGzipEncoding);
    }

    public String postRestService(HttpHost target, String path, String body) throws SOSException {
        HttpPost requestPost = new HttpPost(path);
        try {
            if (body != null && !body.isEmpty()) {
                StringEntity entity = new StringEntity(body, StandardCharsets.UTF_8);
                requestPost.setEntity(entity);
            }
        } catch (Exception e) {
            throw new SOSBadRequestException(body, e);
        }
        return getStringResponse(target, requestPost);
    }

    public String postRestService(URI uri, String body) throws SOSException {
        HttpPost requestPost = new HttpPost(uri);
        try {
            if (body != null && !body.isEmpty()) {
                StringEntity entity = new StringEntity(body, StandardCharsets.UTF_8);
                requestPost.setEntity(entity);
            }
        } catch (Exception e) {
            throw new SOSBadRequestException(body, e);
        }
        return getStringResponse(requestPost);
    }

    public String putRestService(HttpHost target, String path, String body) throws SOSException {
        HttpPut requestPut = new HttpPut(path);
        try {
            if (body != null && !body.isEmpty()) {
                StringEntity entity = new StringEntity(body, StandardCharsets.UTF_8);
                requestPut.setEntity(entity);
            }
        } catch (Exception e) {
            throw new SOSBadRequestException(body, e);
        }
        return getStringResponse(target, requestPut);
    }

    public String putRestService(URI uri, String body) throws SOSException {
        HttpPut requestPut = new HttpPut(uri);
        try {
            if (body != null && !body.isEmpty()) { //ByteArrayEntity
                StringEntity entity = new StringEntity(body, StandardCharsets.UTF_8);
                requestPut.setEntity(entity);
            }
        } catch (Exception e) {
            throw new SOSBadRequestException(body, e);
        }
        return getStringResponse(requestPut);
    }
    
    public String putByteArrayRestService(URI uri, byte[] body) throws SOSException {
        HttpPut requestPut = new HttpPut(uri);
        try {
            if (body != null) {
                ByteArrayEntity entity = new ByteArrayEntity(body);
                requestPut.setEntity(entity);
            }
        } catch (Exception e) {
            throw new SOSBadRequestException(e);
        }
        return getStringResponse(requestPut);
    }

    private String getStringResponse(HttpHost target, HttpRequest request) throws SOSException {
        httpResponse = null;
        createHttpClient();
        setHttpRequestHeaders(request);
        try {
            httpResponse = httpClient.execute(target, request);
            return getResponse();
        } catch (SOSException e) {
            closeHttpClient();
            throw e;
        } catch (ClientProtocolException e) {
            closeHttpClient();
            throw new SOSConnectionRefusedException(e);
        } catch (SocketTimeoutException e) {
            closeHttpClient();
            throw new SOSNoResponseException(e);
        } catch (HttpHostConnectException e) {
            closeHttpClient();
            throw new SOSConnectionRefusedException(e);
        } catch (SocketException e) {
            closeHttpClient();
            if ("connection reset".equalsIgnoreCase(e.getMessage())) {
                throw new SOSConnectionResetException(e);
            }
            throw new SOSConnectionRefusedException(e);
        } catch (Exception e) {
            closeHttpClient();
            throw new SOSConnectionRefusedException(e);
        }
    }

    private String getStringResponse(HttpUriRequest request) throws SOSException {
        httpResponse = null;
        createHttpClient();
        setHttpRequestHeaders(request);
        try {
            httpResponse = httpClient.execute(request);
            return getResponse();
        } catch (SOSException e) {
            closeHttpClient();
            throw e;
        } catch (ClientProtocolException e) {
            closeHttpClient();
            throw new SOSConnectionRefusedException(e);
        } catch (SocketTimeoutException e) {
            closeHttpClient();
            throw new SOSNoResponseException(e);
        } catch (HttpHostConnectException e) {
            closeHttpClient();
            throw new SOSConnectionRefusedException(e);
        } catch (SocketException e) {
            closeHttpClient();
            if ("connection reset".equalsIgnoreCase(e.getMessage())) {
                throw new SOSConnectionResetException(e);
            }
            throw new SOSConnectionRefusedException(e);
        } catch (Exception e) {
            closeHttpClient();
            throw new SOSConnectionRefusedException(e);
        }
    }

    private byte[] getByteArrayResponse(HttpUriRequest request) throws SOSException {
        httpResponse = null;
        createHttpClient();
        setHttpRequestHeaders(request);
        try {
            httpResponse = httpClient.execute(request);
            return getByteArrayResponse();
        } catch (SOSException e) {
            closeHttpClient();
            throw e;
        } catch (ClientProtocolException e) {
            closeHttpClient();
            throw new SOSConnectionRefusedException(e);
        } catch (SocketTimeoutException e) {
            closeHttpClient();
            throw new SOSNoResponseException(e);
        } catch (HttpHostConnectException e) {
            closeHttpClient();
            throw new SOSConnectionRefusedException(e);
        } catch (SocketException e) {
            closeHttpClient();
            if ("connection reset".equalsIgnoreCase(e.getMessage())) {
                throw new SOSConnectionResetException(e);
            }
            throw new SOSConnectionRefusedException(e);
        } catch (Exception e) {
            closeHttpClient();
            throw new SOSConnectionRefusedException(e);
        }
    }

    private Path getFilePathResponse(HttpUriRequest request, String prefix, boolean withGzipEncoding) throws SOSException {
        httpResponse = null;
        createHttpClient();
        setHttpRequestHeaders(request);
        try {
            httpResponse = httpClient.execute(request);
            return getFilePathResponse(prefix, withGzipEncoding);
        } catch (SOSException e) {
            closeHttpClient();
            throw e;
        } catch (ClientProtocolException e) {
            closeHttpClient();
            throw new SOSConnectionRefusedException(e);
        } catch (SocketTimeoutException e) {
            closeHttpClient();
            throw new SOSNoResponseException(e);
        } catch (HttpHostConnectException e) {
            closeHttpClient();
            throw new SOSConnectionRefusedException(e);
        } catch (SocketException e) {
            closeHttpClient();
            if ("connection reset".equalsIgnoreCase(e.getMessage())) {
                throw new SOSConnectionResetException(e);
            }
            throw new SOSConnectionRefusedException(e);
        } catch (Exception e) {
            closeHttpClient();
            throw new SOSConnectionRefusedException(e);
        }
    }

    private String getResponse() throws SOSNoResponseException {
        try {
            String s = "";
            setHttpResponseHeaders();
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                s = EntityUtils.toString(entity, "UTF-8");
            }
            if (isAutoCloseHttpClient()) {
                closeHttpClient();
            }
            return s;
        } catch (Exception e) {
            closeHttpClient();
            throw new SOSNoResponseException(e);
        }
    }

    private byte[] getByteArrayResponse() throws SOSNoResponseException {
        try {
            byte[] is = null;
            setHttpResponseHeaders();
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                is = EntityUtils.toByteArray(entity);
            }
            if (isAutoCloseHttpClient()) {
                closeHttpClient();
            }
            return is;
        } catch (Exception e) {
            closeHttpClient();
            throw new SOSNoResponseException(e);
        }
    }

    private Path getFilePathResponse(String prefix, boolean withGzipEncoding) throws SOSNoResponseException {
        Path path = null;
        try {
            setHttpResponseHeaders();
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                InputStream instream = entity.getContent();
                OutputStream out = null;
                if (instream != null) {
                    try {
                        if (prefix == null) {
                            prefix = "sos-download-";
                        }
                        path = Files.createTempFile(prefix, null);
                        if (withGzipEncoding) {
                            out = new GZIPOutputStream(Files.newOutputStream(path));
                        } else {
                            out = Files.newOutputStream(path);
                        }
                        byte[] buffer = new byte[4096];
                        int length;
                        while ((length = instream.read(buffer)) > 0) {
                            out.write(buffer, 0, length);
                        }
                        out.flush();
                    } finally {
                        try {
                            instream.close();
                            instream = null;
                        } catch (Exception e) {
                        }
                        try {
                            if (out != null) {
                                out.close();
                            }
                        } catch (Exception e) {
                        }
                    }
                }
            }
            if (isAutoCloseHttpClient()) {
                closeHttpClient();
            }
            return path;
        } catch (Exception e) {
            if (path != null) {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e1) {
                }
            }
            closeHttpClient();
            throw new SOSNoResponseException(e);
        } catch (Throwable e) {
            if (path != null) {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e1) {
                }
            }
            closeHttpClient();
            throw e;
        }
    }

    private void setHttpRequestHeaders(HttpRequest request) {
        request.setHeader("Accept", accept);
        if (basicAuthorization != null && !basicAuthorization.isEmpty()) {
            request.setHeader("Authorization", "Basic " + basicAuthorization);
        }
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

    public void addAuthorizationHeader(String user, String password) {
        String s = user + ":" + password;
        byte[] authEncBytes = Base64.encodeBase64(s.getBytes());
        String authStringEnc = new String(authEncBytes);
        addHeader("Authorization", "Basic " + authStringEnc);
    }

    public String addAuthorizationHeader(String jocAccount) throws SOSException {
        if (jocAccount == null) {
            throw new SOSException("There is no valid joc account");
        }
        String user = "";
        String password = "";

        String[] s = jocAccount.split(":");
        if (s.length > 0) {
            user = s[0];
        }
        if (s.length > 1) {
            for (int i=1;i<s.length;i++) {
            	password = password + ":" + s[i];
            }
            password = password.substring(1);
        }
        addAuthorizationHeader(user, password);
        return user;
    }
}