package com.beancrumbs.nullsafe;

import java.util.Properties;

import com.beancrumbs.common.AccessModifier;
import com.beancrumbs.common.ClassWritingConf;
import com.beancrumbs.common.HandlerConf;

public class NullSafeConf extends ClassWritingConf {
	private final HandlerConf accessor;
	
	
	public NullSafeConf(Properties props) {
		super(props);
		accessor = new HandlerConf(null, AccessModifier.PUBLIC, null, NullSafeAccessorHandler.ACCESSOR);
	}

	public HandlerConf getAccessor() {
		return accessor;
	}
	
}
