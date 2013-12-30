package com.beancrumbs.nullsafe;

import java.util.HashMap;
import java.util.Map;

public class NullSafe {
	private static Map<Class<?>, Class<?>> accessors = new HashMap<>();
	
	
	public static <T> T $(T obj) {
		return nullsafe(obj);
	}

	public static <T> T nullsafe(T obj) {
		if (obj == null) {
			return null;
		}
		@SuppressWarnings("unchecked")
		Class<T> type = (Class<T>)obj.getClass();
		return newInstance(getAccessor(type), type, obj);
	}

	
	@SuppressWarnings("unchecked")
	private static <C> Class<? extends C> getAccessor(Class<C> c) {
		Class<? extends C> a = (Class<? extends C>) accessors.get(c);
		if (a == null) {
			String accessorClassName = NullSafeAccessorWriter.getAccessorClassName(c.getName());
			try {
				a = (Class<? extends C>) Class.forName(accessorClassName);
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException(e);
			}
			accessors.put(c, a);
		}
		return a;
	}
	
	private static <T> T newInstance(Class<? extends T> clazz, Class<T> argType, T arg) {
		try {
			return clazz.getConstructor(argType).newInstance(arg);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException(e);
		}
	}
}
