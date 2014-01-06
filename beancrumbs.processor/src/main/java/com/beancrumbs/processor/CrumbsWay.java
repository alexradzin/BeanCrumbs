package com.beancrumbs.processor;

import java.io.OutputStream;
import java.util.Collection;
import java.util.Properties;

/**
 * This interface defines contract of "CrumbsWay" - class that generates resource from 
 * beans metadata.  
 * @author alexr
 */
public interface CrumbsWay {
	/**
	 * Performs the resource generation. Writes its content to output stream accepted as an argument.
	 * @param fullClassName fully qualified class name of generated resource
	 * @param data metadata of source bean.
	 * @param stream the stream where to write 
	 */
	public void strew(String fullClassName, BeansMetadata data, OutputStream stream, Properties props);
	
	/**
	 * Retrieves a collection of fully qualified class names of annotations
	 * used by current {@link CrumbsWay} to identify classes that have to be 
	 * processed.
	 * 
	 * @return collection of fully qualified class name of markers 
	 */
	public Collection<String> getMarkers();
	
	/**
	 * Retrieves the symbolic name of current {@link CrumbsWay}.
	 * @return the {@link CrumbsWay} name
	 */
	public String getName();
	
	/**
	 * Generate the class name of resulting resource from 
	 * the fully qualified name of original class.
	 *  
	 * @param originalClassName
	 * @return generated class name 
	 */
	public String getClassName(String originalClassName);
}
