package com.beanpath.poc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.text.ParseException;
import java.util.Collections;

import org.apache.commons.beanutils.BeanUtils;

public class ManagerTest<M extends Manager> extends EmployeeTest<M> {
	@SuppressWarnings("unchecked")
	@Override
	protected M createPerson() throws ParseException, ReflectiveOperationException {
		return (M) new Manager();
	}

	@Override
	protected M fillPersonData(M manager) throws ParseException, ReflectiveOperationException {
		super.fillPersonData(manager);
		
		manager.setManagerId(1);
		manager.setSubordinateIds(Collections.singleton(2));
		
		return manager;
	}

	
	
	@Override
	protected M validate(M manager) throws ParseException, ReflectiveOperationException {
		// Copy-paste from PersonTest. We have to do this because this class refers to EmployeeSkeleton
		// while PersonTest refers to PersonSkeleton
		assertEquals(manager.getFirstName(), BeanUtils.getProperty(manager, EmployeeSkeleton.firstName));
		assertEquals(manager.getLastName(), BeanUtils.getProperty(manager, EmployeeSkeleton.lastName));
		assertEquals(String.valueOf(manager.getBithday()), BeanUtils.getProperty(manager, EmployeeSkeleton.bithday));

		Address home = manager.getHome();
		
		assertEquals(home.getBuilding(), BeanUtils.getProperty(manager, EmployeeSkeleton.home.building));
		assertEquals(home.getStreet(), BeanUtils.getProperty(manager, EmployeeSkeleton.home.street));
		assertEquals(home.getCity(), BeanUtils.getProperty(manager, EmployeeSkeleton.home.city));
		assertEquals(home.getCountry(), BeanUtils.getProperty(manager, EmployeeSkeleton.home.country));
		assertEquals(home.getApartment(), BeanUtils.getProperty(manager, EmployeeSkeleton.home.apartment));
		assertEquals(home.getComment(), BeanUtils.getProperty(manager, EmployeeSkeleton.home.comment));
		assertEquals(home.getZip(), BeanUtils.getProperty(manager, EmployeeSkeleton.home.zip));
		assertEquals(String.valueOf(home.getLatitude()), BeanUtils.getProperty(manager, EmployeeSkeleton.home.latitude));
		assertEquals(String.valueOf(home.getLongitude()), BeanUtils.getProperty(manager, EmployeeSkeleton.home.longitude));
		assertEquals(String.valueOf(home.getPobox()), BeanUtils.getProperty(manager, EmployeeSkeleton.home.pobox));
		
		// Employee
		assertEquals(String.valueOf("" + manager.getEmployeeId()), BeanUtils.getProperty(manager, EmployeeSkeleton.employeeId));
		assertEquals(String.valueOf("" + manager.getManagerEmployeeId()), BeanUtils.getProperty(manager, EmployeeSkeleton.managerEmployeeId));
		
		// Manager
		assertEquals(String.valueOf("" + manager.getManagerId()), BeanUtils.getProperty(manager, ManagerSkeleton.managerId));
		
		if (manager.getSubordinateIds() == null) {
			assertNull(BeanUtils.getProperty(manager, ManagerSkeleton.subordinateIds));
		} else {
			assertEquals(formatCollectionString(String.valueOf(manager.getSubordinateIds())), formatCollectionString(BeanUtils.getProperty(manager, ManagerSkeleton.subordinateIds)));
		}
		
		return manager;
	}

	
	private String formatCollectionString(String str) {
		return str == null ? "" : str.replaceAll("[<>\\[\\]]", "");
	}
	
}
