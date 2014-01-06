package com.beanpath.poc;

import java.util.Set;

import javax.persistence.Entity;

import com.beancrumbs.nullsafe.NullSafeAccess;

@Entity
@NullSafeAccess
public class User {
	private String name;
	private String password;
	private Set<UserRole> roles;
	private boolean enabled;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Set<UserRole> getRoles() {
		return roles;
	}
	public void setRoles(Set<UserRole> roles) {
		this.roles = roles;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	
	
}
