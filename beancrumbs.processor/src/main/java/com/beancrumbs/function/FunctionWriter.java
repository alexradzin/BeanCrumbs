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
	
	static final String IMPORT = "import"; 
	
	private static final Map<String, String> primitiveWrappers = new HashMap<>();
	static {
		@SuppressWarnings("rawtypes")
		Class[] primitives = {byte.class, short.class, long.class, float.class, double.class, boolean.class};
		for (@SuppressWarnings("rawtypes") Class primitive : primitives) {
			String name = primitive.getName();
			primitiveWrappers.put(name, ParsingUtils.firstToUpperCase(name));
		}
		primitiveWrappers.put(int.class.getName(), Integer.class.getName());
		primitiveWrappers.put(char.class.getName(), Character.class.getName());
	}
	

	
	@Override
	public boolean strew(String fullClassName, BeansMetadata data, OutputStream out, Properties props) {
		WritingConf conf = new WritingConf(props);
		if (!conf.isValid()) {
			return false;
		}
		
		logger.info("Writing properties functions: " + fullClassName + " for bean " + data);
		PrintWriter pw = new PrintWriter(out);

		Entry<String, String> nameElements = ParsingUtils.splitClassName(fullClassName);
		String packageName = nameElements.getKey();
		String simpleName = nameElements.getValue();

		
		if (packageName != null && !"".equals(packageName)) {
			pw.println("package " + packageName + ";");
			pw.println();
		}
		
		writeImports(fullClassName, data, conf, pw);	
		writeFunctions(fullClassName, simpleName, data, conf, pw);
		pw.flush();
		return true;
	}
	
	
	private void writeImports(String fullClassName, BeansMetadata data, WritingConf conf, PrintWriter pw) {
		if (!conf.isImportReferences()) {
			return;
		}
		Collection<String> imports = new HashSet<>();
		for(BeanProperty property : data.getBeanMetadata(fullClassName).getProperties().values()) {
			String typeName = property.getTypeName();
			if(property.isReadable()) {
				imports.add(isBoolean(typeName) ? conf.getGetter().getParentClassName() : conf.getPredicate().getParentClassName());
			} else {
				imports.add(conf.getGetter().getParentClassName());
			}
			if (primitiveWrappers.containsKey(typeName) || ParsingUtils.isJavaLang(typeName)) {
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
	}
	
	
	private void writeFunctions(String fullClassName, String simpleName, BeansMetadata data, WritingConf conf, PrintWriter pw) {
		pw.println("@" + Crumbed.class.getName() + "(" + PropertyFuction.class.getName() + ".class" + ")");
		pw.println("public class " + getClassName(simpleName) + " {");
		Map<String, BeanProperty> properties = data.getBeanMetadata(fullClassName).getProperties();
		for(BeanProperty property : properties.values()) {
			writePropertyFunctions(simpleName, property, conf, pw);
		}
		pw.println("}");
		pw.flush();
	}
	
	
	private void writePropertyFunctions(String simpleClassName, BeanProperty property, WritingConf conf, PrintWriter pw) {
		if (property.isReadable()) {
			String typeName = property.getTypeName();
			HandlerConf handlerConf = isBoolean(typeName) ? conf.getPredicate() : conf.getGetter();
			writePropertyFunction(simpleClassName, property, conf, pw, handlerConf);
		}
		if (property.isWritable()) {
			writePropertyFunction(simpleClassName, property, conf, pw, conf.getSetter());
		}
	}
	
	
	private void writePropertyFunction(String simpleClassName, BeanProperty property, WritingConf conf, PrintWriter pw, HandlerConf handlerConf) {
		pw.println(handlerConf.getRole().getCode(simpleClassName, property, conf));
		pw.flush();
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
		return originalClassName + "Function";
	}
	
	private boolean isBoolean(String type) {
		return "boolean".equals(type);
	}
}
