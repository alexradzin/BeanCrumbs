package com.beancrumbs.nullsafe;

import static com.beancrumbs.utils.ParsingUtils.canoninize;
import static java.lang.String.format;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.beancrumbs.utils.ParsingUtils;

class DefaultValueGenerator {
	private static final Class<?>[] collectionClasses = {
		Iterator.class, ListIterator.class, Enumeration.class, Collection.class, List.class, Set.class, SortedSet.class, Map.class, SortedMap.class	
	};
	private static final Class<?>[] primitives = {
		byte.class, char.class, short.class, int.class, long.class, float.class, double.class, boolean.class
	};
	
	private static final String JAVA_LANG = "java.lang";
	private static final String JAVA_UTIL = "java.util";
	private static final String JAVA_UTIL_CONCURRENT = "java.util.concurrent";
	
	private static final String JAVA_LANG_PREFIX = JAVA_LANG + '.';
	private static final String JAVA_UTIL_PREFIX = JAVA_UTIL + '.';
	private static final String JAVA_UTIL_CONCURRENT_PREFIX = JAVA_UTIL_CONCURRENT + '.';
	
	
	private static final Map<String, String> defaultValuesPerType = new HashMap<>();
	static {
		for (Class<?> primitive : primitives) {
			defaultValuesPerType.put(primitive.getName(), "0");
		}
		defaultValuesPerType.put(long.class.getName(), "0L");
		defaultValuesPerType.put(float.class.getName(), "0.0f");
		defaultValuesPerType.put(double.class.getName(), "0.0");
		defaultValuesPerType.put(boolean.class.getName(), "false");
		
		for (Class<?> clazz : collectionClasses) {
			defaultValuesPerType.put(clazz.getName(), format("%s.<GENERICS>empty%s()", Collections.class.getName(), clazz.getSimpleName()));
		}
		defaultValuesPerType.put(Collection.class.getName(), format("%s.<GENERICS>emptyList()", Collections.class.getName()));
		defaultValuesPerType.put(ConcurrentMap.class.getName(), format("new %s<GENERICS>()", ConcurrentHashMap.class.getName()));
	}
	private static final Map<String, String> importsPerType = new HashMap<>();
	static {
		for (Class<?> clazz : collectionClasses) {
			importsPerType.put(clazz.getName(), Collections.class.getName());
		}
		importsPerType.put(ConcurrentMap.class.getName(), ConcurrentHashMap.class.getName());
	}

	static String getDefaultValue(String typeName, boolean writeSimpleClassName) {
		String[] parts = ParsingUtils.typeDefinitionParts(typeName);
		if (typeName.endsWith("[]")) {
			// array
			String elementTypeName = canoninize(parts[0].substring(0, parts[0].length() - 2), writeSimpleClassName);
			return String.format("new %s[0]", elementTypeName);
		}
		
		String defaultValue = defaultValuesPerType.get(parts[0]);
		if (defaultValue != null) {
			String generics = parts.length < 2 || parts[1] == null ? "" : String.format("<%s>", parts[1]); 
			defaultValue = defaultValue.replace("<GENERICS>", canoninize(generics, writeSimpleClassName));
			if (writeSimpleClassName) {
				defaultValue = defaultValue.replace(JAVA_UTIL_CONCURRENT_PREFIX, "").replace(JAVA_UTIL_PREFIX, "");
			}
			defaultValue = defaultValue.replace(JAVA_LANG_PREFIX, "");
		}
		return defaultValue;
	}
	
	
	static String getImportFor(String typeName, boolean writeSimpleClassName) {
		String[] parts = ParsingUtils.typeDefinitionParts(typeName);
		return importsPerType.get(parts[0]);
	}
	
	static boolean isContainer(String type) {
		return importsPerType.containsKey(type);
	}
}
