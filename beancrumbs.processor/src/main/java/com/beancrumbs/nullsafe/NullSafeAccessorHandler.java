package com.beancrumbs.nullsafe;

import static com.beancrumbs.nullsafe.DefaultValueGenerator.isContainer;
import static com.beancrumbs.utils.ParsingUtils.isArray;
import static com.beancrumbs.utils.ParsingUtils.pureClassName;
import static java.lang.String.format;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import com.beancrumbs.common.SourceCodeGenerator;
import com.beancrumbs.processor.BeanProperty;
import com.beancrumbs.processor.BeansMetadata;
import com.beancrumbs.utils.ParsingUtils;

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
			"		%3$s%n" + 
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

			String defaultValue = DefaultValueGenerator.getDefaultValue(type, conf.isImportReferences());
			String typeName = property.getTypeName();
			AccessLine line = AccessLine.getAccessLine(pureClassName(typeName));
			try {
				PrintWriter pw = new PrintWriter(new FileOutputStream("c:/temp/import.txt", true));
				pw.println(pureClassName(typeName) + "->" + line);
				pw.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			if (conf.isImportReferences()) {
				typeName = ParsingUtils.simpleClassName(typeName);
			}
		
			
			
			
			String directAccessExpression = format(line.codeTemplate(), defaultValue, getterName, typeName);
			
			
			String accessExpression = directAccessExpression;
			if (data.getBeanMetadata(type) != null) {
				String typeAccessor = getAccessorClassName(type);
				if (conf.isImportReferences()) {
					typeAccessor = ParsingUtils.simpleClassName(typeAccessor);
				}
				// accessExpression = directAccessExpression + " == null ? new "
				// + typeAccessor + "() : " + directAccessExpression;
				accessExpression = "new " + typeAccessor + "(" + directAccessExpression + ")";
			}
			accessExpression += ";";

			if (AccessLine.SCALAR.equals(line)) {
				accessExpression = "return " + accessExpression;
			}

			return String.format(codeTemplate(), 
					typeName,
					property.getGetterName(),
					accessExpression
			);
		}
	},
	;
	
	private static final Logger logger = Logger.getLogger(NullSafeAccessorWriter.class.getName());
	private static final String CLASS_NAME_SUFFIX = "NullSafeAccessor";
	
	private final String codeTemplate;
	
	private NullSafeAccessorHandler(String codeTemplate) {
		this.codeTemplate = codeTemplate;
	}

	String codeTemplate() {
		return codeTemplate;
	}
	
	static String getAccessorClassName(String beanClassName) {
		return beanClassName + CLASS_NAME_SUFFIX;
	}

	enum AccessLine {
		SCALAR("instance == null ? %1$s : instance.%2$s()"),
		/**
		 * Used for arrays, collections, maps etc.
		 */
		CONTAINER(
				//"instance == null ? %1$s : instance.%2$s() == null ? %1$s : instance.%2$s()"
				"if (instance == null) {%n" + 
				"			return %1$s;%n" + 
				"		}%n" + 
				"		%3$s value = instance.%2$s();%n" + 
				"		if (value == null) {%n" + 
				"			return %1$s;%n" + 
				"		}%n" + 
				"		return value" 
				
		),
		;
		private String codeTemplate;
		
		AccessLine(String codeTemplate) {
			this.codeTemplate = codeTemplate;
		}
		
		String codeTemplate() {
			return codeTemplate;
		}
		
		static AccessLine getAccessLine(String type) {
			return (isArray(type) || isContainer(type)) ? CONTAINER : SCALAR;
		}
	}
}
