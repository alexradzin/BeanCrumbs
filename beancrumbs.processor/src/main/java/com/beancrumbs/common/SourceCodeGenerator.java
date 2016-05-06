package com.beancrumbs.common;

import com.beancrumbs.processor.BeanProperty;
import com.beancrumbs.processor.BeansMetadata;

public interface SourceCodeGenerator<C extends ClassWritingConf> {
	public String getCode(String simpleClassName, BeansMetadata data, BeanProperty property, C conf);
}
