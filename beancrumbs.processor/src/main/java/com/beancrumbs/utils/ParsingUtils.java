package com.beancrumbs.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class ParsingUtils {
	private static final Map<String, String> primitiveWrappers = new HashMap<>();
	static {
		@SuppressWarnings("rawtypes")
		Class[] primitives = {byte.class, short.class, long.class, float.class, double.class, boolean.class};
		for (@SuppressWarnings("rawtypes") Class primitive : primitives) {
			String name = primitive.getName();
			primitiveWrappers.put(name, ParsingUtils.firstToUpperCase(name));
		}
		primitiveWrappers.put(int.class.getName(), Integer.class.getName());
		primitiveWrappers.put(char.class.getName(), Character.class.getName());
	}
	
	
	
	public static Pattern wildcardToPattern(String wildcard) {
		String regex = wildcard.replace("?", ".").replace("*", ".*");
		return Pattern.compile(regex);
	}
	
	public static Entry<String, String> splitClassName(final String className) {
		String packageName = "";
		String simpleName = className;
		
		int firstLt = className.lastIndexOf('<');
		if (firstLt > 0) {
			simpleName = className.substring(0,  firstLt);
		}
		int lastDot = simpleName.lastIndexOf('.');
		if (lastDot >= 0) {
			packageName = simpleName.substring(0, lastDot);
			simpleName = simpleName.substring(lastDot + 1); 
		}
		
		final String pName = packageName;
		final String cName = simpleName + (firstLt > 0 ? className.substring(firstLt) : "");
		
		return new Entry<String, String>() {
			@Override
			public String getKey() {
				return pName;
			}

			@Override
			public String getValue() {
				return cName;
			}

			@Override
			public String setValue(String value) {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	public static String pureClassName(String className) {
		int posLt = className.indexOf('<');
		return posLt > 0 ? className.substring(0, posLt) : className;
	}
	
	public static String firstToLowerCase(String str) {
		if (str == null || str.length() == 0) {
			return str;
		}
		char first = Character.toLowerCase(str.charAt(0));
		return str.length() > 1 ? first + str.substring(1) : "" + first;
	}

	public static String firstToUpperCase(String str) {
		if (str == null || str.length() == 0) {
			return str;
		}
		char first = Character.toUpperCase(str.charAt(0));
		return str.length() > 1 ? first + str.substring(1) : "" + first;
	}

	public static String canoninize(String type, boolean writeSimpleClassName) {
		String wrapper = primitiveWrappers.get(type);
		String canonicalType = wrapper == null ? type : wrapper;
		return writeSimpleClassName || isJavaLang(canonicalType) ? ParsingUtils.splitClassName(canonicalType).getValue()
				: canonicalType;
	}

	public static String shortClassName(String type) {
		return primitiveWrappers.containsKey(type) ? type : ParsingUtils.splitClassName(type).getValue();
	}

	public static boolean isJavaLang(String fullyQalifiedClassName) {
		return fullyQalifiedClassName.startsWith("java.lang");
	}
	
}
