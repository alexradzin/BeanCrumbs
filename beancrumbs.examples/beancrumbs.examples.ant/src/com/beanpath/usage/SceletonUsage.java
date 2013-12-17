package com.beanpath.usage;

import static com.beanpath.poc.PersonSkeleton.firstName;

import javax.xml.bind.annotation.XmlElement;

import static com.beanpath.poc.PersonSkeleton.lastName;
import static com.beanpath.poc.PersonSkeleton.home;


public class SceletonUsage {
	@XmlElement(name = firstName)
	public String getXXX() {
		return null;
	}

	@XmlElement(name = home.city)
	 String getYYY() {
		return null;
	}

	@XmlElement(name = lastName)
	String getZZZ() {
		return null;
	}
}
