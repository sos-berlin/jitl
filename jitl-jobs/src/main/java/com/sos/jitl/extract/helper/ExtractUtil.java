package com.sos.jitl.extract.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sos.util.SOSDate;

public class ExtractUtil {

    public static boolean hasDateReplacement(String fileName) throws Exception {
        return fileName.contains("[date:");
    }

    public static String getDateReplacement(String fileName) throws Exception {
        String regExp = "(.*)(\\[date:)(\\s*)([yYmMDdhHsS_]+)(\\s*)(\\])(.*)";
        StringBuilder sb = new StringBuilder();
        Pattern pattern = Pattern.compile(regExp);
        Matcher matcher = pattern.matcher(fileName);
        boolean found = matcher.find();
        if (found) {
            if (matcher.group(1) != null && matcher.group(1) != null) {
                sb.append(matcher.group(1));
            }
            if (matcher.group(4) == null && matcher.group(1).isEmpty()) {
                throw new Exception("Could not find date mask to convert!!");
            } else {
                sb.append(SOSDate.getCurrentTimeAsString(matcher.group(4)));
            }
            sb.append(matcher.group(7));
            fileName = sb.toString();
        }
        return fileName;
    }

}
