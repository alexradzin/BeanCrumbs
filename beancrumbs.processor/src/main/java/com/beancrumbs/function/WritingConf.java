package com.beancrumbs.function;

import java.util.Properties;

class WritingConf {
	private boolean importReferences;
	
	private final HandlerConf getter;
	private final HandlerConf setter;
	private final HandlerConf predicate;
	
	
	WritingConf(Properties props) {
		importReferences = Boolean.parseBoolean(props.getProperty(FunctionWriter.IMPORT, "true"));
		getter = parse(props, HandlerRole.GETTER);
		setter = parse(props, HandlerRole.SETTER);
		predicate = parse(props, HandlerRole.PREDICATE);
	}
	
	private HandlerConf parse(Properties props, HandlerRole role) {
		String propertyName = role.propertyName();
		return HandlerConf.parse(propertyName, props.getProperty(propertyName));
	}
	

	boolean isValid() {
		return getter != null || setter != null || predicate != null;
	}

	public boolean isImportReferences() {
		return importReferences;
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