package com.beancrumbs.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;

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
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedOptions({Options.BEANCRUMBS_DIR_OPTION, Options.BEANCRUMBS_LOG_OPTION, Options.BEANCRUMBS_LOG_LEVEL_OPTION, Options.BEANCRUMBS_ENABLED_OPTION})
public class BeanCrumber extends AbstractProcessor {
	private final static String CLASS_ANNOTATION_PROP = "class.annontation";
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

		log("Starting processing");
		for (CrumbsWay way : getWays()) {
			log("Go on way " + way);
			
			Properties props = null;
			Collection<String> classNames;
			try {
				props = getConfigurationProperties(getConfiguration(way, Config.PROPERTIES));
				classNames = getIndex(getConfiguration(way, Config.INDEX));
				System.out.println("Index: " + classNames);
			} catch (Exception ex) {
				ex.printStackTrace();
				processingEnv.getMessager().printMessage(Kind.ERROR,
						ex.getMessage());
				continue;
			}
			
			
			Collection<Class<? extends Annotation>> markers = getMarkers(props, way);
			if (markers != null) {
				for (Class<? extends Annotation> marker : markers) {
					log("Marker " + marker);
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
					sprinkleBeanCrumbs(way);
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
						e.printStackTrace();
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
		System.out.println("url=" + url );
		return new URLClassLoader(new URL[] {url}, getClass().getClassLoader());
	}
	
	private InputStream getConfiguration(CrumbsWay way, Config config) {
		System.out.println("conf path=" + config.getFilePath(way) + " " + this.getClass().getResourceAsStream(config.getFilePath(way)));
		
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
			System.out.println("index is null");
			return Collections.emptyList();
		}
		Collection<String> classNames = new ArrayList<String>();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		for (String line = reader.readLine();  line != null;  line = reader.readLine()) {
			String className = line.trim();
			classNames.add(className);
		}
		
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
	private void sprinkleBeanCrumbs(CrumbsWay way) throws IOException {
		System.out.println("sprinkleBeanCrumbs 1, way: " + way + ", " + metadata.getBeanNames());
		for (String name : metadata.getBeanNames(way)) {
			System.out.println("Write skeleton for class " + name);
			String packageName = "";
			String simpleName = name;
			int lastDot = name.lastIndexOf('.');
			if (lastDot >= 0) {
				packageName = name.substring(0, lastDot);
				simpleName = name.substring(lastDot + 1); 
			}

			System.out.println("Write skeleton for class " + name + " package: " + packageName + ", simple name=" + simpleName + ": " + way.getClassName(simpleName) );
			
			FileObject output = processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, packageName, way.getClassName(simpleName) + ".java");

			File srcFile = getFileInSrcFolder(new File(output.toUri()), packageName);
			srcFile.getParentFile().mkdirs();
			OutputStream out = new FileOutputStream(srcFile);
			way.strew(name, metadata, out);
			out.flush();
			out.close();
		}

		System.out.println("sprinkleBeanCrumbs 2");
	}
	
	private File getFileInSrcFolder(File fileInApt, String packageName) {
		File aptDir = fileInApt.getParentFile();
		String fileName = fileInApt.getName();
		// go up through the package based directories
		int n = "".equals(packageName) ? 0 : packageName.split("\\.").length;
		for (int i = 0; i < n; i++) {
			aptDir = aptDir.getParentFile();
		}
		System.out.println("aptdir: " + aptDir);
		File projectRootDir = aptDir.getParentFile();
		//TODO: add discovery of source folder. Now it is hard coded to src that relevant for not-maven projects only
		return new File(new File(projectRootDir, "src/" + packageName.replace('.', '/')), fileName);
	}
	
	
	// discovers required crumb ways (from data files and annotations) and returns the list.
	private Iterable<CrumbsWay> getWays() {
		if (ways == null) {
			ways = ServiceLoader.load(CrumbsWay.class, projectClassLoader);
		}
		System.out.println("ways: " + ways);
		return ways;
	}
	
	private void log(String msg) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(new FileWriter(new File(new File(System.getProperty("java.io.tmpdir")), "beancrumbs.log"), true));
			writer.println(new Date() + " " + msg);
			writer.flush();
			writer.close();
			System.out.println(new Date() + " " + msg);
		} catch (IOException e) {
			processingEnv.getMessager().printMessage(Kind.ERROR, e.getMessage());
		}
		
	}
	
	
	public static void main(String[] args) {
//		ServiceLoader<Processor> processors = ServiceLoader.load(Processor.class);
//		System.out.println("processors: " + processors);
//		for (Processor p : processors) {
//			System.out.println("processor: " + p);
//		}
//		
//		
//		ServiceLoader<CrumbsWay> ways = ServiceLoader.load(CrumbsWay.class);
//		System.out.println("ways: " + ways);
//		for (CrumbsWay w : ways) {
//			System.out.println("way: " + ways);
//		}

		System.out.println(int[].class.getSuperclass());
		
	}
}
