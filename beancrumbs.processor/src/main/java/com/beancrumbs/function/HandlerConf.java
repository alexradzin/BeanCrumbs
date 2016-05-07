package com.beancrumbs.function;

import com.beancrumbs.common.AccessModifier;
import com.beancrumbs.common.SourceCodeGenerator;

public class HandlerConf {
	private final String parentClassName;
	private final AccessModifier methodModifier;
	private final String method;
	private final SourceCodeGenerator<BeanWritingConf> codeGenerator;

	public HandlerConf(String parentClassName, String method, SourceCodeGenerator<BeanWritingConf> codeGenerator) {
		this(parentClassName, AccessModifier.PUBLIC, method, codeGenerator);
	}
	
	public HandlerConf(String parentClassName, AccessModifier methodModifier, String method, SourceCodeGenerator<BeanWritingConf> codeGenerator) {
		this.parentClassName = parentClassName;
		this.methodModifier = methodModifier;
		this.method = method;
		this.codeGenerator = codeGenerator;
	}

	public String getParentClassName() {
		return parentClassName;
	}

	public AccessModifier getMethodModifier() {
		return methodModifier;
	}

	public String getMethod() {
		return method;
	}
	
	public SourceCodeGenerator<BeanWritingConf> getSourceCodeGenerator() {
		return codeGenerator;
	}
}