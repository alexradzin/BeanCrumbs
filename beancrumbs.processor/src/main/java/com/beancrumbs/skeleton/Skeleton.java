package com.beancrumbs.skeleton;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marker annotation that can be used to mark source classes for creating of 
 * skeletons. 
 * 
 * Skeleton is a class that contains {@code public final static String} fields
 * for each property of source bean. 
 * For example skeleton for bean called {@code Person} has to properties:
 *  
 * <ul>
 * 	<li>{@code String name}</li> 
 * 	<li>{@code int age}</li> 
 * </ul>
 * 
 * will be called {@code PersonSkeleton}:
 * <pre><code>
 * public class PersonSkeleton {
 * 		public final static String name = "name";
 * 		public final static String age = "age";
 * }
 * </code></pre> 
 * 
 * There are the following ways to ask BeanCrumbs to generate {@code PersonSkeleton}
 * from {@code Person}:
 * <ol>
 * 	<li>mark class {@code Person} using annotation {@code Skeleton}</li> 
 * 	<li>write fully qualified class name of class {@code Person} into file {@code META-INF/beancrumbs/skeleton.index}</li> 
 * </ol>
 * 
 * <br/>
 * 
 * Sometimes it is useful to configure BeanCrumbs to work with all classes in specific package or 
 * classes matching specific pattern. The following properties in file {@code META-INF/beancrumbs/skeleton.properties} 
 * can be helpful:
 * <ol>
 * 	<li>{@code class.wildcard} where one can specify shell like wildcards for fully qualified class names, e.g. {@code com.mycompany.*Entity}</li> 
 * 	<li>{@code class.pattern} where one can specify regular expression for fully qualified class names, e.g. {@code com\.mycompany\..*Entity$}</li> 
 * </ol>
 * 
 * Sometimes it is useful to apply BeanCrumbs functionality to classes that already contain specific 
 * annotations, e.g. {@code @Entity} or {@code @XmlType}. Put comma separated list of such 
 * annotations to {@code class.annotation} property into file {@code META-INF/beancrumbs/skeleton.properties}. 
 * 
 * @author alexr
 */
@Retention(SOURCE)
@Target(TYPE)
public @interface Skeleton {

}
