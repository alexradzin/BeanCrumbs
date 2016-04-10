package com.beancrumbs.function;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;

import com.beancrumbs.processor.BeanProperty;
import com.beancrumbs.processor.BeansMetadata;
import com.beancrumbs.processor.Crumbed;
import com.beancrumbs.processor.CrumbsWay;
import com.beancrumbs.skeleton.SkeletonWriter;
import com.beancrumbs.utils.ParsingUtils;

public class FunctionWriter implements CrumbsWay {
	private static final Logger logger = Logger.getLogger(SkeletonWriter.class .getName());
	private static final Map<String, String> primitiveWrappers = new HashMap<>();
	static {
		@SuppressWarnings("rawtypes")
		Class[] primitives = {byte.class, short.class, long.class, float.class, double.class, boolean.class};
		for (@SuppressWarnings("rawtypes") Class primitive : primitives) {
			String name = primitive.getName();
			primitiveWrappers.put(name, firstToUpperCase(name));
		}
		primitiveWrappers.put(int.class.getName(), Integer.class.getName());
		primitiveWrappers.put(char.class.getName(), Character.class.getName());
	}
	
	// 1: object type, 2: canonical property type, 3: object parameter name, 4: getter name, 5: property name
	private final static String GETTER_TEMPLATE = 
			"	public static final Function<%1$s, %2$s> %5$s = new Function<%1$s, %2$s>() {%n" + 
			"		@Override%n" + 
			"		public %2$s apply(%1$s %3$s) {%n" + 
			"			return %3$s.%4$s();%n" + 
			"		}%n" + 
			"	};%n"; 

	// 1: object type, 2: object parameter name, 3: getter name, 4: property name
	private final static String PREDICATE_TEMPLATE = 
			"	public static final Predicate<%1$s> %4$s = new Predicate<%1$s>() {%n" + 
			"		@Override%n" + 
			"		public boolean apply(%1$s %2$s) {%n" + 
			"			return %2$s.%3$s();%n" + 
			"		}%n" + 
			"	};%n"; 
	
	
	// 1: object type, 2: property type, 3: object parameter name, 4: getter name, 5: property parameter name, 6: setter name, 7: real property type  
	private final static String SETTER_TEMPLATE = 
			"	public static final Function<%1$s, %2$s> %5$s(final %7$s %5$s) {%n" + 
			"		return new Function<%1$s, %2$s>() {%n" + 
			"			@Override%n" + 
			"			public %2$s apply(%1$s %3$s) {%n" + 
			"				%2$s prev = %3$s.%4$s();%n" + 
			"				%3$s.%6$s(%5$s);%n" + 
			"				return prev;%n" + 
			"			}%n" + 
			"		};%n" + 
			"	}%n"; 
	
	
	
	@Override
	public boolean strew(String fullClassName, BeansMetadata data, OutputStream out, Properties props) {
		logger.info("Writing properties functions: " + fullClassName + " for bean " + data);
		PrintWriter pw = new PrintWriter(out);

		Entry<String, String> nameElements = ParsingUtils.splitClassName(fullClassName);
		String packageName = nameElements.getKey();
		String simpleName = nameElements.getValue();

		
		if (packageName != null && !"".equals(packageName)) {
			pw.println("package " + packageName + ";");
			pw.println();
		}
		
		Collection<String> imports = new HashSet<>();
		
		for(BeanProperty property : data.getBeanMetadata(fullClassName).getProperties().values()) {
			String typeName = property.getTypeName();
			imports.add(isBoolean(typeName) ? "com.google.common.base.Predicate" : "com.google.common.base.Function");
			if (primitiveWrappers.containsKey(typeName) || typeName.startsWith("java.lang.")) {
				continue;
			}
			imports.add(ParsingUtils.pureClassName(typeName));
		}

		for (String imp : imports) {
			pw.println(String.format("import %s;", imp));
		}
		if (!imports.isEmpty()) {
			pw.println();
		}
		
		writeFunctionsImpl(fullClassName, simpleName, data, pw);
		pw.flush();
		return true;
	}
	
	
	private void writeFunctionsImpl(String fullClassName, String simpleName, BeansMetadata data, PrintWriter pw) {
		pw.println("@" + Crumbed.class.getName() + "(" + PropertyFuction.class.getName() + ".class" + ")");
		pw.println("public class " + getClassName(simpleName) + " {");
		Map<String, BeanProperty> properties = data.getBeanMetadata(fullClassName).getProperties();
		for(BeanProperty property : properties.values()) {
			writePropertyFunctions(simpleName, property, pw);
		}
		pw.println("}");
		pw.flush();
	}
	
	
	private void writePropertyFunctions(String simpleClassName, BeanProperty property, PrintWriter pw) {
		if (property.isReadable()) {
			writeGetter(simpleClassName, property, pw);
		}
		if (property.isWritable()) {
			writeSetter(simpleClassName, property, pw);
		}
	}

	
	private void writeGetter(String simpleClassName, BeanProperty property, PrintWriter pw) {
		String typeName = property.getTypeName();
		if (isBoolean(typeName)) {
			writeFunction(
				PREDICATE_TEMPLATE, 
				pw, 
				simpleClassName, 
				firstToLowerCase(simpleClassName),
				property.getGetterName(),
				property.getName()
			);
		} else {
			writeFunction(
				GETTER_TEMPLATE, 
				pw, 
				simpleClassName, 
				canoninize(typeName), 
				firstToLowerCase(simpleClassName),
				property.getGetterName(),
				property.getName()
			);
		}
	}

	
	private void writeSetter(String simpleClassName, BeanProperty property, PrintWriter pw) {
		String typeName = property.getTypeName();
		writeFunction(
			SETTER_TEMPLATE, 
			pw, 
			simpleClassName, 
			canoninize(typeName), 
			firstToLowerCase(simpleClassName), 
			property.getGetterName(),
			property.getName(),
			property.getSetterName(), 
			shortClassName(typeName) 
		);
	}

	private void writeFunction(String template, PrintWriter pw, Object ... args) {
		pw.println(String.format(template, args));
	}
	
	private static String canoninize(String type) {
		String wrapper = primitiveWrappers.get(type);
		String canonicalType =  wrapper == null ? type : wrapper;
		return ParsingUtils.splitClassName(canonicalType).getValue();
	}

	
	private static String shortClassName(String type) {
		return primitiveWrappers.containsKey(type) ? type : ParsingUtils.splitClassName(type).getValue();
	}
	
	
	private static String firstToLowerCase(String str) {
		if (str == null || str.length() == 0) {
			return str;
		}
		char first = Character.toLowerCase(str.charAt(0));
		return str.length() > 1 ? first + str.substring(1) : "" + first;
	}

	private static String firstToUpperCase(String str) {
		if (str == null || str.length() == 0) {
			return str;
		}
		char first = Character.toUpperCase(str.charAt(0));
		return str.length() > 1 ? first + str.substring(1) : "" + first;
	}
	
	
	@Override
	public Collection<String> getMarkers() {
		return Collections.<String>singleton(PropertyFuction.class.getName());
	}

	@Override
	public String getName() {
		return "function";
	}

	@Override
	public String getClassName(String originalClassName) {
		return originalClassName + "Functions";
	}
	
	private boolean isBoolean(String type) {
		return "boolean".equals(type);
	}
}
