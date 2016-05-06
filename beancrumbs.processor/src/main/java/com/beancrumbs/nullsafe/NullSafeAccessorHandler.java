package com.beancrumbs.nullsafe;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.beancrumbs.common.SourceCodeGenerator;
import com.beancrumbs.processor.BeanProperty;
import com.beancrumbs.processor.BeansMetadata;

public enum NullSafeAccessorHandler implements SourceCodeGenerator<NullSafeConf> {
	CONSTRUCTOR(
			"	public %1$sNullSafeAccessor(%1$s instance) {%n" + 
			"		this.instance = instance;%n" + 
			"	}%n" 
		) {
		@Override
		public String getCode(String simpleClassName, BeansMetadata data, BeanProperty property, NullSafeConf conf) {
			return String.format(codeTemplate(), simpleClassName);
		}
	},
	ACCESSOR(
			"	@Override%n" + 
			"	public %1$s %2$s() {%n" + 
			"		return %3$s;%n" + 
			"	}%n" + 
			""
		) {
		@Override
		public String getCode(String simpleClassName, BeansMetadata data, BeanProperty property, NullSafeConf conf) {
			String type = property.getTypeName();
			

			String fieldName = property.getName();
			logger.info("Writing null-safe accessible property: " + type + " " + fieldName);

			String getterName = (boolean.class.getSimpleName().equals(type) ? "is" : "get")
					+ fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

			String defaultValue = getDefaultValue(type);
			String directAccessExpression = "instance == null ? " + defaultValue + " : instance." + getterName + "()";
			String accessExpression = directAccessExpression;
			if (data.getBeanMetadata(type) != null) {
				String typeAccessor = getAccessorClassName(type);
				// accessExpression = directAccessExpression + " == null ? new "
				// + typeAccessor + "() : " + directAccessExpression;
				accessExpression = "new " + typeAccessor + "(" + directAccessExpression + ")";
			}
			
			return String.format(codeTemplate(), 
					property.getTypeName(), 
					property.getGetterName(),
					accessExpression
			);
		}
	},
	;
	
	private static final Logger logger = Logger.getLogger(NullSafeAccessorWriter.class.getName());
	private static final Map<String, String> defaultValuesPerType = new HashMap<>();
	static {
		defaultValuesPerType.put("byte", "0");
		defaultValuesPerType.put("char", "0");
		defaultValuesPerType.put("short", "0");
		defaultValuesPerType.put("int", "0");
		defaultValuesPerType.put("long", "0L");
		defaultValuesPerType.put("float", "0.0f");
		defaultValuesPerType.put("double", "0.0");
		defaultValuesPerType.put("boolean", "false");
		//TODO: add arrays, collections, lists, sets, maps
	}
	
	private static final String CLASS_NAME_SUFFIX = "NullSafeAccessor";
	
	private final String codeTemplate;
	
	private NullSafeAccessorHandler(String codeTemplate) {
		this.codeTemplate = codeTemplate;
	}

	String codeTemplate() {
		return codeTemplate;
	}
	
	private static String getDefaultValue(String typeName) {
		return defaultValuesPerType.get(typeName);
	}

	static String getAccessorClassName(String beanClassName) {
		return beanClassName + CLASS_NAME_SUFFIX;
	}
	
}
