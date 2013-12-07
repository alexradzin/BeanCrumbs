package com.beancrumbs.processor;

public class BeanProperty {
	private String name;
	private String typeName;
	private boolean readable;
	private boolean writable;
	private String getterName;
	private String setterName;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTypeName() {
		return typeName;
	}
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	public boolean isReadable() {
		return readable;
	}
	public void setReadable(boolean readable) {
		this.readable = readable;
	}
	public boolean isWritable() {
		return writable;
	}
	public void setWritable(boolean writable) {
		this.writable = writable;
	}
	public String getGetterName() {
		return getterName;
	}
	public void setGetterName(String getterName) {
		this.getterName = getterName;
	}
	public String getSetterName() {
		return setterName;
	}
	public void setSetterName(String setterName) {
		this.setterName = setterName;
	}
	
	
}
