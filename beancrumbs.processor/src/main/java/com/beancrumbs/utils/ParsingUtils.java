package com.beancrumbs.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
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

	private static final Pattern genericTypeSeparator = Pattern.compile("\\s*[<>,]\\s*");
	
	
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
	
	public static String[] componentClassNames(String className) {
		return genericTypeSeparator.split(className);
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
		return writeSimpleClassName || isJavaLang(canonicalType) ? ParsingUtils.simpleClassName(canonicalType)
				: canonicalType;
	}

	public static String simpleClassName(String type) {
		if (isPrimitive(type)) {
			return type;
		}
		
		
		String[] parts = splitIncludeDelimeter(genericTypeSeparator, type);
		StringBuilder result = new StringBuilder();
		for (String part : parts) {
			if ("".equals(part) || genericTypeSeparator.matcher(part).matches()) {
				result.append(part);
			} else {
				String simpleClassName = isPrimitive(part) ? type : ParsingUtils.splitClassName(part).getValue();
				result.append(simpleClassName);
			}
		}
		return result.toString();
	}

	
	

	private static String[] splitIncludeDelimeter(Pattern pattern, String text) {
		List<String> list = new ArrayList<>();
		Matcher matcher = pattern.matcher(text);

		int now, old = 0;
		while (matcher.find()) {
			now = matcher.end();
			list.add(text.substring(old, now - 1));
			list.add(text.substring(now - 1, now));
			//System.out.println(list);
			old = now;
		}

		if (list.size() == 0)
			return new String[] { text };

		// adding rest of a text as last element
		String finalElement = text.substring(old);
		list.add(finalElement);

		return list.toArray(new String[list.size()]);
	}	
	
	
	public static boolean isJavaLang(String fullyQalifiedClassName) {
		return fullyQalifiedClassName.startsWith("java.lang");
	}
	
	public static boolean isPrimitive(String fullyQalifiedClassName) {
		return primitiveWrappers.containsKey(fullyQalifiedClassName);
	}
}
