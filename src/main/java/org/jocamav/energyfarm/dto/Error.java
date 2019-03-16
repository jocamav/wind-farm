package org.jocamav.energyfarm.dto;

public class Error {
	private String code;
	private String description;
	
	public Error(String code, String description) {
		super();
		this.code = code;
		this.description = description;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}
	
	
	
}
