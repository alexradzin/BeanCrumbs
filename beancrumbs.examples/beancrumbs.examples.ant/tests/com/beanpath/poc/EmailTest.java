package com.beanpath.poc;

import static org.junit.Assert.assertEquals;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Test;

public class EmailTest {
	@Test
	public void test() throws ReflectiveOperationException {
		Email email = new Email();
		email.setEmail("bill.gates@microsoft.com");
		email.setDisplayName("Bill Gates");

		assertEquals(email.getEmail(), BeanUtils.getProperty(email, EmailSkeleton.email));
		assertEquals(email.getDisplayName(), BeanUtils.getProperty(email, EmailSkeleton.displayName));
	}

}
