package com.beancrumbs.function;

import com.beancrumbs.common.SourceCodeGenerator;
import com.beancrumbs.processor.BeanProperty;
import com.beancrumbs.utils.ParsingUtils;

enum HandlerRole implements SourceCodeGenerator {
	// 1: object type, 2: canonical property type, 3: object parameter name, 4: getter name, 5: property name, 6: parent, 7: method access modifier, 8: method
	GETTER(
			"	public static final %6$s<%1$s, %2$s> %5$s = new %6$s<%1$s, %2$s>() {%n" + 
			"		@Override%n" + 
			"		%7$s %2$s %8$s(%1$s %3$s) {%n" + 
			"			return %3$s.%4$s();%n" + 
			"		}%n" + 
			"	};%n" 
			) {
		
		public String getCode(String simpleClassName, BeanProperty property, ClassWritingConf conf) {
			String typeName = property.getTypeName();
			return String.format(codeTemplate(), //conf.getGetter().getSourceCodeGenerator().codeTemplate(),
					simpleClassName, 
					ParsingUtils.canoninize(typeName, conf.isImportReferences()), 
					ParsingUtils.firstToLowerCase(simpleClassName),
					property.getGetterName(),
					property.getName(),
					ParsingUtils.canoninize(conf.getGetter().getParentClassName(), conf.isImportReferences()),
					conf.getGetter().getMethodModifier(),
					conf.getGetter().getMethod()
				);
		}
	},
	// 1: object type, 2: property type, 3: object parameter name, 4: getter name, 5: property parameter name, 6: setter name, 7: real property type, 8: parent, 9: method access modifier, 10: method  
	SETTER(
			"	public static final Function<%1$s, %2$s> %5$s(final %7$s %5$s) {%n" + 
			"		return new %8$s<%1$s, %2$s>() {%n" + 
			"			@Override%n" + 
			"			%9$s %2$s %10$s(%1$s %3$s) {%n" + 
			"				%2$s prev = %3$s.%4$s();%n" + 
			"				%3$s.%6$s(%5$s);%n" + 
			"				return prev;%n" + 
			"			}%n" + 
			"		};%n" + 
			"	}%n" 
			) {
		
		public String getCode(String simpleClassName, BeanProperty property, ClassWritingConf conf) {
			String typeName = property.getTypeName();
			return String.format(codeTemplate(), //conf.getSetter().getSourceCodeGenerator().codeTemplate(),
					simpleClassName, 
					ParsingUtils.canoninize(typeName, conf.isImportReferences()), 
					ParsingUtils.firstToLowerCase(simpleClassName), 
					property.getGetterName(),
					property.getName(),
					property.getSetterName(), 
					ParsingUtils.shortClassName(typeName), 
					ParsingUtils.canoninize(conf.getSetter().getParentClassName(), conf.isImportReferences()),
					conf.getSetter().getMethodModifier(),
					conf.getSetter().getMethod()
				);
					
		}
	},
	// 1: object type, 2: object parameter name, 3: getter name, 4: property name, 5: parent, 6: method access modifier, 7: method
	PREDICATE(
			"	public static final %5$s<%1$s> %4$s = new %5$s<%1$s>() {%n" + 
			"		@Override%n" + 
			"		%6$s boolean %7$s(%1$s %2$s) {%n" + 
			"			return %2$s.%3$s();%n" + 
			"		}%n" + 
			"	};%n" 
	) {
		
		public String getCode(String simpleClassName, BeanProperty property, ClassWritingConf conf) {
			return String.format(codeTemplate(), //conf.getPredicate().getSourceCodeGenerator().codeTemplate(), 					
					simpleClassName, 
					ParsingUtils.firstToLowerCase(simpleClassName),
					property.getGetterName(),
					property.getName(),
					ParsingUtils.canoninize(conf.getPredicate().getParentClassName(), conf.isImportReferences()),
					conf.getPredicate().getMethodModifier(),
					conf.getPredicate().getMethod());
		}
	},
	;
	
	private final String codeTemplate;
	
	private HandlerRole(String codeTemplate) {
		this.codeTemplate = codeTemplate;
	}
	
	static HandlerRole byName(String name) {
		return valueOf(name.toUpperCase());
	}
	
	
	String codeTemplate() {
		return codeTemplate;
	}
	
	
	String propertyName() {
		return name().toLowerCase();
	}
}