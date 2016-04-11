package com.beancrumbs.function;

import java.util.regex.Pattern;

class HandlerConf {
	private static final Pattern WHITESPACE = Pattern.compile("\\s+");
	private final String parentClassName;
	private final AccessModifier methodModifier;
	private final String method;
	private final HandlerRole role;

	public HandlerConf(String parentClassName, String method, HandlerRole role) {
		this(parentClassName, AccessModifier.PUBLIC, method, role);
	}
	
	public HandlerConf(String getterClass, AccessModifier getterMethodModifier, String getterMethod, HandlerRole role) {
		this.parentClassName = getterClass;
		this.methodModifier = getterMethodModifier;
		this.method = getterMethod;
		this.role = role;
	}

	static HandlerConf parse(String roleName, String conf) {
		if (conf == null) {
			return null;
		}
		String[] args = WHITESPACE.split(conf);
		HandlerRole role = HandlerRole.byName(roleName);
		switch (args.length) {
			case 2:
				return new HandlerConf(args[0], args[1], role);
			case 3:
				return new HandlerConf(args[0], AccessModifier.byName(args[1]), args[2], role);
			default:
				throw new IllegalArgumentException(conf);
		}
	}

	public String getParentClassName() {
		return parentClassName;
	}

	public AccessModifier getMethodModifier() {
		return methodModifier;
	}

	public String getMethod() {
		return method;
	}
	
	public HandlerRole getRole() {
		return role;
	}
}