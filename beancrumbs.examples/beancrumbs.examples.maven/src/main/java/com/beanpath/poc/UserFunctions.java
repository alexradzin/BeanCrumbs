package com.beanpath.poc;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import java.util.Set;

@com.beancrumbs.processor.Crumbed(com.beancrumbs.function.PropertyFuction.class)
public class UserFunctions {
	public static final Function<User, String> name = new Function<User, String>() {
		@Override
		public String apply(User user) {
			return user.getName();
		}
	};

	public static final Function<User, String> name(final String name) {
		return new Function<User, String>() {
			@Override
			public String apply(User user) {
				String prev = user.getName();
				user.setName(name);
				return prev;
			}
		};
	}

	public static final Function<User, String> password = new Function<User, String>() {
		@Override
		public String apply(User user) {
			return user.getPassword();
		}
	};

	public static final Function<User, String> password(final String password) {
		return new Function<User, String>() {
			@Override
			public String apply(User user) {
				String prev = user.getPassword();
				user.setPassword(password);
				return prev;
			}
		};
	}

	public static final Function<User, Set<com.beanpath.poc.UserRole>> roles = new Function<User, Set<com.beanpath.poc.UserRole>>() {
		@Override
		public Set<com.beanpath.poc.UserRole> apply(User user) {
			return user.getRoles();
		}
	};

	public static final Function<User, Set<com.beanpath.poc.UserRole>> roles(final Set<com.beanpath.poc.UserRole> roles) {
		return new Function<User, Set<com.beanpath.poc.UserRole>>() {
			@Override
			public Set<com.beanpath.poc.UserRole> apply(User user) {
				Set<com.beanpath.poc.UserRole> prev = user.getRoles();
				user.setRoles(roles);
				return prev;
			}
		};
	}

	public static final Predicate<User> enabled = new Predicate<User>() {
		@Override
		public boolean apply(User user) {
			return user.isEnabled();
		}
	};

	public static final Function<User, Boolean> enabled(final boolean enabled) {
		return new Function<User, Boolean>() {
			@Override
			public Boolean apply(User user) {
				Boolean prev = user.isEnabled();
				user.setEnabled(enabled);
				return prev;
			}
		};
	}

}
