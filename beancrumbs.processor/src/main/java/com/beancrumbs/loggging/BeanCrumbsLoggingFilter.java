package com.beancrumbs.loggging;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

/**
 * {@code java.util.logging} provides {@code Filter} interface but no filter implementation. 
 * This simple hard coded implementation is useful to configure log that will store only 
 * BeanCrumbs relevant information. 
 * @author alexr
 */
public class BeanCrumbsLoggingFilter implements Filter {

	@Override
	public boolean isLoggable(LogRecord record) {
		return record.getLoggerName().startsWith("com.beancrumbs");
	}

}
