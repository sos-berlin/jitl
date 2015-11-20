package com.sos.jitl.restclient;
  
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.http.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class JobSchedulerRestClient {
	
	public static String accept="application/json";
	public static HashMap<String,String> headers = new HashMap<String, String>();
  
	 public static String executeRestServiceCommand(String restCommand, String urlParam) throws Exception {
		 String result = "";
		 String s = urlParam.replaceFirst("^([^:]*)://.*$","$1");
		 String protocol = "";
		 
	 
		 if ( s.equals(urlParam)){
			 urlParam = "http://" + urlParam;
		 }
	
		 
		 java.net.URL url = new java.net.URL(urlParam);
		 String host = url.getHost();
		 int port = url.getPort();
		 String path = url.getPath();
		 protocol = url.getProtocol();
		 
		 if (restCommand.equalsIgnoreCase("delete") || restCommand.equalsIgnoreCase("put")){
			 result = String.valueOf(execute(restCommand,url));
		 }else{
			 if (restCommand.equalsIgnoreCase("get")){
				 result = getRestService( host, port, path, protocol);
			 }else{
				 if (restCommand.equalsIgnoreCase("post")){
					 result =  postRestService( host, port, path, protocol);
				 }else{
	             	throw new Exception (String.format("Unknown rest command: %s (usage: get|post|delete|put)",restCommand));
				 }
			 }
		 }
		 
		
		 return result;
	 }
	 
	 public static String executeRestService(String urlParam) throws Exception{
 		 return executeRestServiceCommand("get", urlParam);
	 }
	 
	 public void addHeader(String header, String value){
		 headers.put(header, value);
	 }
	 
	 private static int execute(String command, java.net.URL url) throws IOException{
		 HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		 connection.setRequestMethod(command.toUpperCase());
		 return connection.getResponseCode();
	 }
	 
	  
      public static String getRestService(String host, int port, String path, String protocol) {
          CloseableHttpClient  httpClient = HttpClientBuilder.create().build();
          String s = "";
           
          try {
            HttpHost target = new HttpHost(host, port, protocol);
            HttpGet getRequestGet = new HttpGet(path);
             
            getRequestGet.setHeader("Accept", accept);
         
            HttpResponse httpResponse=null;
            for(Entry<String, String> entry : headers.entrySet()) {
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
       
          } catch (Exception e) {
            e.printStackTrace();
          }  
          return s;              
      }
      
      public static String postRestService(String host, int port, String path, String protocol) {
          CloseableHttpClient  httpClient = HttpClientBuilder.create().build();
          String s = "";
           
          try {
            HttpHost target = new HttpHost(host, port, protocol);
            HttpPost getRequestPost = new HttpPost(path);
             
            getRequestPost.setHeader("Accept", accept);
            HttpResponse httpResponse=null;
            for(Entry<String, String> entry : headers.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();    
                getRequestPost.setHeader(key, value);
            }

            httpResponse = httpClient.execute(target, getRequestPost);

            HttpEntity entity = httpResponse.getEntity();
     
            if (entity != null) {
              s = EntityUtils.toString(entity);
            }
            httpClient.close();
       
          } catch (Exception e) {
            e.printStackTrace();
          }  
          return s;              
      }
      
}
