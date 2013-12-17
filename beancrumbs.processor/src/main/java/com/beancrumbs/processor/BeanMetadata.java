package com.beancrumbs.processor;

import java.util.LinkedHashMap;
import java.util.Map;

public class BeanMetadata {
	private String fullName;
	private String packageName;
	private String simpleName;
	private String superClassName;
	
	private Map<String, BeanProperty> properties = new LinkedHashMap<String, BeanProperty>();
	
	
	public BeanMetadata(String name, String superName) {
		this.fullName = name;
		this.superClassName = superName;
		String[] parts = splitClassName(name);
		this.packageName = parts[0];
		this.simpleName = parts[1];
	}
	

	public String getFullName() {
		return fullName;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getSimpleName() {
		return simpleName;
	}

	public String getSuperClassName() {
		return superClassName;
	}

	public void setParentName(String parentName) {
		this.superClassName = parentName;
	}
	
	
	public Map<String, BeanProperty> getProperties() {
		return properties;
	}

	public void addBeanProperty(BeanProperty prop) {
		String propName = prop.getName();
		BeanProperty existingProp = properties.get(propName);
		BeanProperty newProp = existingProp == null ? prop : merge(existingProp, prop);
		properties.put(propName, normalize(newProp));
	}

	private BeanProperty merge(BeanProperty dst, BeanProperty src) {
		if (src != null && src.getGetterName() != null) {
			dst.setGetterName(src.getGetterName());
		}
		if (src != null && src.getSetterName() != null) {
			dst.setSetterName(src.getSetterName());
		}
		if (src != null && src.getTypeName() != null) {
			dst.setTypeName(src.getTypeName());
		}
		if (src != null && src.getName() != null) {
			dst.setName(src.getName());
		}

		return dst;
	}
	
	private BeanProperty normalize(BeanProperty src) {
		if (src.getGetterName() != null) {
			src.setReadable(true);
		}
		if (src.getSetterName() != null) {
			src.setWritable(true);
		}
		return src;
	}
	
	private String[] splitClassName(String name) {
		String[] parts = new String[2];
		parts[0] = "";
		parts[1] = name;
		int lastDot = name.lastIndexOf('.');
		if (lastDot > 0) {
			parts[0] = name.substring(0, lastDot);
			parts[1] = name.substring(lastDot + 1);
		}
		
		return parts;
	}
	
	@Override
	public String toString() {
		return fullName + " extends " + superClassName + ": " + properties;
	}
	
}
