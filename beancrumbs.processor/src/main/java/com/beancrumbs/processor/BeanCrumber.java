package com.beancrumbs.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URISyntaxException;
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
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.beancrumbs.utils.ParsingUtils;



@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedOptions({Options.BEANCRUMBS_DIR_OPTION, Options.BEANCRUMBS_LOG_OPTION, Options.BEANCRUMBS_LOG_LEVEL_OPTION, Options.BEANCRUMBS_ENABLED_OPTION})
public class BeanCrumber extends AbstractProcessor {
	private final static String CLASS_ANNOTATION_PROP = "class.annotation";
	private final static String CLASS_REGEX_PROP = "class.regex";
	private final static String CLASS_WILDCARD_PROP = "class.wildcard";
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
	
	
	static {
		final String logFileConfigPropertyName = "java.util.logging.config.file";
		String logFileConfigPropertyValue = System.getProperty(logFileConfigPropertyName);
		
		if (logFileConfigPropertyValue == null) {
			//TODO: replace CWD by the project directory. 
			File cwd = new File(".");
			File logProps = new File(cwd, "logging.properties");
			//log("logProps=" + logProps + ", " + logProps.exists());
			if (logProps.exists()) {
				try {
					LogManager.getLogManager().readConfiguration(new FileInputStream(logProps));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	private final static Logger logger = Logger.getLogger(BeanCrumber.class .getName()); 
	
	public BeanCrumber() {
		logger.info("BeanCrumber is created cwd=" + new File(".").getAbsolutePath());
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
		
		try {
			logger.fine("Starting processing");
			for (CrumbsWay way : getWays()) {
				logger.fine("Go on way " + way);
				
				Properties props = null;
				try {
					props = getConfigurationProperties(getConfiguration(way, Config.PROPERTIES));
					config.put(way, props);
					logger.fine("props: " + props);
				} catch (IOException | RuntimeException ex) {
					ex.printStackTrace();
					processingEnv.getMessager().printMessage(Kind.ERROR,
							ex.getMessage());
					continue;
				}
				
				
				Collection<String> markers = getMarkers(props, way);
				for (String marker : markers) {
					logger.fine("Handling Marker annotation " + marker);
					createProcessor(roundEnv, marker).handleTypes(way);
				}
				
				Collection<Pattern> patterns = getPatterns(props, way);
				for (Pattern pattern : patterns) {
					logger.fine("Handling classes that match pattern " + pattern);
					createProcessor(roundEnv, pattern).handleTypes(way);
				}
				
				
				Collection<String> classNames = getIndex(getConfiguration(way, Config.INDEX));
				createReflectionParser(classNames).handleTypes(way);
			}
			
			for (CrumbsWay way : getWays()) {
				createReflectionParser(metadata.getReferencedClassNames()).handleTypes(way);
				createReflectionParser(getSuperClassNames()).handleTypes(way);
				sprinkleBeanCrumbs(way, config.get(way));
			}
		} catch (IOException | RuntimeException ex) {
			ex.printStackTrace();
			logger.severe(ex.toString());
			processingEnv.getMessager().printMessage(Kind.ERROR, ex.getMessage());
		}
		
		
		return false;
	}

	

	// TODO: getMarkers should return a collection of annotation names.
	private Collection<String> getMarkers(Properties props, CrumbsWay way) {
		try {
			logger.finest("getMarkers " + props);
			if (props != null) {
				logger.finest("getMarkers props!=null");
				String classAnnotationProp = props.getProperty(CLASS_ANNOTATION_PROP);
				logger.finest("getMarkers classAnnotationProp=" + classAnnotationProp);
				if (classAnnotationProp != null) {
					Collection<String> annotationClasses = new LinkedHashSet<String>();  
					for (String annotationClassName : classAnnotationProp.split("\\s*[,;]\\s*")) {
						logger.finest("getMarkers annotationClassName=" + annotationClassName);
						annotationClasses.add(annotationClassName);
					}
					return annotationClasses;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.severe(ex.toString());
			processingEnv.getMessager().printMessage(Kind.ERROR,
					ex.getMessage());
		}
		
		logger.finest("getMarkers returning default markers of " + way);
		
		Collection<String> annotationClasses = new LinkedHashSet<String>();
		for (String c : way.getMarkers()) {
			annotationClasses.add(c);
		}
		
		return annotationClasses;
		
	}
	
	private Collection<Pattern> getPatterns(Properties props, CrumbsWay way) {
		String classRegexProp = props.getProperty(CLASS_REGEX_PROP);
		String classWildcardProp = props.getProperty(CLASS_WILDCARD_PROP);

		Collection<Pattern> patterns = new ArrayList<>();
		
		
		
		if (classWildcardProp != null) {
			if (classRegexProp != null) {
				throw new IllegalArgumentException(CLASS_REGEX_PROP  + " cannot be used in conjnction with " + CLASS_WILDCARD_PROP);
			}
		
			String[] wildcards = classWildcardProp.split("\\s[,;]\\s*");
			for (String wildcard : wildcards) {
				patterns.add(ParsingUtils.wildcardToPattern(wildcard));
			}
		}

		if (classRegexProp != null) {
			String[] regexs = classRegexProp.split("\\s[,;]\\s*");
			for (String regex : regexs) {
				patterns.add(Pattern.compile(regex));
			}
		}
		
		return patterns;
	}
	
	
	
	
	private BeanProcessor createProcessor(final RoundEnvironment roundEnv, final String marker) {
		BeanProcessor processor = new BeanProcessor() {
			@Override
			protected Set<? extends Element> getElements() {
				Set<Element> elements = new LinkedHashSet<>();
				for (Element element : roundEnv.getRootElements()) {
					logger.finest("Marker based processor: " + element);
					if (!(element instanceof TypeElement)) {
						continue;
					}
					for (AnnotationMirror annotation : element.getAnnotationMirrors()) {

						Name qualifiedName = ((TypeElement) (annotation.getAnnotationType()).asElement()).getQualifiedName();

						if (qualifiedName.contentEquals(marker)) {
							elements.add(element);
						}
					}
				}

				return elements;
			}
		};
		processor.setMetadata(metadata);
		return processor;
	}
	
	
	private BeanProcessor createProcessor(final RoundEnvironment roundEnv, final Pattern pattern) {
		BeanProcessor processor = new BeanProcessor() {
			@Override
			protected Set<? extends Element> getElements() {
				Set<Element> elements = new LinkedHashSet<>();
				for (Element element : roundEnv.getRootElements()) {
					logger.finest("Pattern based processor: " + element);
					if (!(element instanceof TypeElement)) {
						continue;
					}
					Name qualifiedName = ((TypeElement)element).getQualifiedName();
					if (pattern.matcher(qualifiedName).find()) {
						elements.add(element);
						logger.finest("Pattern based processor: adding element: " + element);
					}
				}

				logger.finest("Pattern based processor: all elements: " + elements);
				return elements;
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
	
	private InputStream getConfiguration(CrumbsWay way, Config config) throws IOException {
		String path = config.getFilePath(way);
		
		logger.fine("conf path=" + path + " " + this.getClass().getResourceAsStream(path) + ", " + projectClassLoader.getResourceAsStream(path));
		
		InputStream configIn = projectClassLoader.getResourceAsStream(config.getFilePath(way));
		if (configIn != null) {
			// if resource is already created, i.e. already copied from source directory we can read it as a resource 
			return configIn;
		}

		//TODO: remove casting. Store the URL in class member. 
		// if not, try to locate it in source directory. 
		for (URL url : ((URLClassLoader)projectClassLoader).getURLs()) {
			File f;
			try {
				f = new File(new File(url.toURI()), path);
				File propsSrcDir = findSrcDir(f, path);
				File propsFile = new File(propsSrcDir, path);
				logger.fine("propsFile=" + propsFile.getAbsolutePath() + ", " + propsFile.exists());
				return new FileInputStream(propsFile);
			} catch (URISyntaxException e) {
				throw new IllegalArgumentException(e);
			}
		}
		
		
		return null;
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
			processingEnv.getMessager().printMessage(Kind.ERROR, e.getMessage());
			return Collections.emptyList();
		}
		
		Collection<String> classNames = new LinkedHashSet<String>();
		
		for(String name : metadata.getBeanNames()) {
			logger.finest("bean name: " + name);
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
		logger.fine("sprinkleBeanCrumbs way: " + way + ", for beans: " + metadata.getBeanNames(way));

		File generatedSrcDir = null;
		File generatedSrcProjectRoot;// = new File(".").getCanonicalFile();
		
		
		if (props != null) {
			String generatedSrcProjectProp = props.getProperty(GENERATED_SRC_PROJECT_PROP);
			String generatedSrcProp = props.getProperty(GENERATED_SRC_DIR_PROP);
			if (generatedSrcProjectProp != null) {
				generatedSrcProjectRoot = new File(generatedSrcProjectProp);
				if (generatedSrcProp != null) {
					generatedSrcDir = new File(generatedSrcProjectRoot, generatedSrcProp);
				}				
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

			FileObject output;
			try {
				output = processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, packageName, way.getClassName(simpleName) + ".java");
			} catch (FilerException e) {
				continue;
			}
			FileObject input = processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, packageName, simpleName + ".java");
			File generatedSrcFile = new File(output.toUri());
			File originalSrcFile = new File(input.toUri());
			
			if (generatedSrcDir == null) {
				generatedSrcDir = findSrcDir(originalSrcFile, packageName, simpleName);
			}

			logger.fine("Source directory for generated sources is: " + generatedSrcDir);
		
			if (generatedSrcDir == null) {
				throw new IllegalStateException("Cannot find directory for generated source files");
			}
			
			File srcFile = new File(new File(generatedSrcDir, packageName.replace('.', '/')), generatedSrcFile.getName());
			srcFile.getParentFile().mkdirs();

			logger.fine("Write crumbs for class " + name + " package: " + packageName + ", simple name=" + simpleName + ": " + way.getClassName(simpleName) + " to " + srcFile.getAbsolutePath());
			
			OutputStream out = new FileOutputStream(srcFile);
			way.strew(name, metadata, out);
			out.flush();
			out.close();
		}
	}

	
	private File findSrcDir(File path, String packageName, String simpleName) {
		return findSrcDir(path, packageNameToPath(packageName) + "/" + simpleName + ".java");
	}

	
	private String packageNameToPath(String packageName) {
		return packageName.replace('.', '/');
	}

	private File findSrcDir(File file, String path) {
		String[] pathFragments = path.split("/");

		File f = file;
		for (int i = pathFragments.length - 1; i >= 0; i--) {
			if (!f.getName().equals(pathFragments[i])) {
				throw new IllegalArgumentException("Wrong path fragment# "  + i + " " + pathFragments + ": " + file + " does not match " + path);
			}
			f = f.getParentFile();
		}
		
		for (; f != null; f = f.getParentFile()) {
			File root = findPath(f, path);
			if (root != null) {
				return root;
			}
		}
		
		return null;
	}

	
	private File findPath(File root, String path) {
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
			File res = findPath(f, path);
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
	
	
//	private static void log(String msg) {
//		try {
//			PrintWriter writer = new PrintWriter(new FileWriter(new File("/tmp/mylog.log"), true));
//			writer.println(msg);
//			writer.flush();
//			writer.close();
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//	}
}