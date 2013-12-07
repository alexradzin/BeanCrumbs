package com.beancrumbs.processor;

import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.util.Collection;

public interface CrumbsWay {
	public void strew(String fullClassName, BeansMetadata data, OutputStream stream);
	public Collection<Class<? extends Annotation>> getMarkers();
	public String getName();
	public String getClassName(String originalClassName);
}
