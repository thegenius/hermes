package com.lvonce.hermes;

import java.io.File;
import java.util.Map;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import com.lvonce.hermes.compilers.Compiler;

public class HermesClassLoader extends ClassLoader {
    private static final Logger logger = LoggerFactory.getLogger(HermesClassLoader.class);
    private static final HermesClassLoader instance;
    private static final Map<String, FileWatcherNext> watchers;
    private static final Map<String, HermesClassManager> classCache;

    static {
        instance = new HermesClassLoader();
        classCache = new LinkedHashMap<String, HermesClassManager>();
        watchers = new LinkedHashMap<String, FileWatcherNext>();
        addWatcher("src");
    }

    private HermesClassLoader() {
    }

    public static HermesClassLoader getInstance() {
        return instance;
    }

    public static void addWatcher(String dir) {
        logger.debug("addWatcher({})", dir);
        if (!watchers.containsKey(dir)) {
            FileWatcherNext watcher = FileWatcherNext.create(dir, HermesClassLoader::update, true);
            watcher.watch();
            watchers.put(dir, watcher);
        }
    }

    public static void delWatcher(String dir) {
        logger.debug("delWatcher({})", dir);
        if (watchers.containsKey(dir)) {
            FileWatcherNext watcher = watchers.get(dir);
            watcher.stop();
            watchers.remove(dir);
        }
    }

    public static Object create(String className, Object... args) {
        // new Throwable().printStackTrace();
        logger.debug("create({}, {})", className, args);
        HermesClassManager manager = classCache.get(className);
        if (manager == null) {
            try {
                Class<?> targetClass = instance.loadClass(className);
                manager = new HermesClassManager();
                manager.update(targetClass);
                classCache.put(className, manager);
            } catch (ClassNotFoundException e) {
                logger.warn("create({}), but there is no such class, please write it.", className);
                return null;
            }
        }
        return manager.createInstance(args);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (classCache.containsKey(name)) {
            logger.debug("loadClass({}) from cache", name);
            return classCache.get(name).getImplementClass();
        } else {
            logger.debug("loadClass({}) from super", name);
            return super.loadClass(name, false);
        }
    }

    public static void update(File file) {
        logger.debug("update({})", file.getName());
        Class<?>[] classes = Compiler.compileFile(file);
        for (Class<?> classType : classes) {
            putClassDefinition(classType.getName(), classType);
        }
    };

    public static synchronized void putClassDefinition(String name, Class<?> definition) {
        logger.debug("putClassDefinition({})", name);
        if (!classCache.containsKey(name)) {
            classCache.put(name, new HermesClassManager());
        }
        classCache.get(name).update(definition);
    }

}