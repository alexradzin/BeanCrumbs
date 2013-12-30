package com.beancrumbs.poc;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Test;

import com.beanpath.poc.User;
import com.beanpath.poc.UserRole;
import com.beanpath.poc.UserSkeleton;

public class UserTest {
	@Test
	public void skeletonTest() throws ReflectiveOperationException {
		User user = new User();
		user.setName("Winnie the Pooh");
		user.setPassword("piglet");
		user.setEnabled(true);
		user.setRoles(Collections.singleton(UserRole.ADMINISTRATOR));
		
		assertEquals(user.getName(), BeanUtils.getProperty(user, UserSkeleton.name));
		assertEquals(user.getPassword(), BeanUtils.getProperty(user, UserSkeleton.password));
		assertEquals(user.isEnabled(), Boolean.valueOf(BeanUtils.getProperty(user, UserSkeleton.enabled)));
		assertEquals(formatCollectionString(String.valueOf(user.getRoles())), formatCollectionString(BeanUtils.getProperty(user, UserSkeleton.roles)));
	}
	
	
	private String formatCollectionString(String str) {
		return str.replaceAll("[<>\\[\\]]", "");
	}
}
