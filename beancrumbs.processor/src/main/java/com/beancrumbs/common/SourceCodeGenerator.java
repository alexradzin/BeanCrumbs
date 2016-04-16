package com.beancrumbs.common;

import com.beancrumbs.function.ClassWritingConf;
import com.beancrumbs.processor.BeanProperty;

public interface SourceCodeGenerator {
	public String getCode(String simpleClassName, BeanProperty property, ClassWritingConf conf);
}
