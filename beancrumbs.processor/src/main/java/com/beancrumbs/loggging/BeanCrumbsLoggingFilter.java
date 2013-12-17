package com.beancrumbs.loggging;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class BeanCrumbsLoggingFilter implements Filter {

	@Override
	public boolean isLoggable(LogRecord record) {
		return record.getLoggerName().startsWith("com.beancrumbs");
	}

}
