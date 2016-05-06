package com.beancrumbs.nullsafe;

import static com.beancrumbs.utils.ParsingUtils.componentClassNames;
import static com.beancrumbs.utils.ParsingUtils.isJavaLang;
import static com.beancrumbs.utils.ParsingUtils.pureClassName;
import static com.beancrumbs.utils.ParsingUtils.simpleClassName;

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

import com.beancrumbs.processor.BeanMetadata;
import com.beancrumbs.processor.BeanProperty;
import com.beancrumbs.processor.BeansMetadata;
import com.beancrumbs.processor.Crumbed;
import com.beancrumbs.processor.CrumbsWay;
import com.beancrumbs.skeleton.SkeletonWriter;
import com.beancrumbs.utils.ParsingUtils;

public class NullSafeAccessorWriter implements CrumbsWay {
	static final String IMPORT = "import";
	private static final Logger logger = Logger.getLogger(SkeletonWriter.class.getName());
	private static final String CLASS_NAME_SUFFIX = "NullSafeAccessor";
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
	}

	@Override
	public boolean strew(String fullClassName, BeansMetadata data, OutputStream out, Properties props) {
		NullSafeConf conf = new NullSafeConf(props);
		logger.info("Writing skeleton: " + fullClassName + " for bean " + data);
		PrintWriter pw = new PrintWriter(out);

		Entry<String, String> nameElements = ParsingUtils.splitClassName(fullClassName);
		String packageName = nameElements.getKey();
		String simpleName = nameElements.getValue();

		if (packageName != null && !"".equals(packageName)) {
			pw.println("package " + packageName + ";");
			pw.println();
		}

		writeImports(fullClassName, data, conf, pw);	
		writeAccessorImpl(fullClassName, simpleName, data, conf, pw);
		return true;
	}

	
	private void writeImports(String fullClassName, BeansMetadata data, NullSafeConf conf, PrintWriter pw) {
		if (!conf.isImportReferences()) {
			return;
		}
		
		Collection<String> imports = new HashSet<>();
		imports.add(Crumbed.class.getName());
		imports.add(NullSafeAccess.class.getName());
		
		for(BeanProperty property : data.getBeanMetadata(fullClassName).getProperties().values()) {
			String typeName = property.getTypeName();
			if(!property.isReadable()) {
				continue;
			}
			if (ParsingUtils.isPrimitive(typeName) || ParsingUtils.isJavaLang(typeName)) {
				continue;
			}
			
			for (String type : componentClassNames(typeName)) {
				if (!isJavaLang(typeName)) {
					imports.add(pureClassName(type));
				}
			}
		}
		for (String imp : imports) {
			pw.println(String.format("import %s;", imp));
		}
		if (!imports.isEmpty()) {
			pw.println();
		}
	}
	
	
	
	private void writeAccessorImpl(String name, String simpleName, BeansMetadata data, NullSafeConf conf, PrintWriter pw) {
		logger.info("Writing accessor: " + name + ", " + simpleName);
		
		pw.println("@" + (conf.isImportReferences() ? Crumbed.class.getSimpleName() : Crumbed.class.getName()) + "(" + (conf.isImportReferences() ? NullSafeAccess.class.getSimpleName() : NullSafeAccess.class.getName()) + ".class" + ")");
		final String accessorClassName = getAccessorClassName(simpleName);
		pw.println("public class " + accessorClassName + " extends " + simpleName + " {");
		pw.println("	private final " + simpleName + " instance;");
		pw.println("	public " + accessorClassName + "(" + simpleName + " instance" + ")" + " {");
		pw.println("		this.instance = instance;");
		pw.println("	}");
		BeanMetadata context = data.getBeanMetadata(name);
		writeProperties(context.getProperties(), data, conf, pw);
		pw.println("}");
		pw.flush();
	}

	private void writeProperties(Map<String, BeanProperty> properties, BeansMetadata data, NullSafeConf conf, PrintWriter pw) {
		for (Map.Entry<String, BeanProperty> entry : properties.entrySet()) {
			if (!entry.getValue().isReadable()) {
				continue;
			}
			String type = entry.getValue().getTypeName();

			String fieldName = entry.getKey();
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

			String returnType = conf.isImportReferences() ? simpleClassName(type) : type;
			pw.println("	@" + Override.class.getSimpleName());
			pw.println("	public " + returnType + " " + getterName + "() {");
			pw.println("		return " + accessExpression + ";");
			pw.println("	}");
		}
	}

	@Override
	public Collection<String> getMarkers() {
		return Collections.<String> singleton(NullSafeAccess.class.getName());
	}

	@Override
	public String getName() {
		return "nullsafe";
	}

	@Override
	public String getClassName(String originalClassName) {
		return originalClassName + CLASS_NAME_SUFFIX;
	}

	private String getDefaultValue(String typeName) {
		return defaultValuesPerType.get(typeName);
	}

	static String getAccessorClassName(String beanClassName) {
		return beanClassName + CLASS_NAME_SUFFIX;
	}
}
