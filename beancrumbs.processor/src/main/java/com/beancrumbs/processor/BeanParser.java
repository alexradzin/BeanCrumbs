package com.beancrumbs.processor;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Abstract base class for all bean parsers. 
 * @author alexr
 *
 * @param <E>
 * @param <T>
 * @param <M>
 */
public abstract class BeanParser<E, T, M> {
	private static final Logger logger = Logger.getLogger(BeanParser.class.getName()); 
	private BeansMetadata metadata; 
	
	
	private static final Map<String, Class<?>> primitives = new HashMap<String, Class<?>>();
	static {
		for (Class<?> c : new Class[] {short.class, int.class, long.class, byte.class, boolean.class, float.class, double.class}) {
			primitives.put(c.getName(), c);
		}
	}
	
	
	
	//TODO: pass metadata through constructor and remove this method.
	void setMetadata(BeansMetadata metadata) {
		this.metadata = metadata;
	}


	public void handleTypes(CrumbsWay way) {
		Set<? extends T> elements = getTypes();
		logger.fine("handling types " + elements);
		for (T e : elements) {
			logger.finer("handling type " + e + " by " + way);
			handleType(e, way);
			logger.finer("type is handled " + e);
		}
		
		logger.finer("All found metadata: " + metadata);
	}


	public void handleType(T type, CrumbsWay way) {
		String typeName  = getTypeName(type);
		if (isPrimitive(typeName)) {
			logger.finest("Type " + type + "is not hanlded because it is a primitive.");
			return;
		}
		if (isCrumbed(type)) {
			logger.finest("Type " + type + "is not hanlded because it is already crumbed.");
			return;
		}
		
		
		Set<? extends M> methods = getMethods(type);
		
		if (methods.isEmpty()) {
			metadata.addBeanProperty(typeName, getSuperTypeName(type), null, way);
			logger.fine("addBeanProperty(" + typeName + ",..., " + null + ",...");
		}
		
		for (M method : methods) {
			BeanProperty prop = null;
			
			String methodName = getMethodName(method);
			String propertyType = getMethodReturnType(method);
			if ((methodName.startsWith("get") || methodName.startsWith("is")) && getMethodParemeterTypes(method).length == 0 && !isVoid(method)) {
				// getter
				String propertyName = methodName.replaceFirst("^(get|is)", "");
				propertyName = propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);
				String propertyTypeName = propertyType.toString();
				
				prop = new BeanProperty();
				prop.setName(propertyName);
				prop.setGetterName(methodName);
				prop.setReadable(true);
				prop.setTypeName(propertyTypeName);
			} 
			else if (methodName.startsWith("set") && getMethodParemeterTypes(method).length == 1 && isVoid(method)) {
				// setter
				String propertyName = methodName.replaceFirst("^set", "");
				propertyName = propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);
				String propertyTypeName = getMethodParemeterTypes(method)[0];
				
				prop = new BeanProperty();
				prop.setName(propertyName);
				prop.setSetterName(methodName);
				prop.setWritable(true);
				prop.setTypeName(propertyTypeName);
			}

			if (prop != null) {
				metadata.addBeanProperty(typeName, getSuperTypeName(type), prop, way);
				logger.fine("addBeanProperty(" + typeName + ",..., " + prop + ",...");
			}
		}
		
		
	}

	
	protected boolean isPrimitive(Class<?> clazz) {
		return primitives.containsValue(clazz);
	}

	protected boolean isPrimitive(String className) {
		return primitives.containsKey(className);
	}
	
	protected Class<?> getPrimitiveClassByName(String className) {
		return primitives.get(className);
	}
	
	
	
	protected abstract Set<? extends E> getElements();
	protected abstract Set<? extends T> getTypes();
	protected abstract Set<? extends M> getMethods(T type);
	protected abstract String getTypeName(T type);
	protected abstract String getSuperTypeName(T type);
	protected abstract String getMethodName(M method);
	protected abstract boolean isVoid(M method);
	protected abstract String[] getMethodParemeterTypes(M method);
	protected abstract String getMethodReturnType(M method);
	protected abstract boolean isCrumbed(T type);
}
