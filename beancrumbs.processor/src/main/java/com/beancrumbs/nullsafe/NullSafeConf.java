package com.beancrumbs.nullsafe;

import java.util.Properties;

public class NullSafeConf {
	private boolean importReferences;
	
	NullSafeConf(Properties props) {
		importReferences = Boolean.parseBoolean(props.getProperty(NullSafeAccessorWriter.IMPORT, "true"));
	}

	public boolean isImportReferences() {
		return importReferences;
	}
}
