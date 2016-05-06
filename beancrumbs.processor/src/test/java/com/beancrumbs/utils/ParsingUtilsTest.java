package com.beancrumbs.utils;

import org.junit.Test;


import junit.framework.Assert;

public class ParsingUtilsTest {
	@Test
	public void simpleClassNamePrimitive() {
		simpleClassName(int.class.getName(), int.class.getName());
	}

	@Test
	public void simpleClassNameString() {
		simpleClassName(String.class.getName(), String.class.getSimpleName());
	}

	@Test
	public void simpleClassNameListOfString() {
		simpleClassName("java.util.List<java.lang.String>", "List<String>");
	}

	@Test
	public void simpleClassNameSetOfCustomClass() {
		simpleClassName("java.util.Set<com.beanpath.poc.UserRole>", "Set<UserRole>");
	}
	
	
	@Test
	public void simpleClassNameComplexMap() {
		simpleClassName("java.util.Map<java.lang.String, List<com.mycompany.MyClass>>", "Map<String, List<MyClass>>");
	}

	@Test
	public void simpleClassNameFunctionOfCustomTypeToSetOfCustomType() {
		simpleClassName("com.google.common.base.Function<com.beanpath.poc.User, java.util.Set<com.beanpath.poc.UserRole>>", "Function<User, Set<UserRole>>");
	}

	@Test
	public void canonizeClassNamePrimitive() {
		Assert.assertEquals(Integer.class.getSimpleName(), ParsingUtils.canoninize(int.class.getName(), false));
		Assert.assertEquals(Integer.class.getSimpleName(), ParsingUtils.canoninize(int.class.getName(), true));
	}

	@Test
	public void canonizeClassNameString() {
		Assert.assertEquals(String.class.getSimpleName(), ParsingUtils.canoninize(String.class.getName(), true));
	}

	@Test
	public void canonizeClassNameListOfString() {
		canonizeClassName("java.util.List<java.lang.String>", "List<String>");
	}

	@Test
	public void canonizeClassNameSetOfCustomClass() {
		canonizeClassName("java.util.Set<com.beanpath.poc.UserRole>", "Set<UserRole>");
	}
	
	
	@Test
	public void canonizeClassNameComplexMap() {
		canonizeClassName("java.util.Map<java.lang.String, List<com.mycompany.MyClass>>", "Map<String, List<MyClass>>");
	}

	@Test
	public void canonizeClassNameFunctionOfCustomTypeToSetOfCustomType() {
		canonizeClassName("com.google.common.base.Function<com.beanpath.poc.User, java.util.Set<com.beanpath.poc.UserRole>>", "Function<User, Set<UserRole>>");
	}
	
	
	private void simpleClassName(String in, String out) {
		Assert.assertEquals(out, ParsingUtils.simpleClassName(in));
	}
	
	private void canonizeClassName(String in, String out) {
		Assert.assertEquals(in, ParsingUtils.canoninize(in, false));
		Assert.assertEquals(out, ParsingUtils.canoninize(in, true));
	}
	
}
