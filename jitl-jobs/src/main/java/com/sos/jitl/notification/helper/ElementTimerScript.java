package com.sos.jitl.notification.helper;

public class ElementTimerScript {

    private String language;
    private String value;
    private String elementTitle;

    public ElementTimerScript(String title, String l, String s) {
        elementTitle = title;
        language = l;
        value = s;
    }

    public String getLanguage() {
        return language;
    }

    public String getValue() {
        return value;
    }

    public String getElementTitle() {
        return elementTitle;
    }
}
