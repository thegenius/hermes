package com.lvonce.hermes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.WeakHashMap;
//import groovy.lang.GroovyCodeSource;
//import groovy.lang.GroovyClassLoader;

import java.lang.SecurityException;
import java.lang.NoSuchMethodException;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.InvocationHandler;  
import java.lang.reflect.Method;  
import java.lang.reflect.Proxy;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
public class ClassManager<T> {

	private static Logger logger = LoggerFactory.getLogger(ClassManager.class);
	private static final String hotswapBase = ".reload/";
	private static final String srcBase = "src/main/";
	private static final String testBase = "src/test/";
	private static final String targetBase = "target/";

	private final String classFilePath;
	private final Class<T> interfaceType;
	private final Map<EntityProxy<T>, Object[]> objectRefs;
	private Class<T> implementClass;
	
	public ClassManager(Class<T> interfaceType, String filePath) {
		this.classFilePath = filePath;
		this.interfaceType = interfaceType;
		this.objectRefs = new WeakHashMap<EntityProxy<T>, Object[]>();
		this.implementClass = (Class<T>)findClass(filePath);
	}
	
	public void load() {
		load(new File(this.classFilePath));	
	}
	
	public void load(File file) {
		try {
			logger.debug( "load " + file.toString());
			this.implementClass = (Class<T>)findClassByFile(file);
			Iterator<Map.Entry<EntityProxy<T>, Object[]>> it = this.objectRefs.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<EntityProxy<T>, Object[]> entry = it.next();
				EntityProxy<T> proxy = entry.getKey();
				if (proxy.getTarget() != null) {
					proxy.setTarget(createInstance(entry.getValue())); 
				} 				
			}
		} catch (Exception e) {
			e.printStackTrace();
			this.implementClass = null;
		}
	}

	public static <E> E createInstance(Class<E> classType, Object ...args) {
		try {
			E obj = null;
			if (classType != null) {
				if (args == null || args.length == 0) {	
					obj = (E)classType.newInstance();
				} else {
					Class[] paramTypes = new Class[args.length];	
					for (int i=0; i<args.length; ++i) {
						paramTypes[i] = args[i].getClass();
					}
					Constructor<E> constructor = (Constructor<E>)classType.getDeclaredConstructor(paramTypes);
					obj = constructor.newInstance(args);
				}
			}
			return obj;
		} catch (InstantiationException | 
					IllegalAccessException | 
					SecurityException |
					InvocationTargetException | 
					NoSuchMethodException e) {
    		e.printStackTrace();
    		return null;
		}
	}

	private T createInstance(Object ...args) {
		return createInstance(this.implementClass, args);
	}

	public T newInstance(Object ...args) {
		T obj = createInstance(args);
		if (obj == null) {
			return null;
		}
		EntityProxy<T> proxy = new EntityProxy(obj);
		this.objectRefs.put(proxy, args);
		return proxy.getProxy();
	}

	public static Class<?> findClassByClassFile(File file) {
		try {
			Path path = file.toPath();
			byte[] data = Files.readAllBytes(path);
			return new ClassLoader() {
    		    	public Class<?> defineClass(byte[] bytes) {
    		        	return super.defineClass(null, bytes, 0, bytes.length);
    		    	}
    			}.defineClass(data);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Class<?> findClassByGroovyFile(File file) {
		return GroovyScriptSupport.findClassByGroovyFile(file);
		//try {
		//	GroovyCodeSource source = new GroovyCodeSource(file);
		//	Class<?> classType = EntityFactory.getGroovyClassLoader().parseClass(source, false);
		//	return classType;
		//} catch (Exception e) {
		//	e.printStackTrace();
		//	return null;
		//}
	}

	public static Class<?> findClassByFile(File file) {
		String fileName = file.toPath().toString();
		if (fileName.endsWith(".class")) {
			return findClassByClassFile(file);
		}
		if (fileName.endsWith(".groovy")) {
			return findClassByGroovyFile(file);
		}	
		return null;
	}

	private static File searchFile(String basePath, String fileName, String suffix) {
		if (!basePath.endsWith("/")) {
			basePath = basePath + "/";
		}
		File file = new File(basePath + fileName + suffix);	
		logger.debug("search file : " + file.toPath().toString());
		if (file.exists() && file.isFile()) {
			logger.debug("found file : " + file.toPath().toString());
			return file;
		}

		String[] filePaths = fileName.split("/");
		if (filePaths.length > 1) {
			file = new File(basePath + filePaths[filePaths.length-1] + suffix);
			logger.debug("search file : " + file.toPath().toString());
			if (file.exists() && file.isFile()) {
				logger.debug("found file : " + file.toPath().toString());
				return file;
			}
		}
		return null;
	}

	public static Class<?> findClass(String className) {
		try {
			String filePath = className.replace(".", "/");
			File file = searchFile(hotswapBase + "groovy/", filePath, ".groovy");
			if (file != null) {
				return findClassByGroovyFile(file);
			}

			file = searchFile(hotswapBase, filePath, ".groovy");
			if (file != null) {
				return findClassByGroovyFile(file);
			}

			file = searchFile(srcBase + "groovy/", filePath, ".groovy");
			if (file != null) {
				return findClassByGroovyFile(file);
			}

			file = searchFile(srcBase, filePath, ".groovy");
			if (file != null) {
				return findClassByGroovyFile(file);
			}
			
			file = searchFile(testBase + "groovy/", filePath, ".groovy");
			if (file != null) {
				return findClassByGroovyFile(file);
			}

			file = searchFile(testBase, filePath, ".groovy");
			if (file != null) {
				return findClassByGroovyFile(file);
			}
			
			file = searchFile(hotswapBase + "classes/", filePath, ".class");
			if (file != null) {
				return findClassByClassFile(file);
			}
			
			file = searchFile(hotswapBase, filePath, ".class");
			if (file != null) {
				return findClassByClassFile(file);
			}
			
			file = searchFile(targetBase + "classes/", filePath, ".class");
			if (file != null) {
				return findClassByClassFile(file);
			}
			
			file = searchFile(targetBase, filePath, ".class");
			if (file != null) {
				return findClassByClassFile(file);
			}

			return Class.forName(className);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 	
	}
}


