package com.beancrumbs.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;



@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedOptions({Options.BEANCRUMBS_DIR_OPTION, Options.BEANCRUMBS_LOG_OPTION, Options.BEANCRUMBS_LOG_LEVEL_OPTION, Options.BEANCRUMBS_ENABLED_OPTION})
public class BeanCrumber extends AbstractProcessor {
	private final static Logger logger = Logger.getLogger(BeanCrumber.class .getName()); 
	private final static String CLASS_ANNOTATION_PROP = "class.annontation";
	private final static String GENERATED_SRC_DIR_PROP = "generated.src.dir";
	private final static String GENERATED_SRC_PROJECT_PROP = "generated.src.project";
	
	private static enum Config {
		PROPERTIES,
		INDEX,
		;
		
		
		String getFilePath(CrumbsWay way) {
			return "META-INF/beancrumbs/" + way.getName() + "." + extension();
		}
		
		private String extension() {
			return name().toLowerCase();
		}
	}
	
	
	private boolean enabled = true;
	private BeansMetadata metadata = new BeansMetadata();
	private Iterable<CrumbsWay> ways = null;
	private ClassLoader projectClassLoader;
	
	public BeanCrumber() {
		final String logFileConfigPropertyName = "java.util.logging.config.file";
		String logFileConfigPropertyValue = System.getProperty(logFileConfigPropertyName);
		
		if (logFileConfigPropertyValue == null) {
			File cwd = new File(".");
			File logProps = new File(cwd, "logging.properties");
			if (logProps.exists()) {
				logger.info("Log is configured using " + logProps.getPath());
				System.setProperty(logFileConfigPropertyName, logProps.getPath());
			}
		}
		
	}
	
