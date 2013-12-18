package com.beanpath.poc;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;

import org.apache.commons.beanutils.BeanUtils;

public class FaxTest extends PhoneTest<Fax> {

	@Override
	protected Fax createPhone() throws ParseException, ReflectiveOperationException {
		return new Fax();
	}

	
	@Override
	protected Fax fillPhoneData(Fax fax) throws ParseException, ReflectiveOperationException {
		fax.setNumber("123456789");
		return fax;
	}
	
	
	@Override
	protected void validate(Fax fax) throws ParseException, ReflectiveOperationException {
		assertEquals(fax.getNumber(), BeanUtils.getProperty(fax, FaxSkeleton.number));
		assertEquals(String.valueOf(fax.getType()), BeanUtils.getProperty(fax, FaxSkeleton.type));
	}

}
