package com.beanpath.poc;

import static org.junit.Assert.assertEquals;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Test;

public class AddressTest {
	@Test
	public void skeletonTest() throws ReflectiveOperationException {
		Address address = createAddress();
		
		assertEquals(address.getBuilding(), BeanUtils.getProperty(address, AddressSkeleton.building));
		assertEquals(address.getStreet(), BeanUtils.getProperty(address, AddressSkeleton.street));
		assertEquals(address.getCity(), BeanUtils.getProperty(address, AddressSkeleton.city));
		assertEquals(address.getCountry(), BeanUtils.getProperty(address, AddressSkeleton.country));
		assertEquals(address.getApartment(), BeanUtils.getProperty(address, AddressSkeleton.apartment));
		assertEquals(address.getComment(), BeanUtils.getProperty(address, AddressSkeleton.comment));
		assertEquals(address.getZip(), BeanUtils.getProperty(address, AddressSkeleton.zip));
		assertEquals(String.valueOf(address.getLatitude()), BeanUtils.getProperty(address, AddressSkeleton.latitude));
		assertEquals(String.valueOf(address.getLongitude()), BeanUtils.getProperty(address, AddressSkeleton.longitude));
		assertEquals(String.valueOf(address.getPobox()), BeanUtils.getProperty(address, AddressSkeleton.pobox));
	}
	
	static Address createAddress() {
		Address address = new Address();
		address.setBuilding("221B");
		address.setStreet("Baker Street");
		address.setCity("London");
		address.setCountry("GB");
		address.setApartment("1");
		address.setComment("The address of  Sherlock Holmes");
		address.setZip("NW1 6XE");
		address.setLatitude(51.5237038);
		address.setLongitude(-0.1585531);
		address.setPobox(11111);
		
		return address;
	}
}
