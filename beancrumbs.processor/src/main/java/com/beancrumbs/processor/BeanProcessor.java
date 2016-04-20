package com.beancrumbs.processor;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * Abstract base class for bean processors. It concretizes the type parameters defined in
 * {@link BeanParser} using annotation processor terms like 
 * {@link Element}, {@link TypeElement}, {@link ExecutableElement}
 *  
 * @author alexr
 *
 */
public abstract class BeanProcessor extends BeanParser<Element, TypeElement, ExecutableElement> {
	private static final Logger logger = Logger.getLogger(BeanProcessor.class.getName()); 
	
	@Override
	protected Set<? extends TypeElement> getTypes() {
		Set<TypeElement> result = new LinkedHashSet<TypeElement>();
		
		Set<? extends Element> elements = getElements();
		for (Element element : elements) {
			if (element instanceof TypeElement) {
				result.add((TypeElement)element);
			}
		}
		return result;
	}
	
	@Override
	protected Set<? extends ExecutableElement> getMethods(TypeElement type) {
		Set<ExecutableElement> methods = new LinkedHashSet<ExecutableElement>();
		for (Element element : type.getEnclosedElements()) {
			ElementKind kind = element.getKind();
			
			if (!ElementKind.METHOD.equals(kind)) {
				continue;
			}
			ExecutableElement method = (ExecutableElement)element;
			methods.add(method);
		}
		return methods;
	}

	@Override
	protected String getTypeName(TypeElement element) {
		return element.getQualifiedName().toString();
	}

	@Override
	protected String getMethodName(ExecutableElement method) {
		return method.getSimpleName().toString();
	}

	@Override
	protected boolean isVoid(ExecutableElement method) {
		return TypeKind.VOID.equals(method.getReturnType().getKind());
	}

	@Override
	protected String[] getMethodParemeterTypes(ExecutableElement method) {
		List<? extends VariableElement> params = method.getParameters();
		String[] paramNames = new String[params.size()];
		int i = 0;
		for (VariableElement param : params) {
			logger.finest("param kind is: " + param.getKind() + ", " + param.asType());
			paramNames[i] = param.asType().toString();
			i++;
		}
		return paramNames;
	}

	@Override
	protected String getMethodReturnType(ExecutableElement method) {
		return method.getReturnType().toString();
	}

	@Override
	protected String getSuperTypeName(TypeElement type) {
		TypeMirror mirror = type.getSuperclass();
		return (mirror instanceof NoType) ? null : mirror.toString();
	}

	@Override
	protected boolean isCrumbed(TypeElement type) {
		return type.getAnnotation(Crumbed.class) != null;
	}
	
}
