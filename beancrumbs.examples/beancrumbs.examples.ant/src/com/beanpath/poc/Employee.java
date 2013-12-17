package com.beanpath.poc;

import com.beancrumbs.skeleton.Skeleton;

@Skeleton
public class Employee extends Person {
	private int employeeId;
//	private int managerEmployeeId;

	public int getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(int employeeId) {
		this.employeeId = employeeId;
	}

//	public int getManagerEmployeeId() {
//		return managerEmployeeId;
//	}
//
//	public void setManagerEmployeeId(int managerEmployeeId) {
//		this.managerEmployeeId = managerEmployeeId;
//	}

	
}
