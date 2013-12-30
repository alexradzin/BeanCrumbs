package com.beanpath.poc;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Test;

public class PersonTest<P extends Person> {
	@Test
	public void skeletonTest() throws ParseException, ReflectiveOperationException {
		P person = fillPersonData(createPerson());
		validate(person);
	}

	@SuppressWarnings("unchecked")
	protected P createPerson() throws ParseException, ReflectiveOperationException {
		return (P)new Person();
	}
	
	protected P fillPersonData(P person) throws ParseException, ReflectiveOperationException {
		person.setFirstName("Sherlock");
		person.setLastName("Holmes");
		person.setBithday(new SimpleDateFormat("MMMM DD, yyyy").parse("January 6, 1854")); 
		person.setHome(AddressTest.createAddress());
		
		return person;
	}

	protected void validate(P person) throws ParseException, ReflectiveOperationException {
		assertEquals(person.getFirstName(), BeanUtils.getProperty(person, PersonSkeleton.firstName));
		assertEquals(person.getLastName(), BeanUtils.getProperty(person, PersonSkeleton.lastName));
		assertEquals(String.valueOf(person.getBithday()), BeanUtils.getProperty(person, PersonSkeleton.bithday));

		Address home = person.getHome();
		
		assertEquals(home.getBuilding(), BeanUtils.getProperty(person, PersonSkeleton.home.building));
		assertEquals(home.getStreet(), BeanUtils.getProperty(person, PersonSkeleton.home.street));
		assertEquals(home.getCity(), BeanUtils.getProperty(person, PersonSkeleton.home.city));
		assertEquals(home.getCountry(), BeanUtils.getProperty(person, PersonSkeleton.home.country));
		assertEquals(home.getApartment(), BeanUtils.getProperty(person, PersonSkeleton.home.apartment));
		assertEquals(home.getComment(), BeanUtils.getProperty(person, PersonSkeleton.home.comment));
		assertEquals(home.getZip(), BeanUtils.getProperty(person, PersonSkeleton.home.zip));
		assertEquals(String.valueOf(home.getLatitude()), BeanUtils.getProperty(person, PersonSkeleton.home.latitude));
		assertEquals(String.valueOf(home.getLongitude()), BeanUtils.getProperty(person, PersonSkeleton.home.longitude));
		assertEquals(String.valueOf(home.getPobox()), BeanUtils.getProperty(person, PersonSkeleton.home.pobox));
	}

}
