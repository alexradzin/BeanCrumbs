package com.beancrumbs.processor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class BeansMetadata {
	/**
	 * Contains map from fully qualified class name to Bean metadata.
	 */
	private Map<String, BeanMetadata> properties = new HashMap<String, BeanMetadata>();
	
	/**
	 * Contains names of classes referenced from processed beans.  
	 */
	private Collection<String> referencedClassNames = new HashSet<String>();
	
	/**
	 * Contains names of classes per type of "crumb way". 
	 * Some classes could be probably processed for different "crumb ways". 
	 */
	private Map<CrumbsWay, Collection<String>> typedClassNames = new HashMap<CrumbsWay, Collection<String>>(); 
	
	
	public void addBeanProperty(String className, String superClassName, BeanProperty prop, CrumbsWay way) {
		BeanMetadata beanMetadata = properties.get(className);
		if (beanMetadata == null) {
			beanMetadata = new BeanMetadata(className, superClassName);
			properties.put(className, beanMetadata);
		}
		beanMetadata.addBeanProperty(prop);
		
		Collection<String> typedClasses = typedClassNames.get(way);
		if (typedClasses == null) {
			typedClasses = new HashSet<String>();
			typedClassNames.put(way, typedClasses);
		}
		typedClasses.add(className);
		System.out.println("typedClassNames: " + typedClassNames);
		

		// the class is now defined. Remove it from list of classes that do not have definition yet. 
		referencedClassNames.remove(className);
		
		// current class probably refers to other classes. 
		// Add them to the list of classes that need definition 
		// unless these classes have been already discovered
		
		String propTypeName = prop.getTypeName();
		if (shouldBeDiscovered(propTypeName) && !properties.containsKey(propTypeName)) {
			referencedClassNames.add(propTypeName);
		}
	}
	
	public Collection<String> getReferencedClassNames() {
		return referencedClassNames;
	}
	
	
	// TODO: make this method configurable. 
	boolean shouldBeDiscovered(String className) {
		if (className.startsWith("java.") || className.startsWith("javax.") || className.startsWith("com.sun.") || className.startsWith("sun.")) {
			 return false;
		}
		return true;
	}
	

	public Collection<String> getBeanNames() {
		return properties.keySet();
	}

	public Collection<String> getBeanNames(CrumbsWay way) {
		System.out.println("typedClassNames: " + typedClassNames);
		Collection<String> result = typedClassNames.get(way); 
		return result == null ? Collections.<String>emptyList() : result;
	}
	
	public BeanMetadata getBeanMetadata(String name) {
		return properties.get(name);
	}
}
