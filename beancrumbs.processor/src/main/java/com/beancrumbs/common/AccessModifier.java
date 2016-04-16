package com.beancrumbs.common;

public enum AccessModifier {
	PUBLIC, PROTECTED, PACKAGE;
	
	public static AccessModifier byName(String name) {
		return valueOf(name.toUpperCase());
	}
	
	public String code() {
		return name().toLowerCase();
	}
	
	@Override
	public String toString() {
		return code();
	}
}