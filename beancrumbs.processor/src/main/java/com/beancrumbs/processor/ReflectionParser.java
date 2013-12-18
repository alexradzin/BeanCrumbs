package com.beancrumbs.processor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class ReflectionParser extends BeanParser<Class<?>, Class<?>, Method> {
	@Override
	protected Set<? extends Class<?>> getTypes() {
		return getElements();
	}

	@Override
	protected Set<? extends Method> getMethods(Class<?> type) {
		return new LinkedHashSet<Method>(Arrays.asList(type.getDeclaredMethods()));
	}

	@Override
	protected String getTypeName(Class<?> type) {
		return type.getName();
	}

	@Override
	protected String getMethodName(Method method) {
		return method.getName();
	}

	@Override
	protected boolean isVoid(Method method) {
		return method.getReturnType().equals(void.class);
	}

	@Override
	protected String[] getMethodParemeterTypes(Method method) {
		Class<?>[] types = method.getParameterTypes();
		String[] typeNames = new String[types.length];
		for (int i = 0;  i < types.length;  i++) {
			typeNames[i] = types[i].getName();
		}
		return typeNames;
	}

	@Override
	protected String getMethodReturnType(Method method) {
		return method.getReturnType().getName();
	}

	@Override
	protected String getSuperTypeName(Class<?> type) {
		Class<?> superClass = type.getSuperclass();
		return superClass == null ? null : superClass.getName();
	}
	
	@Override
	protected boolean isCrumbed(Class<?> type) {
		return type.getAnnotation(Crumbed.class) != null;
	}
	
}
