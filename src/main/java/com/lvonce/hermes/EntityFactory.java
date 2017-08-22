package com.lvonce.hermes;
import com.lvonce.hermes.FileWatcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.WeakHashMap;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyClassLoader;

import java.lang.SecurityException;
import java.lang.NoSuchMethodException;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.InvocationHandler;  
import java.lang.reflect.Method;  
import java.lang.reflect.Proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyClassLoader;

public class EntityFactory implements FileWatcherHandler {

	private static final Logger logger = LoggerFactory.getLogger(EntityFactory.class);

	private static final GroovyClassLoader groovyClassLoader 
		= new GroovyClassLoader(EntityFactory.class.getClassLoader());	

	private static final Map<String, ClassManager<?>> classManagerMap
		= new LinkedHashMap<String, ClassManager<?>>();

	private static EntityFactory instance = new EntityFactory();
	static {
		commandLine("mkdir -p .reload");
		FileWatcher.register("src", instance);
		FileWatcher.register(".reload", instance, true);
		FileWatcher.watch();
	}

	private EntityFactory() {}
	
	public static EntityFactory getInstance() {
		return instance;
	}
	
	public static GroovyClassLoader getGroovyClassLoader() {
		return groovyClassLoader;
	}
	
	private static void copyFile(File source, File dest) {    
        FileChannel inputChannel = null;    
        FileChannel outputChannel = null;    
    	try {
			if (!dest.exists()) {
				dest.createNewFile();
			}
    	    inputChannel = new FileInputStream(source).getChannel();
    	    outputChannel = new FileOutputStream(dest).getChannel();
    	    outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
    	} catch (Exception e) { 
			e.printStackTrace();
		} finally {
			try {
    	    	inputChannel.close();
    	    	outputChannel.close();
    		} catch (Exception e) { 
				e.printStackTrace();
			}
    	}
	}


	public static String getSystemClassPath() {
		String classPath = "target/classes";
		Map<String, String> envs = System.getenv();
		for (Map.Entry<String, String> entry : envs.entrySet()) {
			String envKey = entry.getKey();
			envKey = envKey.toLowerCase();
			if (envKey.equals("classpath")) {
				if (OSValidator.isWindows()) {
					return classPath + ";" + entry.getValue();
				} else {
					return classPath + ":" + entry.getValue();
				}
			}
		}
		return classPath;
	}


	public void updateByFile(File file) {
		try {
			String filePath = file.toPath().toString();
			if (filePath.endsWith(".groovy") || filePath.endsWith(".class")) {
				String className = ClassManager.findClassByFile(file).getName();
				ClassManager<?> manager = classManagerMap.get(className);
				if (manager != null) {
					manager.load(file);	
				} else {
					logger.debug( "manager is null");
				}
			}
			if (filePath.endsWith(".java")) {
				commandLine("javac " + filePath + " -cp " + getSystemClassPath() + " -d .reload");
			}
			if (filePath.endsWith(".kt")) {
				commandLine("kotlinc " + filePath + " -cp " + getSystemClassPath() + " -d .reload");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int commandLine(String cmd) {
		try {
			Process process = Runtime.getRuntime().exec(cmd);
			logger.info("command line: " + cmd);
			int exitValue = process.waitFor();
			logger.info("command line: " + cmd + " return value: " + exitValue);
			return exitValue;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	public static<T> T create(Class<T> interfaceType, String className, Object... args) {
		try {
			className = ClassManager.findClass(className).getName();
			ClassManager<T> manager = (ClassManager<T>)classManagerMap.get(className);
			if (manager == null) {
				manager = new ClassManager(interfaceType, className);
				classManagerMap.put(className, manager);
			}
			return manager.newInstance(args);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}

