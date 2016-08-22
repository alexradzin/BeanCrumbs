package com.beancrumbs.poc;

//import static com.beancrumbs.nullsafe.NullSafe.$;
import static com.beanpath.poc.UserFunction.enabled;
import static com.beanpath.poc.UserFunction.name;
import static com.beanpath.poc.UserFunction.password;
import static com.beanpath.poc.UserFunction.roles;
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
		
		name("Piglet").apply(user);
		password("pooh").apply(user);
		enabled(false).apply(user);
		roles(Collections.singleton(UserRole.USER)).apply(user);
		
		assertFunctions(user);
	}
	/*
	@Test
	public void nullsafeCollection() {
		User user = new User();
		assertNull(user.getRoles());
		assertNotNull($(user).getRoles());
		assertEquals(0, $(user).getRoles().size());
	}
	*/

	
	private void assertFunctions(User user) {
		assertEquals(user.getName(), name.apply(user));
		assertEquals(user.getPassword(), password.apply(user));
		assertEquals(user.isEnabled(), enabled.apply(user));
		assertEquals(formatCollectionString(String.valueOf(user.getRoles())), formatCollectionString(roles.apply(user).toString()));
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
