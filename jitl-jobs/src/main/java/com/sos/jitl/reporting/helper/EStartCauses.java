package com.sos.jitl.reporting.helper;

public enum EStartCauses {
	ORDER("order"),
	FILE_TRIGGER("file_trigger");
	
	
	private String value;
	private EStartCauses(String startCause){
		value = startCause;
	}
	
	public String value(){
		return value;
	}
}