    @Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
    	super.init(processingEnv);
		try {
			projectClassLoader = getProjectClassLoader();
		} catch (IOException ex) {
			ex.printStackTrace();
			processingEnv.getMessager().printMessage(Kind.ERROR,
					ex.getMessage());
		}
    	
    }
	
	

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		if (!enabled) {
			return false;
		}

		Map<CrumbsWay, Properties> config = new HashMap<>();
		
		logger.fine("Starting processing");
		for (CrumbsWay way : getWays()) {
			logger.fine("Go on way " + way);
			
			Properties props = null;
			try {
				props = getConfigurationProperties(getConfiguration(way, Config.PROPERTIES));
				config.put(way, props);
			} catch (Exception ex) {
				ex.printStackTrace();
				processingEnv.getMessager().printMessage(Kind.ERROR,
						ex.getMessage());
				continue;
			}
			
			
			Collection<Class<? extends Annotation>> markers = getMarkers(props, way);
			if (markers != null) {
				for (Class<? extends Annotation> marker : markers) {
					logger.fine("Handling Marker annotation " + marker);
					createProcessor(roundEnv, marker).handleTypes(way);
				}
			}
		}
				
		if(roundEnv.processingOver()) {
			try {
				for (CrumbsWay way : getWays()) {
					Collection<String> classNames = getIndex(getConfiguration(way, Config.INDEX));
					createReflectionParser(classNames).handleTypes(way);
					createReflectionParser(metadata.getReferencedClassNames()).handleTypes(way);
					createReflectionParser(getSuperClassNames()).handleTypes(way);
					sprinkleBeanCrumbs(way, config.get(way));
				}
			} catch (IOException ex) {
				ex.printStackTrace();
				processingEnv.getMessager().printMessage(Kind.ERROR,
						ex.getMessage());
			}
		}
		
		
		return false;
	}

	
	private Collection<Class<? extends Annotation>> getMarkers(Properties props, CrumbsWay way) {
		try {
			if (props != null) {
				String classAnnotationProp = props.getProperty(CLASS_ANNOTATION_PROP);
				if (classAnnotationProp != null) {
					Collection<Class<? extends Annotation>> annotationClasses = new ArrayList<Class<? extends Annotation>>();  
					for (String annotationClassName : classAnnotationProp.split("\\s*[,;]\\s*")) {
						@SuppressWarnings("unchecked")
						Class<? extends Annotation> clazz = (Class<? extends Annotation>)projectClassLoader.loadClass(annotationClassName);
						annotationClasses.add(clazz);
					}
					return annotationClasses;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			processingEnv.getMessager().printMessage(Kind.ERROR,
					ex.getMessage());
		}
		
		
		return way.getMarkers();
		
	}
	
	
	// create anonymous inner class that extends BeanProcessor and implements method getElements();
	private BeanProcessor createProcessor(final RoundEnvironment roundEnv, final Class<? extends Annotation> marker) {
		BeanProcessor processor = new BeanProcessor() {
			@Override
			protected Set<? extends Element> getElements() {
				return roundEnv.getElementsAnnotatedWith(marker);
			}
		};
		processor.setMetadata(metadata);
		return processor;
	}
	
	// create anonymous inner class that extends ReflectionParser and implements method getElements();
	private ReflectionParser createReflectionParser(final Collection<String> referencedClassNames) {
		ReflectionParser parser = new ReflectionParser() {
			@Override
			protected Set<Class<?>> getElements() {
				if (referencedClassNames == null) {
					return Collections.emptySet();
				}
				
				Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
				for (String className : referencedClassNames) {
					Class<?> clazz;
					try {
						clazz = getPrimitiveClassByName(className);
						if (clazz == null) {
							clazz = projectClassLoader.loadClass(className);
						}
						classes.add(clazz);
					} catch (ClassNotFoundException e) {
						processingEnv.getMessager().printMessage(Kind.WARNING, e.getMessage());
					}
				}
				return classes;
			}
		};
		
		parser.setMetadata(metadata);
		
		return parser;
	}
	
	//TODO: get full project's classpath
	private ClassLoader getProjectClassLoader() throws IOException {
		FileObject d = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "com", "dummy");
		URL url = new File(d.toUri()).getParentFile().getParentFile().toURI().toURL();
		logger.fine("classpath URL: " + url);
		return new URLClassLoader(new URL[] {url}, getClass().getClassLoader());
	}
	
	private InputStream getConfiguration(CrumbsWay way, Config config) {
		logger.fine("conf path=" + config.getFilePath(way) + " " + this.getClass().getResourceAsStream(config.getFilePath(way)));
		return projectClassLoader.getResourceAsStream(config.getFilePath(way));
	}
	
	private Properties getConfigurationProperties(InputStream in) throws IOException {
		if (in == null) {
			return null;
		}
		
		Properties props = new Properties();
		props.load(in);
		
		return props;
	}

	private Collection<String> getIndex(InputStream in) throws IOException {
		if (in == null) {
			logger.fine("Index is null");
			return Collections.emptyList();
		}
		Collection<String> classNames = new ArrayList<String>();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		for (String line = reader.readLine();  line != null;  line = reader.readLine()) {
			String className = line.trim();
			classNames.add(className);
		}

		logger.fine("Index: " + classNames);
		
		return classNames;
	}
	
	
	private Collection<String> getSuperClassNames() {
		ClassLoader classLoader;
		try {
			classLoader = getProjectClassLoader();
		} catch (IOException e) {
			e.printStackTrace();
			processingEnv.getMessager().printMessage(Kind.ERROR, e.getMessage());
			return Collections.emptyList();
		}
		
		Collection<String> classNames = new LinkedHashSet<String>();
		
		for(String name : metadata.getBeanNames()) {
			BeanMetadata m = metadata.getBeanMetadata(name);
			String className = m.getSuperClassName();
			if (metadata.getBeanMetadata(className) != null) {
				continue; // class metadata already exists
			}
			
			
			try {
				Class<?> clazz = classLoader.loadClass(className);
				for (Class<?> c : getHierarchyCrumbs(clazz)) {
					String superClassName = c.getName();
					if (metadata.getBeanMetadata(superClassName) == null && metadata.shouldBeDiscovered(superClassName)) {
						classNames.add(superClassName);
					}
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				processingEnv.getMessager().printMessage(Kind.WARNING, e.getMessage());
			}
		}
		
		return classNames;
	}
	
	
	private Iterable<Class<?>> getHierarchyCrumbs(Class<?> clazz) {
		List<Class<?>> superClasses = new ArrayList<Class<?>>();
		
		for (Class<?> c = clazz;  c != null;  c = c.getSuperclass()) {
			superClasses.add(c);
		}
		return superClasses;
	}

	/**
	 * Call class that generates code using bean meta data. 
	 */
	private void sprinkleBeanCrumbs(CrumbsWay way, Properties props) throws IOException {
		logger.fine("sprinkleBeanCrumbs way: " + way + ", for beans: " + metadata.getBeanNames());

		File generatedSrcDir = null;
		File generatedSrcProjectRoot = new File(".").getCanonicalFile();
		
		
		if (props != null) {
			String generatedSrcProjectProp = props.getProperty(GENERATED_SRC_PROJECT_PROP);
			if (generatedSrcProjectProp != null) {
				generatedSrcProjectRoot = new File(generatedSrcProjectProp);
			}
			
			String generatedSrcProp = props.getProperty(GENERATED_SRC_DIR_PROP);
			if (generatedSrcProp != null) {
				generatedSrcDir = new File(generatedSrcProjectRoot, generatedSrcProp);
			} else if (generatedSrcProjectProp != null) {
				throw new IllegalArgumentException("Source directory for generated files in extenal project must be defined explicitly. Add property " + GENERATED_SRC_DIR_PROP);
			}
		}
		
		
		for (String name : metadata.getBeanNames(way)) {
			String packageName = "";
			String simpleName = name;
			int lastDot = name.lastIndexOf('.');
			if (lastDot >= 0) {
				packageName = name.substring(0, lastDot);
				simpleName = name.substring(lastDot + 1); 
			}

			logger.fine("Write crumbs for class " + name + " package: " + packageName + ", simple name=" + simpleName + ": " + way.getClassName(simpleName) );
			
			FileObject output = processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, packageName, way.getClassName(simpleName) + ".java");
			File generatedSrcFile = new File(output.toUri());
			
			if (generatedSrcDir == null) {
				generatedSrcDir = findGeneratedSrcDir(generatedSrcProjectRoot, packageName, simpleName);
			}
			
			File srcFile = new File(new File(generatedSrcDir, packageName.replace('.', '/')), generatedSrcFile.getName());
			srcFile.getParentFile().mkdirs();
			OutputStream out = new FileOutputStream(srcFile);
			way.strew(name, metadata, out);
			out.flush();
			out.close();
		}
	}

	
	private File findGeneratedSrcDir(File root, String packageName, String simpleName) {
		return findGeneratedSrcDir(root, packageNameToPath(packageName) + "/" + simpleName + ".java");
	}
	
	private String packageNameToPath(String packageName) {
		return packageName.replace('.', '/');
	}

	private File findGeneratedSrcDir(File root, String path) {
		if (!root.exists()) {
			throw new IllegalArgumentException(root.getAbsolutePath() + " does not exist");
		}
		if (!root.isDirectory()) {
			throw new IllegalArgumentException(root.getAbsolutePath() + " is not a directory");
		}		
		
		if (new File(root, path).exists()) {
			return root;
		}
		for (File f : root.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		})) {
			File res = findGeneratedSrcDir(f, path);
			if (res != null) {
				return res;
			}
		}
		return null;
	}
	
	
	// discovers required crumb ways (from data files and annotations) and returns the list.
	private Iterable<CrumbsWay> getWays() {
		if (ways == null) {
			ways = ServiceLoader.load(CrumbsWay.class, projectClassLoader);
		}
		logger.fine("ways: " + ways);
		return ways;
	}
}
