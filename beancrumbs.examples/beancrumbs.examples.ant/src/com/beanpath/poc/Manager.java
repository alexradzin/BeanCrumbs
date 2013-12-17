package com.beanpath.poc;

import java.util.Collection;

public class Manager extends Employee {
	private Collection<Integer> subordinateIds;
	private int managerId;

	public Collection<Integer> getSubordinateIds() {
		return subordinateIds;
	}

	public void setSubordinateIds(Collection<Integer> subordinateIds) {
		this.subordinateIds = subordinateIds;
	}

	public int getManagerId() {
		return managerId;
	}

	public void setManagerId(int managerId) {
		this.managerId = managerId;
	}
	
	
	
}
