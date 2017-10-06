package com.sos.jitl.notification.helper;

public class ElementTimerScript {

    private String language;
    private String value;
    private String elementTitle;
    private boolean isMinimum = false;

    public ElementTimerScript(String title, boolean minimum, String l, String s) {
        elementTitle = title;
        isMinimum = minimum;
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

    public boolean isMinimum() {
        return isMinimum;
    }
}
