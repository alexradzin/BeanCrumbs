package com.beancrumbs.skeleton;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.beancrumbs.processor.BeanMetadata;
import com.beancrumbs.processor.BeanProperty;
import com.beancrumbs.processor.BeansMetadata;
import com.beancrumbs.processor.CrumbsWay;

public class SkeletonWriter implements CrumbsWay {
	private final static String ground = "$";
	private final static Set<String> javaKeywords = new HashSet<String>(Arrays.asList(
			new String[] {
					"abstract",
					"assert",
					"boolean",
					"break",
					"byte",
					"case",
					"catch",
					"char",
					"class",
					"const",
					"continue",
					"default",
					"do",
					"double",
					"else",
					"enum",
					"extends",
					"final",
					"finally",
					"float",
					"for",
					"goto",
					"if",
					"implements",
					"import",
					"instanceof",
					"int",
					"interface",
					"long",
					"native",
					"new",
					"package",
					"private",
					"protected",
					"public",
					"return",
					"short",
					"static",
					"strictfp", 
					"super",
					"switch",
					"synchronized",
					"this",
					"throw",
					"throws",
					"transient",
					"try",
					"void",
					"volatile",
					"while",
					"false",
					"null",
					"true",				
			}));

	@Override
	public void strew(String name, BeansMetadata data, OutputStream out) {
		System.out.println("strew: " + data);
		PrintWriter pw = new PrintWriter(out);
		

		String packageName = "";
		String simpleName = name;
		int lastDot = name.lastIndexOf('.');
		if (lastDot >= 0) {
			packageName = name.substring(0, lastDot);
			simpleName = name.substring(lastDot + 1); 
		}
		
		
		if (packageName != null && !"".equals(packageName)) {
			pw.println("package " + packageName + ";");
			pw.println();
		}
		

		writeSkeletonImpl(name, simpleName, 0, data, pw);
	}

	
	private void writeSkeletonImpl(String name, String simpleName, int nesting, BeansMetadata data, PrintWriter pw) {
		String tabs = tab(nesting);
		String propTabs = tab(nesting + 1);
		
		String suffix = "Skeleton";
		String modifier = "";
		if (nesting > 0) {
			suffix = "";
			modifier = "static ";
		}
		
		pw.println(tabs + "public " + modifier + "class " + simpleName + suffix + " {");
		
		if (nesting > 0) {
			pw.println(propTabs + "public final static String " + ground + " = " + "\"" + simpleName + "\"" + ";");
		}

		
		BeanMetadata context = data.getBeanMetadata(name);
		
		writeProperties(context.getProperties(), nesting, data, pw);
		
		
		for (BeanMetadata superContext = data.getBeanMetadata(context.getSuperClassName()); 
				superContext != null; 
				superContext = data.getBeanMetadata(superContext.getSuperClassName())) {
			
			pw.println(propTabs + "// Inherited from " + superContext.getFullName());
			writeProperties(superContext.getProperties(), nesting, data, pw);
		}
		
		pw.println(tabs + "}");
		pw.flush();
	}
	
	
	private void writeProperties(Map<String, BeanProperty> properties, int nesting, BeansMetadata data, PrintWriter pw) {
		String propTabs = tab(nesting + 1);
		
		for(Map.Entry<String, BeanProperty> entry : properties.entrySet()) {
			String type = entry.getValue().getTypeName();
			String fieldName = entry.getKey();
			
			if (data.getBeanMetadata(type) != null) {
				writeSkeletonImpl(type, fieldName, nesting + 1, data, pw);
			} else {
				String fmtFieldName = fixPropertyName(fieldName);
				String prefix = propTabs + "public final static String " + fmtFieldName + " = ";
				if (nesting > 0) {
					pw.println(prefix + ground + " + " + "\"." + fieldName + "\"" + ";");
				} else {
					pw.println(prefix + "\"" + fieldName + "\"" + ";");
				}
			}
		}
	}
	
	
	private String tab(int n) {
		char[] tabs = new char[n];
		Arrays.fill(tabs, '\t');
		return new String(tabs);
	}
	
	// This method "fixes" possible conflict: bean property name extracted from getter may be
	// as java keyword. For example getClass() method produces property named class. 
	// But we create field with the same name that is illegal. 
	// For now to fix this problem we just add trailing underscore character, i.e. create 
	// skeleton field named class_. It is ugly fast solution. 
	// Probably this method should be configurable as strategy. For example we can capitalize all
	// skeleton fields (according to java naming convention).
	private String fixPropertyName(String propertyName) {
		return javaKeywords.contains(propertyName) ? propertyName + "_" : propertyName;
	}
	
	
	
	@Override
	public Collection<Class<? extends Annotation>> getMarkers() {
		return Collections.<Class<? extends Annotation>>singleton(Skeleton.class);
	}

	@Override
	public String getName() {
		return "skeleton";
	}

	@Override
	public String getClassName(String originalClassName) {
		return originalClassName + "Skeleton";
	}
}