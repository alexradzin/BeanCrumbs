package com.beanpath.poc;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;

import org.apache.commons.beanutils.BeanUtils;

public class EmployeeTest<E extends Employee> extends PersonTest<E> {
	@SuppressWarnings("unchecked")
	@Override
	protected E createPerson() throws ParseException, ReflectiveOperationException {
		return (E)new Employee();
	}

	@Override
	protected E fillPersonData(E employee) throws ParseException, ReflectiveOperationException {
		super.fillPersonData(employee);
		
		employee.setEmployeeId(1);
		employee.setManagerEmployeeId(0);
		
		return employee;
	}

	
	
	@Override
	protected E validate(E employee) throws ParseException, ReflectiveOperationException {
		// Copy-paste from PersonTest. We have to do this because this class refers to EmployeeSkeleton
		// while PersonTest refers to PersonSkeleton
		assertEquals(employee.getFirstName(), BeanUtils.getProperty(employee, EmployeeSkeleton.firstName));
		assertEquals(employee.getLastName(), BeanUtils.getProperty(employee, EmployeeSkeleton.lastName));
		assertEquals(String.valueOf(employee.getBithday()), BeanUtils.getProperty(employee, EmployeeSkeleton.bithday));

		Address home = employee.getHome();
		
		assertEquals(home.getBuilding(), BeanUtils.getProperty(employee, EmployeeSkeleton.home.building));
		assertEquals(home.getStreet(), BeanUtils.getProperty(employee, EmployeeSkeleton.home.street));
		assertEquals(home.getCity(), BeanUtils.getProperty(employee, EmployeeSkeleton.home.city));
		assertEquals(home.getCountry(), BeanUtils.getProperty(employee, EmployeeSkeleton.home.country));
		assertEquals(home.getApartment(), BeanUtils.getProperty(employee, EmployeeSkeleton.home.apartment));
		assertEquals(home.getComment(), BeanUtils.getProperty(employee, EmployeeSkeleton.home.comment));
		assertEquals(home.getZip(), BeanUtils.getProperty(employee, EmployeeSkeleton.home.zip));
		assertEquals(String.valueOf(home.getLatitude()), BeanUtils.getProperty(employee, EmployeeSkeleton.home.latitude));
		assertEquals(String.valueOf(home.getLongitude()), BeanUtils.getProperty(employee, EmployeeSkeleton.home.longitude));
		assertEquals(String.valueOf(home.getPobox()), BeanUtils.getProperty(employee, EmployeeSkeleton.home.pobox));
		
		assertEquals(String.valueOf("" + employee.getEmployeeId()), BeanUtils.getProperty(employee, EmployeeSkeleton.employeeId));
		assertEquals(String.valueOf("" + employee.getManagerEmployeeId()), BeanUtils.getProperty(employee, EmployeeSkeleton.managerEmployeeId));
		
		return employee;
	}
	
}
