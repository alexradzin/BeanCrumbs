package com.beanpath.poc;

public class EmployeeSkeleton {
	public final static String employeeId = "employeeId";
	public final static String managerEmployeeId = "managerEmployeeId";
	// Inherited from com.beanpath.poc.Person
	public static class home {
		public final static String $ = "home";
		public final static String zip = $ + ".zip";
		public final static String country = $ + ".country";
		public final static String city = $ + ".city";
		public final static String street = $ + ".street";
		public final static String building = $ + ".building";
		public final static String apartment = $ + ".apartment";
		public final static String latitude = $ + ".latitude";
		public final static String longitude = $ + ".longitude";
		public final static String pobox = $ + ".pobox";
		public final static String comment = $ + ".comment";
	}
	public final static String firstName = "firstName";
	public final static String lastName = "lastName";
	public final static String bithday = "bithday";
}
