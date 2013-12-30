package com.beanpath.poc;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Test;

import com.beanpath.poc.Phone.Type;

public class PhoneTest<P extends Phone> {
	@Test
	public void skeletonTest() throws ParseException, ReflectiveOperationException {
		P phone = fillPhoneData(createPhone());
		validate(phone);
	}

	
	@SuppressWarnings("unchecked")
	protected P createPhone() throws ParseException, ReflectiveOperationException {
		return (P)new Phone();
	}

	
	protected P fillPhoneData(P phone) throws ParseException, ReflectiveOperationException {
		phone.setNumber("123456789");
		phone.setType(Type.MOBILE);
		return phone;
	}
	
	
	protected void validate(P phone) throws ParseException, ReflectiveOperationException {
		assertEquals(phone.getNumber(), BeanUtils.getProperty(phone, PhoneSkeleton.number));
		assertEquals(String.valueOf(phone.getType()), BeanUtils.getProperty(phone, PhoneSkeleton.type));
	}
	
}
