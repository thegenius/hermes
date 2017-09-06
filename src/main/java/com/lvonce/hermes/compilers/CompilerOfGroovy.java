package com.lvonce.hermes.compilers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.lvonce.hermes.ReflectUtils;

public class CompilerOfGroovy implements Compiler {
	private static final Logger logger = LoggerFactory.getLogger(CompilerOfGroovy.class);
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
			loader = constructor.newInstance(CompilerOfGroovy.class.getClassLoader());	
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

	@Override
	public boolean supported() {
		return groovyParseMethod != null;
	}


	@Override
	public String getSourceFileSuffix() {
		return ".groovy";
	}

	@Override
	public Class<?>[] compile(Iterable<File> sourceFiles) {
		ArrayList<Class<?>> classes = new ArrayList<>();
		for (File file : sourceFiles) {
			classes.add(findClassByGroovyFile(file));
		}
		return classes.toArray(new Class<?>[classes.size()]);
	}

	public static Class<?> findClassByGroovyFile(File file) {
		if (groovyParseMethod == null) {
			logger.warn("groovy compile is not supported, please add groovy dependency to your project!");
			return null;
		}
		try {
			Object source = ReflectUtils.createInstance(groovyCodeSourceClass, file);
			Class<?> classType = (Class<?>)groovyParseMethod.invoke(groovyClassLoader, source, false);
			return classType;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
