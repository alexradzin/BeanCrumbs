package com.beanpath.poc;

import static com.beancrumbs.nullsafe.NullSafe.$;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Ignore;
import org.junit.Test;

public class PersonTest<P extends Person> {
	@Test
	public void skeletonTest() throws ParseException, ReflectiveOperationException {
		P person = fillPersonData(createPerson());
		validate(person);
	}


	@Test
	public void personWithParents() throws ParseException, ReflectiveOperationException {
		P billGates = validate(fillPersonData(createPerson(), "Bill", "Gates", "October 28, 1955"));
		P billGatesSr = validate(fillPersonData(createPerson(), "William", "Gates", "November 30, 1925"));
		P maryGates = validate(fillPersonData(createPerson(), "Mary", "Gates", "July 5, 1929"));
		
		billGates.setMother(maryGates);
		
		assertEquals("Mary", BeanUtils.getProperty(billGates, PersonSkeleton.mother.firstName));
		
	}
	
	@Ignore(PersonSkeleton.mother.birthday)
	public void test() {
		String s = PersonSkeleton.mother.birthday;
	}


	@SuppressWarnings("unchecked")
	protected P createPerson() throws ParseException, ReflectiveOperationException {
		return (P)new Person();
	}

	
	protected P fillPersonData(P person) throws ParseException, ReflectiveOperationException {
		return fillPersonData(person, "Sherlock", "Holmes", "January 6, 1854");
	}
	
	protected P fillPersonData(P person, String firstName, String lastName, String birthday) throws ParseException, ReflectiveOperationException {
		person.setFirstName(firstName);
		person.setLastName(lastName);
		person.setBithday(new SimpleDateFormat("MMMM DD, yyyy", Locale.US).parse(birthday)); 
		person.setHome(AddressTest.createAddress());
		
		return person;
	}

	protected P validate(P person) throws ParseException, ReflectiveOperationException {
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
		
		return person;
	}
	
	@Test
	public void nullsafeArray() {
		Person p = new Person();
		assertNull(p.getPhones());
		assertNotNull($(p).getPhones());
		assertEquals(0, $(p).getPhones().length);
	}
}
