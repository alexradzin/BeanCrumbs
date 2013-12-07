package com.beancrumbs.skeleton;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SkeletonWriterTest {
	private final static String SKELETON = "Skeleton";
	
	
	private String className;
	
	private ClassLoader classLoader;
	
	
	public SkeletonWriterTest(File projectDir, String src, String classes, String className) throws IOException {
		this.className = className;
		classLoader = new URLClassLoader(new URL[] {new File(projectDir, classes).toURI().toURL()}, Thread.currentThread().getContextClassLoader());
	}
	

	@Test
	public void testSkeletonCreated() throws ClassNotFoundException {
		Class<?> clazz = classLoader.loadClass(className);
		assertNotNull(clazz);
		
		String skeletonClassName = className + SKELETON;
		
		Class<?> skeletonClazz = classLoader.loadClass(skeletonClassName);
		assertNotNull(skeletonClazz);
	}

	
	@Test
	public void testSkeletonIsValid() throws ClassNotFoundException, NoSuchFieldException {
		validateSkeleton(classLoader.loadClass(className), new HashSet<Class<?>>());
	}

	
	private void validateSkeleton(Class<?> clazz, Set<Class<?>> alreadyValidated) throws ClassNotFoundException, NoSuchFieldException {
		if (alreadyValidated.contains(clazz)) {
			// this prevents infinite recursion if class refers to property of the same type. 
			return;
		}
		alreadyValidated.add(clazz);
		String skeletonClassName = clazz.getName() + SKELETON;
		final Class<?> skeletonClazz;
		try {
			skeletonClazz = classLoader.loadClass(skeletonClassName);
		} catch(ClassNotFoundException e) {
			// This class does not have skeleton, so we just ignore it here. 
			// It is OK, because testSkeletonCreated() validates that skeletons 
			// have been created for all @Skeleton annotated classes. 
			return;
		}
		
		
		Map<String, Class<?>> clazzProperties = new HashMap<>();
		for (Class<?> c = clazz; !Object.class.equals(c); c = c.getSuperclass()) {
			for (Field f : c.getDeclaredFields()) {
				clazzProperties.put(f.getName(), f.getType());
			}
		}
		
		for (Method m : clazz.getMethods()) {
			String methodName = m.getName();
			final Class<?> type;
			if ((methodName.startsWith("get") || methodName.startsWith("is")) && m.getTypeParameters().length == 0) {
				type = m.getReturnType();
			} else if (methodName.startsWith("set") && m.getParameterTypes().length == 1) {
				type = m.getParameterTypes()[0];
			} else {
				continue;
			}
			String fieldName = getFieldName(m);
			
			if (clazzProperties.containsKey(fieldName)) {
				continue;
			}
			clazzProperties.put(fieldName, type);
		}
	
		
		
		for (Entry<String, Class<?>> property : clazzProperties.entrySet()) {
			String propertyName = property.getKey();
			Class<?> propertyType = property.getValue();
			if (isPrimitive(propertyType)) {
				skeletonClazz.getField(propertyName); // will throw NoSuchFieldException if field does not exist
			} else {
				validateSkeleton(propertyType, alreadyValidated);
			}
		}
	}
	
	private boolean isPrimitive(Class<?> clazz) {
		if (clazz.isPrimitive()) {
			return true;
		}
		if (Number.class.isAssignableFrom(clazz) || Boolean.class.equals(clazz) || String.class.equals(clazz)) {
			return true;
		}
		return false;
	}

	private String getFieldName(Method m) {
		String fieldName = m.getName().replaceFirst("^(get|set|is)", "");
		fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
		return fieldName;
		
	}
	
	@Parameters(name = "{3}")
	public static Collection<Object[]> parameters() throws IOException, ClassNotFoundException {
		final File thisProjectDir = new File("").getAbsoluteFile();
		final File parentProjectDir = thisProjectDir.getParentFile();
		
		
		File[] testProjects = parentProjectDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory() && !thisProjectDir.getName().equals(pathname.getName());
			}
		});

		
		Collection<Object[]> args = new ArrayList<>();
		
		for (File testProject : testProjects) {
			final String src;
			final String classes;
			if (new File(testProject, "project.properties").exists()) {
				Properties projectProperties = new Properties();
				projectProperties.load(new FileReader(new File(testProject, "project.properties")));
				src = projectProperties.getProperty("src");
				classes = projectProperties.getProperty("classes");
			} else if (new File(testProject, "build.xml").exists()) {
				src = "src";
				classes = "classes";
			} else if (new File(testProject, "pom.xml").exists()) {
				src = "src/main/java";
				classes = "target/classes";
			} else {
				continue;
			}
			
			Collection<String> sourcePaths = paths(findFiles(new File(testProject, src), Pattern.compile("^.*\\.java$")));
			Collection<String> classPaths = paths(findFiles(new File(testProject, classes), Pattern.compile("^.*\\.class$")));
			

			// validate that all source files have been compiled
			File srcDir = new File(testProject, src);
			File classDir = new File(testProject, classes);
			for (String srcPath : sourcePaths) {
				String classPath = srcPath.replace(srcDir.getPath(), classDir.getPath()).replaceFirst("\\.java$", ".class");
				assertTrue(classPath + " is absent", classPaths.contains(classPath));
			}
			
			// validate that all @Skeleton annotated classes have skeletons.
			// We parse the java source code instead of creating Class instance and getting annotation 
			// using regular java API because the annotation's retention is SOURCE, so it is not visible 
			// at runtime. 
			for (String srcPath : sourcePaths) {
				String className = srcPath.replace(srcDir.getPath(), "").replaceFirst("\\.java$", "").replaceFirst("/", "").replace('/', '.');
				if (grep(Pattern.compile("^\\s*@" + Skeleton.class.getSimpleName()), new File(srcPath)).isEmpty()) {
					continue;
				}
				
				args.add(new Object[] {testProject, src, classes, className});
			}
		}
		
		return args;
	}

	private static Collection<String> paths(Collection<File> files) {
		Collection<String> paths = new ArrayList<>();
		for (File file : files) {
			paths.add(file.getPath());
		}
		return paths;
	}
	
	private static Collection<File> findFiles(File dir, Pattern namePattern) {
		Collection<File> files = new ArrayList<>();
		return findFiles(dir, namePattern, files);
	}

	private static Collection<File> findFiles(File dir, Pattern namePattern, Collection<File> files) {
		if (dir.isDirectory()) {
			for (File f : dir.listFiles()) {
				if (f.isDirectory()) {
					findFiles(f, namePattern, files);
				} else if (namePattern.matcher(f.getName()).find()) {
					files.add(f);
				}
			}
		}
		return files;
	}
	
	
	private static Collection<String> grep(Pattern pattern, File ...files) throws IOException {
		Collection<String> matches = new ArrayList<>();
		for (File file : files) {
			try (
					BufferedReader reader = new BufferedReader(new FileReader(file));
			) {
				for (String line = reader.readLine(); line != null; line = reader.readLine()) {
					if (pattern.matcher(line).matches()) {
						matches.add(line);
					}
				}
			}
		}
		return matches;
	}
}
