package com.beanpath.poc;

import java.util.Date;

import com.beancrumbs.skeleton.Skeleton;

@Skeleton
public class Person {
	private String firstName;
	private String lastName;
	private Date bithday;
	private Address home;
	
	public Address getHome() {
		return home;
	}
	public void setHome(Address home) {
		this.home = home;
	}
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
	
	@Override
	public String toString() {
		return "Person" + firstName + " " + lastName + "";
	}
	
}
