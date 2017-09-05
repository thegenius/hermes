package com.lvonce.hermes;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.BeforeClass;

import static org.testng.Assert.*;
import static com.lvonce.hermes.EntityFactory.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;

public class CompilerTest {

    public String createSourceString() {
        StringBuilder builder = new StringBuilder();
        builder.append("package com.lvonce;");
        builder.append("    public class TestNew {");
        builder.append("        public static void main(String[] args) {");
        builder.append("            System.out.println(\"Hello new\");" );
        builder.append("        }");
        builder.append("    public int add(int x, int y) {");
        builder.append("        return x + y;");
        builder.append("    }");
        builder.append("}");
        return builder.toString();
    }
    public File createSourceFile() {
        try {
            File file = new File("TestNew.java");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file);
            writer.append(createSourceString());
            writer.flush();
            writer.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Test
    public void test() {
        try {
            File file = createSourceFile();
            byte[] classData = Compiler.compile(file);
            Class<?> classType = new ClassLoader() {
                public Class<?> defineClass(byte[] bytes) {
                    return super.defineClass(null, bytes, 0, bytes.length);
                }
            }.defineClass(classData);
            Object obj = classType.newInstance();
            System.out.println("class name: " + classType.getName());
            Method method = classType.getDeclaredMethod("add", new Class<?>[]{int.class, int.class});
            int result = (int)method.invoke(obj, 1, 2);
            assertEquals(result, 3);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void test2() {
        try {
            byte[] classData = Compiler.compile("com.lvonce.TestNew", createSourceString());
            Class<?> classType = new ClassLoader() {
                public Class<?> defineClass(byte[] bytes) {
                    return super.defineClass(null, bytes, 0, bytes.length);
                }
            }.defineClass(classData);
            Object obj = classType.newInstance();
            System.out.println("class name: " + classType.getName());
            Method method = classType.getDeclaredMethod("add", new Class<?>[]{int.class, int.class});
            int result = (int)method.invoke(obj, 1, 2);
            assertEquals(result, 3);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
