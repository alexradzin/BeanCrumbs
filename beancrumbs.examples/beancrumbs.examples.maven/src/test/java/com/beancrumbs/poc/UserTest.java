package com.beancrumbs.poc;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Test;

import com.beanpath.poc.User;
import com.beanpath.poc.UserFunction;
import com.beanpath.poc.UserRole;
import com.beanpath.poc.UserSkeleton;

public class UserTest {
	@Test
	public void skeletonTest() throws ReflectiveOperationException {
		User user = createUser();
		
		assertEquals(user.getName(), BeanUtils.getProperty(user, UserSkeleton.name));
		assertEquals(user.getPassword(), BeanUtils.getProperty(user, UserSkeleton.password));
		assertEquals(user.isEnabled(), Boolean.valueOf(BeanUtils.getProperty(user, UserSkeleton.enabled)));
		assertEquals(formatCollectionString(String.valueOf(user.getRoles())), formatCollectionString(BeanUtils.getProperty(user, UserSkeleton.roles)));
	}
	

	@Test
	public void functionTest() throws ReflectiveOperationException {
		User user = createUser();
		assertFunctions(user);
		
		UserFunction.name("Piglet").apply(user);
		UserFunction.password("pooh").apply(user);
		UserFunction.enabled(false).apply(user);
		UserFunction.roles(Collections.singleton(UserRole.USER)).apply(user);
		
		assertFunctions(user);
	}

	
	private void assertFunctions(User user) {
		assertEquals(user.getName(), UserFunction.name.apply(user));
		assertEquals(user.getPassword(), UserFunction.password.apply(user));
		assertEquals(user.isEnabled(), UserFunction.enabled.apply(user));
		assertEquals(formatCollectionString(String.valueOf(user.getRoles())), formatCollectionString(UserFunction.roles.apply(user).toString()));
	}
	
	
	private User createUser() {
		User user = new User();
		user.setName("Winnie the Pooh");
		user.setPassword("piglet");
		user.setEnabled(true);
		user.setRoles(Collections.singleton(UserRole.ADMINISTRATOR));
		return user;
	}
	
	private String formatCollectionString(String str) {
		return str.replaceAll("[<>\\[\\]]", "");
	}
}
