package com.lvonce.hermes.compilers;

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
import java.util.Map;

import com.lvonce.hermes.ReflectUtils;
import com.lvonce.hermes.RuntimeFileUtils;
import com.lvonce.hermes.compilers.CompilerOfJava;

@Test
public class CompilerTest {

    public static final Logger logger = LoggerFactory.getLogger(CompilerTest.class);

    public String createSourceString() {
        StringBuilder builder = new StringBuilder();
        builder.append("package com.lvonce;");
        builder.append("    public class TestNew {");
        builder.append("        public static void main(String[] args) {");
        builder.append("            System.out.println(\"Hello new\");");
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
    public void testCompileFile() {
        try {
            File file = createSourceFile();
            Class<?>[] classTypes = Compiler.compileFile(file);
            Object obj = ReflectUtils.createInstance(classTypes[0]);
            int result = (int) ReflectUtils.invoke(obj, "add", 1, 2);
            assertEquals(result, 3);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testChildCompilers() {
        for (Map.Entry<String, Compiler> entry : Compiler.compilers.entrySet()) {
            logger.info("{} -> {}", entry.getKey(), entry.getValue());
        }
    }

}
