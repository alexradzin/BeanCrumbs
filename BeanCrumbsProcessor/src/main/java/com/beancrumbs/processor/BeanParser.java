package com.beancrumbs.processor;

import java.util.Set;
import java.util.logging.Logger;

public abstract class BeanParser<E, T, M> {
	private final static Logger logger = Logger.getLogger(BeanParser.class .getName()); 
	private BeansMetadata metadata; 
	
	
	
	protected BeansMetadata getMetadata() {
		return metadata;
	}


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
	}


	public void handleType(T type, CrumbsWay way) {
		String typeName  = getTypeName(type);
		
		
		for (M method : getMethods(type)) {
			
			String methodName = getMethodName(method);
			
			String propertyType = getMethodReturnType(method);
			if (!((methodName.startsWith("get") || methodName.startsWith("is")) && getMethodParemeterTypes(method).length == 0 && !isVoid(method))) {
				continue;
			}
			
			String propertyName = methodName.replaceFirst("^(get|is)", "");
			propertyName = propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);
			String propertyTypeName = propertyType.toString();
			
			BeanProperty prop = new BeanProperty();
			prop.setName(propertyName);
			prop.setGetterName(methodName);
			prop.setReadable(true);
			prop.setTypeName(propertyTypeName);
			metadata.addBeanProperty(typeName, getSuperTypeName(type), prop, way);
			logger.fine("addBeanProperty(" + typeName + ",..., " + prop + ",...");
		}
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
}
