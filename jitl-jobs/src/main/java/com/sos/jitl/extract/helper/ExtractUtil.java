package com.sos.jitl.extract.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sos.util.SOSDate;

public class ExtractUtil {
	
	/**
	 * 
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public static boolean hasDateReplacement(String fileName) throws Exception {
		return fileName.contains("[date:");
	}
	
	/**
	 * 
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public static String getDateReplacement(String fileName) throws Exception {
		String regExp = "(.*)(\\[date:)(\\s*)([yYmMDdhHsS_]+)(\\s*)(\\])(.*)";
		/*
		 group(0): our string itself
		 group(1): prefix
		 group(2): [date:
		 group(3): white space
		 group(4): yyyyMMdd_HHmmss
		 group(5): white space
		 group(6): ]
		 group(7): rest of string
		 */

		StringBuffer sb = new StringBuffer();
		Pattern pattern = Pattern.compile(regExp);
		Matcher matcher = pattern.matcher(fileName);
		boolean found = matcher.find();
		if (found) {
			if (matcher.group(1) != null && matcher.group(1) != null)
				sb.append(matcher.group(1));

			if (matcher.group(4) == null && matcher.group(1).length() == 0)
				throw new Exception("Could not find date mask to convert!!");
			else
				sb.append(SOSDate.getCurrentTimeAsString(matcher.group(4)));
			sb.append(matcher.group(7));
			fileName = sb.toString();
		}// found

		return fileName;
	}

}
