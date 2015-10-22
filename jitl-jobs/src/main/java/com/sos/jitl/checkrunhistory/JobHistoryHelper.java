package com.sos.jitl.checkrunhistory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JobHistoryHelper {
	
	
	public String getParameter(String p){
		p = p.trim();
		String s="";
		
		Pattern pattern = Pattern.compile("^.*\\(([^\\)]*)\\)$", Pattern.DOTALL + Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(p);

        if (matcher.find()) {
            s = matcher.group(1);
        } 
		return s;
	}
	
	public String getMethodName(String p){
		p = p.trim();
		String s=p;
		
		Pattern pattern = Pattern.compile("^([^\\(]*)\\(.*\\)$", Pattern.DOTALL + Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(p);

        if (matcher.find()) {
            s = matcher.group(1);
        } 
		 
		return s.trim();
	}	
	
	public String getTime(String defaultValue,String p){
		String param = getParameter(p);
		if (param.length() == 0){
           param = defaultValue;			
		}
		return param;
	}
}
