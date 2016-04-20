package com.beancrumbs.skeleton;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import com.beancrumbs.processor.BeanCrumber;
import com.beancrumbs.processor.BeanMetadata;
import com.beancrumbs.processor.BeanProperty;
import com.beancrumbs.processor.BeansMetadata;
import com.beancrumbs.processor.Crumbed;
import com.beancrumbs.processor.CrumbsWay;
import com.beancrumbs.utils.ParsingUtils;

/**
 * Implementation of {@link CrumbsWay} that creates skeletons.
 * @see Skeleton 
 * @author alexr
 */
public class SkeletonWriter implements CrumbsWay {
	private static final Logger logger = Logger.getLogger(SkeletonWriter.class .getName()); 
	
	private static final String ground = "$";
	private static final Set<String> javaKeywords = new HashSet<String>(Arrays.asList(
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
	public boolean strew(String name, BeansMetadata data, OutputStream out, Properties props) {
		logger.info("Writing skeleton: " + name + " for bean " + data);
		PrintWriter pw = new PrintWriter(out);

		Entry<String, String> nameElements = ParsingUtils.splitClassName(name);
		String packageName = nameElements.getKey();
		String simpleName = nameElements.getValue();


		
		if (packageName != null && !"".equals(packageName)) {
			pw.println("package " + packageName + ";");
			pw.println();
		}
		
		int maxNesting = Integer.parseInt(props.getProperty(BeanCrumber.MAX_NESTING, "16"));
		writeSkeletonImpl(name, simpleName, 0, maxNesting, data, pw);
		return true;
	}

	
	private void writeSkeletonImpl(String name, String simpleName, int nesting, int maxNesting, BeansMetadata data, PrintWriter pw) {
		if (nesting > maxNesting) {
			logger.info("Writing skeleton: " + name + ", " + simpleName + " if finished because nesting " + nesting + " > " + maxNesting);
			return;
		}
		logger.info("Writing skeleton: " + name + ", " + simpleName);
		String tabs = tab(nesting);
		String propTabs = tab(nesting + 1);
		
		String suffix = "Skeleton";
		String modifier = "";
		if (nesting > 0) {
			suffix = "";
			modifier = "static ";
		}
		if (nesting == 0) {
			pw.println("@" + Crumbed.class.getName() + "(" + Skeleton.class.getName() + ".class" + ")");
		}
		pw.println(tabs + "public " + modifier + "class " + simpleName + suffix + " {");
		
		if (nesting > 0) {
			pw.println(propTabs + "public static final String " + ground + " = " + "\"" + simpleName + "\"" + ";");
		}

		
		BeanMetadata context = data.getBeanMetadata(name);
		
		writeProperties(simpleName, context.getProperties(), nesting, maxNesting, data, pw);
		
		
		for (BeanMetadata superContext = data.getBeanMetadata(context.getSuperClassName()); 
				superContext != null; 
				superContext = data.getBeanMetadata(superContext.getSuperClassName())) {
			
			pw.println(propTabs + "// Inherited from " + superContext.getFullName());
			writeProperties(simpleName, superContext.getProperties(), nesting, maxNesting, data, pw);
		}
		
		pw.println(tabs + "}");
		pw.flush();
	}
	
	
	private void writeProperties(String simpleName, Map<String, BeanProperty> properties, int nesting, int maxNesting, BeansMetadata data, PrintWriter pw) {
		String propTabs = tab(nesting + 1);
		
		for(Map.Entry<String, BeanProperty> entry : properties.entrySet()) {
			String type = entry.getValue().getTypeName();
			String fieldName = entry.getKey();
			logger.info("Writing skeleton property: " + type + " " + fieldName);
			
			if (data.getBeanMetadata(type) != null) {
				if (simpleName.equals(fieldName)) {
					logger.info("Ignoring skeleton property " + fieldName + " because it equals to referencing property (e.g. enclosing class)");
					continue;
				}
				writeSkeletonImpl(type, fieldName, nesting + 1, maxNesting, data, pw);
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
	public Collection<String> getMarkers() {
		return Collections.<String>singleton(Skeleton.class.getName());
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
