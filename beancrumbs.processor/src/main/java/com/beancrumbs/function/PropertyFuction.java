package com.beancrumbs.function;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(SOURCE)
@Target({TYPE, METHOD, FIELD})
public @interface PropertyFuction {
	/**
	 * Enables/disables read function creating
	 * This feature is currently not supported by framework
	 * @return
	 */
	boolean read() default true;
	/**
	 * Enables/disables write function creating. 
	 * This feature is currently not supported by framework
	 * @return
	 */
	boolean write() default true;
}
