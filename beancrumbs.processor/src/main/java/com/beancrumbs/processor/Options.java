package com.beancrumbs.processor;

import java.io.File;
import java.util.Map;

public class Options {
	public final static String BEANCRUMBS_DIR_OPTION = "skeleton_dir";
	public final static String BEANCRUMBS_LOG_OPTION = "skeleton_log";
	public final static String BEANCRUMBS_LOG_LEVEL_OPTION = "skeleton_log_level";
	public final static String BEANCRUMBS_ENABLED_OPTION = "skeleton_enabled";
	
	
	private File dir;
	private File log;
	private LogLevel logLevel = LogLevel.OFF;
	private boolean enabled = true;
	
	
	enum LogLevel {
		OFF, SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST, ALL,
		;
	}
	
	public Options(Map<String,String> values) {
		// TODO calculate default value of directory
		String dirPath = values.get(BEANCRUMBS_DIR_OPTION);
		if (dirPath != null) {
			dir = new File(values.get(BEANCRUMBS_DIR_OPTION));
		}
		
		String logPath = values.get(BEANCRUMBS_LOG_OPTION);
		if (logPath != null) {
			log = new File(logPath);
		}
		
		String logLevelParam = values.get(BEANCRUMBS_LOG_LEVEL_OPTION);
		if (logLevelParam != null) {
			logLevel = LogLevel.valueOf(logLevelParam);
		}
		
		String enabledParam = values.get(BEANCRUMBS_ENABLED_OPTION);
		if (enabledParam != null) {
			enabled = Boolean.parseBoolean(enabledParam);
		}
		
		
	}

	public File getDir() {
		return dir;
	}

	public File getLog() {
		return log;
	}

	public LogLevel getLogLevel() {
		return logLevel;
	}

	public boolean isEnabled() {
		return enabled;
	}
}
