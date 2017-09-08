package com.lvonce.hermes;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.BeforeClass;
import java.util.Enumeration;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.testng.Assert.*;
import static com.lvonce.hermes.EntityFactory.*;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;

import com.lvonce.hermes.ReflectUtils;
import com.lvonce.hermes.RuntimeFileUtils;
import com.lvonce.hermes.compilers.Compiler;
import com.lvonce.hermes.compilers.CompilerOfJava;

@Test
public class RuntimeFileUtilsTest {

    public static final Logger logger = LoggerFactory.getLogger(RuntimeFileUtilsTest.class);

    public void walkRoot(Path root) {
        try {
            File file = root.toFile();
            if (file.isFile()) {
                logger.debug("walkUrl -> {}", file.getName());
            } else if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File childFile : files) {
                    walkRoot(childFile.toPath());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void walkUrls(Enumeration<URL> urls) {
        logger.debug("walkUrls({}) hasMoreElements -> {}", urls, urls.hasMoreElements());
        try {
            while (urls.hasMoreElements()) {
                Path path = Paths.get(urls.nextElement().toURI());
                walkRoot(path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void loadResourceTest() {
        try {
            Enumeration<URL> urls = RuntimeFileUtils.class.getClassLoader().getSystemResources("");
            walkUrls(urls);

            urls = RuntimeFileUtils.class.getClassLoader().getResources("");
            walkUrls(urls);

            urls = RuntimeFileUtils.class.getClassLoader().getSystemResources("/");
            walkUrls(urls);

            urls = RuntimeFileUtils.class.getClassLoader().getResources("/");
            walkUrls(urls);

            urls = RuntimeFileUtils.class.getClassLoader().getSystemResources(".");
            walkUrls(urls);
            
            urls = RuntimeFileUtils.class.getClassLoader().getResources(".");
            walkUrls(urls);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test() {

        Class<?>[] childrenClasses = RuntimeFileUtils.collectChildrenClasses(Compiler.class);
        for (Class<?> childClass : childrenClasses) {
            logger.info("child class name: {}", childClass.getName());
        }

        Class<?>[] annotationClasses = RuntimeFileUtils.collectAnnotationClasses(Test.class);
        for (Class<?> childClass : annotationClasses) {
            logger.info("child class name: {}", childClass.getName());
        }
    }
}