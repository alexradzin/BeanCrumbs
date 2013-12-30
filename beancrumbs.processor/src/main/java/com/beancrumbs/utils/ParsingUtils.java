package com.beancrumbs.utils;

import java.util.Map.Entry;
import java.util.regex.Pattern;

public class ParsingUtils {
	public static Pattern wildcardToPattern(String wildcard) {
		String regex = wildcard.replace("?", ".").replace("*", ".*");
		return Pattern.compile(regex);
	}
	
	public static Entry<String, String> splitClassName(String className) {
		String packageName = "";
		String simpleName = className;
		int lastDot = className.lastIndexOf('.');
		if (lastDot >= 0) {
			packageName = className.substring(0, lastDot);
			simpleName = className.substring(lastDot + 1); 
		}
		
		final String pName = packageName;
		final String cName = simpleName;
		
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
}
