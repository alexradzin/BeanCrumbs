package com.beanpath.poc;

public class Phone {
	public static enum Type {
		FIXED, MOBILE, FAX;
	}
	
	
	private String number;
	private Type type;

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
}
