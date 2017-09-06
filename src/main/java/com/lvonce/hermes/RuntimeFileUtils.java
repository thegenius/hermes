package com.lvonce.hermes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.function.Predicate;
import java.net.URISyntaxException;
import java.net.URL;

public class RuntimeFileUtils {

    public static final Logger logger = LoggerFactory.getLogger(RuntimeFileUtils.class);

    public static void collectClasses(Predicate<Class<?>> predicate, Path root, ArrayList<Class<?>> childrenClassList) {
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

    public static Class<?>[] collectClasses(Predicate<Class<?>> predicate) {
        logger.debug("collectClasses({})", predicate);
        try {
            Enumeration<URL> urls = RuntimeFileUtils.class.getClassLoader().getSystemResources("");
            ArrayList<Class<?>> childrenClasses = new ArrayList<>();
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

    public static Class<?>[] collectChildrenClasses(Class<?> parentClass) {
        return collectClasses((Class<?> childClass) -> {
            return parentClass.isAssignableFrom(childClass);
        });
    }

    public static Class<?>[] collectAnnotationClasses(Class<? extends Annotation> annotation) {
        return collectClasses((Class<?> childClass) -> {
            return childClass.isAnnotationPresent(annotation);
        });
    }

}