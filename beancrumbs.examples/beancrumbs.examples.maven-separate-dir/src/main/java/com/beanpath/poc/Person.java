package com.beanpath.poc;

import java.util.Date;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class Person {
	private String firstName;
	private String lastName;
	private Date bithday;
	private Person spouse;
	
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String name) {
		this.firstName = name;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public Date getBithday() {
		return bithday;
	}
	public void setBithday(Date bithday) {
		this.bithday = bithday;
	}
	
	public Person getSpouse() {
		return spouse;
	}
	public void setSpouse(Person spouse) {
		this.spouse = spouse;
	}
	@Override
	public String toString() {
		return "Person" + firstName + " " + lastName + "";
	}
}
