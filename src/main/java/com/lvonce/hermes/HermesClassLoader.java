package com.lvonce.hermes;

import java.io.File;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.lang.reflect.Field;
import java.util.function.Consumer;

public class HermesClassLoader extends ClassLoader {
    private static final Logger logger = LoggerFactory.getLogger(HermesClassLoader.class);
    private static final HermesClassLoader instance;
    private static final FileWatcherNext watcher;
    private static final Map<String, HermesClassManager> classCache;

    static {
        instance = new HermesClassLoader();
        classCache = new LinkedHashMap<String, HermesClassManager>();
        watcher = FileWatcherNext.create("src", HermesClassLoader::update, true);
        // watcher = FileWatcherNext.create("src", (File file) -> {
        //     logger.debug("update handle func: {}", file.getName());
        //     if (file.getName().endsWith(".java")) {
        //         byte[] classData = Compiler.compile(file);
        //         if (classData != null) {
        //             putClassDefinition(classData);
        //         }
        //     }
        // }, true);
        watcher.watch();
    }

    public static HermesClassLoader getInstance() {
        return instance;
    }

    private static void update(File file) {
        logger.debug("update handle func: {}", file.getName());
        if (file.getName().endsWith(".java")) {
            byte[] classData = Compiler.compile(file);
            if (classData != null) {
                putClassDefinition(classData);
            }
        }
    };

    public static Object create(String className, Object... args) {
        try {
            className = ClassManager.findClass(className).getName();
            HermesClassManager manager = classCache.get(className);
            if (manager != null) {
                return manager.createInstance(args);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (classCache.containsKey(name)) {
            logger.debug("loadClass({}) from cache", name);
            return classCache.get(name).getImplementClass();
        }
        logger.debug("loadClass({}) from super", name);
        return super.loadClass(name, false);
    }

    public static synchronized void putClassDefinition(String name, Class<?> definition) {
        logger.debug("putClassDefinition({})", name);
        if (!classCache.containsKey(name)) {
            classCache.put(name, new HermesClassManager());
        }
        classCache.get(name).update(definition);
        ;
    }

    public static void putClassDefinition(byte[] definition) {
        Class<?> classDef = instance.defineClass(null, definition, 0, definition.length);
        putClassDefinition(classDef.getName(), classDef);
    }

}