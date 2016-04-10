package com.beancrumbs.utils;

import java.util.Map.Entry;
import java.util.regex.Pattern;

public class ParsingUtils {
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
	
}
