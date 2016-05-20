package com.beancrumbs.nullsafe;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentMap;

import org.junit.Test;

import junit.framework.Assert;

public class DefaultValueGeneratorTest {
	@Test
	public void getDefaultValueForPrimitive() {
		for (boolean flag : new boolean[] {true, false}) {
			getDefaultValue("byte", flag, "0");
			getDefaultValue("char", flag, "0");
			getDefaultValue("short", flag, "0");
			getDefaultValue("int", flag, "0");
			getDefaultValue("long", flag, "0L");
			getDefaultValue("short", flag, "0");
			getDefaultValue("boolean", flag, "false");
		}
		
	}

	@Test
	public void getDefaultValueForCollectionWithoutGenerics() {
		getDefaultValue(Collection.class, false, "java.util.Collections.emptyList()");
		getDefaultValue(Iterator.class, false, "java.util.Collections.emptyIterator()");
		getDefaultValue(Enumeration.class, false, "java.util.Collections.emptyEnumeration()");
		getDefaultValue(ListIterator.class, false, "java.util.Collections.emptyListIterator()");
		getDefaultValue(List.class, false, "java.util.Collections.emptyList()");
		getDefaultValue(List.class, false, "java.util.Collections.emptyList()");
		getDefaultValue(Set.class, false, "java.util.Collections.emptySet()");
		getDefaultValue(Map.class, false, "java.util.Collections.emptyMap()");
		getDefaultValue(SortedSet.class, false, "java.util.Collections.emptySortedSet()");
		getDefaultValue(ConcurrentMap.class, false, "new java.util.concurrent.ConcurrentHashMap()");
	}

	@Test
	public void getDefaultValueForCollectionWithoutGenericsWithImport() {
		getDefaultValue(Collection.class, true, "Collections.emptyList()");
		getDefaultValue(Iterator.class, true, "Collections.emptyIterator()");
		getDefaultValue(Enumeration.class, true, "Collections.emptyEnumeration()");
		getDefaultValue(ListIterator.class, true, "Collections.emptyListIterator()");
		getDefaultValue(List.class, true, "Collections.emptyList()");
		getDefaultValue(List.class, true, "Collections.emptyList()");
		getDefaultValue(Set.class, true, "Collections.emptySet()");
		getDefaultValue(Map.class, true, "Collections.emptyMap()");
		getDefaultValue(SortedSet.class, true, "Collections.emptySortedSet()");
		getDefaultValue(ConcurrentMap.class, true, "new ConcurrentHashMap()");
	}

	@Test
	public void getDefaultValueForCollectionOfStrings() {
		getDefaultValue("java.util.Collection<java.lang.String>", false, "java.util.Collections.<String>emptyList()");
		getDefaultValue("java.util.Iterator<java.lang.String>", false, "java.util.Collections.<String>emptyIterator()");
		getDefaultValue("java.util.Enumeration<java.lang.String>", false, "java.util.Collections.<String>emptyEnumeration()");
		getDefaultValue("java.util.ListIterator<java.lang.String>", false, "java.util.Collections.<String>emptyListIterator()");
		getDefaultValue("java.util.List<java.lang.String>", false, "java.util.Collections.<String>emptyList()");
		getDefaultValue("java.util.Set<java.lang.String>", false, "java.util.Collections.<String>emptySet()");
		getDefaultValue("java.util.Map<java.lang.String, java.lang.Long>", false, "java.util.Collections.<String, Long>emptyMap()");
		getDefaultValue("java.util.SortedSet<java.lang.String>", false, "java.util.Collections.<String>emptySortedSet()");
		getDefaultValue("java.util.concurrent.ConcurrentMap<java.lang.String, java.lang.Integer>", false, "new java.util.concurrent.ConcurrentHashMap<String, Integer>()");
	}
	
	@Test
	public void getDefaultValueForCollectionWithGenericsWithImport() {
		getDefaultValue("java.util.Collection<java.lang.String>", true, "Collections.<String>emptyList()");
		getDefaultValue("java.util.Iterator<java.lang.String>", true, "Collections.<String>emptyIterator()");
		getDefaultValue("java.util.Enumeration<java.lang.String>", true, "Collections.<String>emptyEnumeration()");
		getDefaultValue("java.util.ListIterator<java.lang.String>", true, "Collections.<String>emptyListIterator()");
		getDefaultValue("java.util.List<java.lang.String>", true, "Collections.<String>emptyList()");
		getDefaultValue("java.util.Set<java.lang.String>", true, "Collections.<String>emptySet()");
		getDefaultValue("java.util.Map<java.lang.String, java.lang.Long>", true, "Collections.<String, Long>emptyMap()");
		getDefaultValue("java.util.SortedSet<java.lang.String>", true, "Collections.<String>emptySortedSet()");
		getDefaultValue("java.util.concurrent.ConcurrentMap<java.lang.String, java.lang.Integer>", true, "new ConcurrentHashMap<String, Integer>()");
	}
	
	@Test
	public void getDefaultValueForCollectionWithCustomGenerics() {
		getDefaultValue("java.util.Collection<com.company.MyClass>", true, "Collections.<MyClass>emptyList()");
		getDefaultValue("java.util.concurrent.ConcurrentMap<com.company.MyKey, com.company.MyValue>", true, "new ConcurrentHashMap<MyKey, MyValue>()");

		getDefaultValue("java.util.Collection<com.company.MyClass>", false, "java.util.Collections.<com.company.MyClass>emptyList()");
		getDefaultValue("java.util.concurrent.ConcurrentMap<com.company.MyKey, com.company.MyValue>", false, "new java.util.concurrent.ConcurrentHashMap<com.company.MyKey, com.company.MyValue>()");
	}
	

	private void getDefaultValue(Class<?> type, boolean writeSimpleClassName, String expected) {
		getDefaultValue(type.getName(), writeSimpleClassName, expected);
	}
	
	
	private void getDefaultValue(String typeName, boolean writeSimpleClassName, String expected) {
		String actual = DefaultValueGenerator.getDefaultValue(typeName, writeSimpleClassName);
		Assert.assertEquals(expected, actual);
	}
}
