package com.beancrumbs.nullsafe;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.beancrumbs.processor.BeanMetadata;
import com.beancrumbs.processor.BeanProperty;
import com.beancrumbs.processor.BeansMetadata;
import com.beancrumbs.processor.Crumbed;
import com.beancrumbs.processor.CrumbsWay;
import com.beancrumbs.skeleton.SkeletonWriter;
import com.beancrumbs.utils.ParsingUtils;

public class NullSafeAccessorWriter implements CrumbsWay {
	private final static Logger logger = Logger.getLogger(SkeletonWriter.class .getName()); 
	private final static String CLASS_NAME_SUFFIX = "NullSafeAccessor";

	@Override
	public void strew(String fullClassName, BeansMetadata data, OutputStream out) {
		logger.info("Writing skeleton: " + fullClassName + " for bean " + data);
		PrintWriter pw = new PrintWriter(out);
		
		Entry<String, String> nameElements = ParsingUtils.splitClassName(fullClassName);
		String packageName = nameElements.getKey();
		String simpleName = nameElements.getValue();
		
		if (packageName != null && !"".equals(packageName)) {
			pw.println("package " + packageName + ";");
			pw.println();
		}
		

		writeAccessorImpl(fullClassName, simpleName, data, pw);
	}
	
	private void writeAccessorImpl(String name, String simpleName, BeansMetadata data, PrintWriter pw) {
		logger.info("Writing accessor: " + name + ", " + simpleName);
		pw.println("@" + Crumbed.class.getName() + "(" + NullSafeAccess.class.getName() + ".class" + ")");
		final String accessorClassName = getAccessorClassName(simpleName);
		pw.println("public class " + accessorClassName + " extends " + simpleName + " {");
		pw.println("	private final " + simpleName + " instance;");
		pw.println("	public " + accessorClassName + "(" + simpleName + " instance" + ")" + " {");
		pw.println("		this.instance = instance;");
		pw.println("	}");
		BeanMetadata context = data.getBeanMetadata(name);
		writeProperties(context.getProperties(), data, pw);
		pw.println("}");
		pw.flush();
	}
	
	private void writeProperties(Map<String, BeanProperty> properties, BeansMetadata data, PrintWriter pw) {
		for(Map.Entry<String, BeanProperty> entry : properties.entrySet()) {
			if (!entry.getValue().isReadable()) {
				continue;
			}
			String type = entry.getValue().getTypeName();
			
			String fieldName = entry.getKey();
			logger.info("Writing null-safe accessible property: " + type + " " + fieldName);
			
			
			String getterName = 
					(boolean.class.getSimpleName().equals(type) ? "is" : "get") + 
					fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
			
			
			String directAccessExpression = "instance == null ? null : instance." + getterName + "()";
			String accessExpression = directAccessExpression;
			if (data.getBeanMetadata(type) != null) {
				String typeAccessor = getAccessorClassName(type);
				//accessExpression = directAccessExpression + " == null ? new " + typeAccessor + "() : " + directAccessExpression;
				accessExpression = "new " + typeAccessor + "(" + directAccessExpression + ")";
			}

			pw.println("	@" + Override.class.getSimpleName());
			pw.println("	public " + type + " " + getterName + "() {");
			pw.println("		return " + accessExpression + ";");
			pw.println("	}");
		}
	}
	

	@Override
	public Collection<String> getMarkers() {
		return Collections.<String>singleton(NullSafeAccess.class.getName());
	}

	@Override
	public String getName() {
		return "nullsafe";
	}

	@Override
	public String getClassName(String originalClassName) {
		return originalClassName + CLASS_NAME_SUFFIX;
	}


	static String getAccessorClassName(String beanClassName) {
		return beanClassName + CLASS_NAME_SUFFIX;
	}
}
