package com.beanpath.poc;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class Magazine {
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
