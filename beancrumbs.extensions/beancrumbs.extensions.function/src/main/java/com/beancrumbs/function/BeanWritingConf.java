package com.beancrumbs.function;

import java.util.Properties;
import java.util.regex.Pattern;

import com.beancrumbs.common.AccessModifier;
import com.beancrumbs.common.ClassWritingConf;

public class BeanWritingConf extends ClassWritingConf {
	private static final Pattern WHITESPACE = Pattern.compile("\\s+");

	private final HandlerConf getter;
	private final HandlerConf setter;
	private final HandlerConf predicate;

	BeanWritingConf(Properties props) {
		super(props);
		getter = parse(props, HandlerRole.GETTER);
		setter = parse(props, HandlerRole.SETTER);
		predicate = parse(props, HandlerRole.PREDICATE);
	}

	private HandlerConf parse(Properties props, HandlerRole role) {
		String propertyName = role.propertyName();
		return parse(propertyName, props.getProperty(propertyName));
	}

	private HandlerConf parse(String roleName, String conf) {
		if (conf == null) {
			return null;
		}
		String[] args = WHITESPACE.split(conf);
		HandlerRole role = HandlerRole.byName(roleName);
		switch (args.length) {
		case 2:
			return new HandlerConf(args[0], args[1], role);
		case 3:
			return new HandlerConf(args[0], AccessModifier.byName(args[1]), args[2], role);
		default:
			throw new IllegalArgumentException(conf);
		}
	}

	boolean isValid() {
		return getter != null || setter != null || predicate != null;
	}

	public HandlerConf getGetter() {
		return getter;
	}

	public HandlerConf getSetter() {
		return setter;
	}

	public HandlerConf getPredicate() {
		return predicate;
	}

}