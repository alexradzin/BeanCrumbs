package com.beanpath.poc;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class Email {
	private String displayName;
	private String email;
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	
}
