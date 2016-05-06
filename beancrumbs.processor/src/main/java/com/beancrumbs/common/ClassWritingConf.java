package com.beancrumbs.common;

import java.util.Properties;

public class ClassWritingConf {
	static final String IMPORT = "import"; 
	private boolean importReferences;

	public ClassWritingConf(Properties props) {
		importReferences = Boolean.parseBoolean(props.getProperty(IMPORT, "true"));
	}

	public boolean isImportReferences() {
		return importReferences;
	}
}
