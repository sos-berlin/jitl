package com.sos.jitl.reporting.helper;

import org.w3c.dom.NodeList;

import sos.xml.SOSXMLXPath;

public class ReportXmlHelper {

	/**
	 * 
	 * @param xpath
	 * @return
	 * @throws Exception
	 */
	public static NodeList getRootChilds(SOSXMLXPath xpath) throws Exception{
		return xpath.selectNodeList("/" + xpath.root.getNodeName()+ "/*");
	}
	
	/**
	 * 
	 * @param xpath
	 * @return
	 * @throws Exception
	 */
	public static String getJobChainStartCause(SOSXMLXPath xpath) throws Exception{
		String root = xpath.root.getNodeName();
		boolean hasFileOrderSink = !ReportXmlHelper.isElementEmpty(xpath, "/"+root+"/file_order_source");
		String startCase = EStartCauses.ORDER.value();
		if(hasFileOrderSink){
			startCase = EStartCauses.FILE_TRIGGER.value();
		}
		return startCase;
	}
	
	/**
	 * 
	 * @param xpath
	 * @return
	 */
	public static String getTitle(SOSXMLXPath xpath){
		return xpath.root.getAttribute("title");
	}
	
	/**
	 * 
	 * @param xpath
	 * @return
	 */
	public static boolean isOrderJob(SOSXMLXPath xpath){
		return xpath.root.getAttribute("order") != null && xpath.root.getAttribute("order").toLowerCase().equals("yes") ? true : false;
	}
	
	/**
	 * 
	 * @param xpath
	 * @return
	 * @throws Exception
	 */
	public static boolean isRuntimeDefined(SOSXMLXPath xpath) throws Exception{
		String root = xpath.root.getNodeName();
		boolean isRuntimeDefined = !ReportXmlHelper.isElementEmpty(xpath,"/"+root+"/run_time[1]/*");
		if(!isRuntimeDefined){
			isRuntimeDefined = !ReportXmlHelper.isElementEmpty(xpath,"/"+root+"/run_time[1][@schedule and string-length(@schedule)!=0]");
		}
		return isRuntimeDefined;
	}
	
	/**
	 * 
	 * @param xpath
	 * @param expr
	 * @throws Exception
	 */
	private static boolean isElementEmpty(SOSXMLXPath xpath,String expr) throws Exception{
		NodeList nl = xpath.selectNodeList(expr);
		if(nl != null && nl.item(0) != null){
			return false;
		}
		return true;
	}

}
