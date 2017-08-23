package com.lvonce.hermes;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
//import groovy.lang.GroovyCodeSource;
//import groovy.lang.GroovyClassLoader;

public class GroovyScriptSupport {

	private static final Method groovyParseMethod;
	private static final Object groovyClassLoader;
	private static final Class<?> groovyCodeSourceClass;

	static {
		Class<?> sourceClass = null;
		Class<?> loaderClass = null;
		Object loader = null;
		Method method = null;
		try {
			sourceClass = Class.forName("groovy.lang.GroovyCodeSource");
			loaderClass = Class.forName("groovy.lang.GroovyClassLoader");
			method = loaderClass.getDeclaredMethod("parseClass", sourceClass, boolean.class);
			Constructor<?> constructor = loaderClass.getDeclaredConstructor(ClassLoader.class);
			loader = constructor.newInstance(GroovyScriptSupport.class.getClassLoader());	
		} catch (ClassNotFoundException | 
				NoSuchMethodException |
				InstantiationException |
				IllegalAccessException |
				InvocationTargetException |
                SecurityException e) {
		 	loader = null;
		 	method = null;
			sourceClass = null;
			e.printStackTrace();
		} finally {
			groovyClassLoader = loader;	
			groovyParseMethod = method;
			groovyCodeSourceClass = sourceClass;
		}
	}

	public static boolean exists() {
		return groovyParseMethod != null;
	}

	public static Class<?> findClassByGroovyFile(File file) {
		if (groovyParseMethod == null) {
			return null;
		}
		try {
			Object source = ClassManager.createInstance(groovyCodeSourceClass, file);
			Class<?> classType = (Class<?>)groovyParseMethod.invoke(groovyClassLoader, source, false);
			return classType;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
