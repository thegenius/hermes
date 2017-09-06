package com.lvonce.hermes;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.BeforeClass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.testng.Assert.*;
import static com.lvonce.hermes.EntityFactory.*;

import java.io.BufferedWriter;
import java.io.File;
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