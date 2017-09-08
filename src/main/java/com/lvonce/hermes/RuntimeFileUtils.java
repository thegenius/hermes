package com.lvonce.hermes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.function.Predicate;
import java.net.URISyntaxException;
import java.net.URL;

public class RuntimeFileUtils {

    public static final Logger logger = LoggerFactory.getLogger(RuntimeFileUtils.class);

    public static void collectClasses(Predicate<Class<?>> predicate, Path root, ArrayList<Class<?>> childrenClassList) {
        logger.debug("collectClasses({})", root);
        try {
            File file = root.toFile();
            if (file.isFile()) {
                byte[] classData = Files.readAllBytes(root);
                Class<?> childClass = ReflectUtils.getClass(classData);
                if (predicate.test(childClass)) {
                    childrenClassList.add(childClass);
                }
            } else if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File childFile : files) {
                    collectClasses(predicate, childFile.toPath(), childrenClassList);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Class<?>[] collectClasses(String basePath, Predicate<Class<?>> predicate) {
        try {
            Enumeration<URL> urls = RuntimeFileUtils.class.getClassLoader().getSystemResources(basePath);
            ArrayList<Class<?>> childrenClasses = new ArrayList<>();
            logger.debug("root path hasMoreElements() -> {}", urls.hasMoreElements());
            while (urls.hasMoreElements()) {
                Path path = Paths.get(urls.nextElement().toURI());
                collectClasses(predicate, path, childrenClasses);
            }
            return childrenClasses.toArray(new Class<?>[childrenClasses.size()]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Class<?>[0];
    }

    public static Class<?>[] getAllPackageClasses() throws Exception {
        return getAllPackageClasses((Class<?> classType) -> true);
    }

    public static Class<?>[] getAllPackageClasses(Predicate<Class<?>> predicate) throws Exception {
        ZipFile jarFile;
        try {
            String jarName = App.class.getProtectionDomain().getCodeSource().getLocation().toURI().toString();
            logger.debug("getAllPackageClasses -> origin jar name -> {}", jarName);
            int index = jarName.indexOf(":");
            jarName = jarName.substring(index + 1);
            logger.debug("getAllPackageClasses -> jar name -> {}", jarName);
            jarFile = new ZipFile(jarName);
        } catch (URISyntaxException e) {
            throw e;
        }

        Enumeration e = jarFile.entries();
        ArrayList<Class<?>> classes = new ArrayList<>();
        while (e.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) e.nextElement();
            logger.debug("getAllPackageClasses -> entry name -> {}", entry.getName());
            if (!entry.getName().endsWith(".class")) {
                continue;
            }
            try {
                InputStream inputStream = jarFile.getInputStream(entry);
                int len = inputStream.available();
                if (len > 0) {
                    byte[] classData = new byte[len];
                    inputStream.read(classData, 0, len);
                    Class<?> classType = ReflectUtils.getClass(classData);
                    if ((classType != null) && (predicate.test(classType))) {
                        classes.add(classType);
                    }
                }
            } catch (IOException ex) {
                logger.debug("{}", ex.getMessage());
            }
        }
        return classes.toArray(new Class<?>[classes.size()]);
    }

    public static Class<?>[] collectChildrenClasses(Class<?> parentClass) {
        logger.debug("collectChildrenClasses({})", parentClass);
        Predicate<Class<?>> predicate = (Class<?> childClass) -> {
            return parentClass.isAssignableFrom(childClass);
        };
        try {
            return getAllPackageClasses(predicate);
        } catch (Exception e) {
            return collectClasses("", predicate);
        }
    }

    public static Class<?>[] collectAnnotationClasses(Class<? extends Annotation> annotation) {
        logger.debug("collectAnnotationClasses({})", annotation);
        Predicate<Class<?>> predicate = (Class<?> childClass) -> {
            return childClass.isAnnotationPresent(annotation);
        };
        try {
            return getAllPackageClasses(predicate);
        } catch (Exception e) {
            return collectClasses("", predicate);
        }
    }

}