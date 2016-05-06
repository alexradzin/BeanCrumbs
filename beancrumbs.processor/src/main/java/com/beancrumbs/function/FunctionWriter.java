package com.beancrumbs.function;

import static com.beancrumbs.utils.ParsingUtils.isJavaLang;
import static com.beancrumbs.utils.ParsingUtils.isPrimitive;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;

import com.beancrumbs.common.HandlerConf;
import com.beancrumbs.nullsafe.NullSafeAccess;
import com.beancrumbs.processor.BeanProperty;
import com.beancrumbs.processor.BeansMetadata;
import com.beancrumbs.processor.Crumbed;
import com.beancrumbs.processor.CrumbsWay;
import com.beancrumbs.skeleton.SkeletonWriter;
import com.beancrumbs.utils.ParsingUtils;

public class FunctionWriter implements CrumbsWay {
	private static final Logger logger = Logger.getLogger(SkeletonWriter.class .getName());
	
	static final String IMPORT = "import"; 

	
	@Override
	public boolean strew(String fullClassName, BeansMetadata data, OutputStream out, Properties props) {
		BeanWritingConf conf = new BeanWritingConf(props);
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
	
	
	private void writeImports(String fullClassName, BeansMetadata data, BeanWritingConf conf, PrintWriter pw) {
		if (!conf.isImportReferences()) {
			return;
		}
		Collection<String> imports = new HashSet<>();
		imports.add(Crumbed.class.getName());
		imports.add(NullSafeAccess.class.getName());
		for(BeanProperty property : data.getBeanMetadata(fullClassName).getProperties().values()) {
			String typeName = property.getTypeName();
			if(property.isReadable()) {
				imports.add(isBoolean(typeName) ? conf.getGetter().getParentClassName() : conf.getPredicate().getParentClassName());
			} else {
				imports.add(conf.getGetter().getParentClassName());
			}
			if (isPrimitive(typeName) || isJavaLang(typeName)) {
				continue;
			}
			for (String type : ParsingUtils.componentClassNames(typeName)) {
				if (!ParsingUtils.isJavaLang(typeName)) {
					imports.add(ParsingUtils.pureClassName(type));
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
	
	
	private void writeFunctions(String fullClassName, String simpleName, BeansMetadata data, BeanWritingConf conf, PrintWriter pw) {
		pw.println("@" + (conf.isImportReferences() ? Crumbed.class.getSimpleName() : Crumbed.class.getName()) + "(" + (conf.isImportReferences() ? NullSafeAccess.class.getSimpleName() : NullSafeAccess.class.getName()) + ".class" + ")");
		pw.println("public class " + getClassName(simpleName) + " {");
		Map<String, BeanProperty> properties = data.getBeanMetadata(fullClassName).getProperties();
		for(BeanProperty property : properties.values()) {
			writePropertyFunctions(simpleName, data, property, conf, pw);
		}
		pw.println("}");
		pw.flush();
	}
	
	
	private void writePropertyFunctions(String simpleClassName, BeansMetadata data, BeanProperty property, BeanWritingConf conf, PrintWriter pw) {
		if (property.isReadable()) {
			String typeName = property.getTypeName();
			HandlerConf handlerConf = isBoolean(typeName) ? conf.getPredicate() : conf.getGetter();
			writePropertyFunction(simpleClassName, data, property, conf, pw, handlerConf);
		}
		if (property.isWritable()) {
			writePropertyFunction(simpleClassName, data, property, conf, pw, conf.getSetter());
		}
	}
	
	
	private void writePropertyFunction(String simpleClassName, BeansMetadata data, BeanProperty property, BeanWritingConf conf, PrintWriter pw, HandlerConf handlerConf) {
		pw.println(handlerConf.getSourceCodeGenerator().getCode(simpleClassName, data, property, conf));
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
