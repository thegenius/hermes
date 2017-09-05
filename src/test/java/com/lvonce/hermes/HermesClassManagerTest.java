
package com.lvonce.hermes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.testng.annotations.*;
import static org.testng.Assert.*;
import com.lvonce.hermes.prepares.Proxy;
import com.lvonce.hermes.prepares.Target;

public class HermesClassManagerTest {

    private static final Logger logger = LoggerFactory.getLogger(HermesClassManagerTest.class);
    public String createSourceString1() {
        StringBuilder builder = new StringBuilder();
        builder.append("package com.lvonce;");
        builder.append("    public class TestNew {");
        builder.append("        public static void main(String[] args) {");
        builder.append("            System.out.println(\"Hello new\");" );
        builder.append("        }");
        builder.append("    public int add(int x, int y) {");
        builder.append("        return x + y;");
        builder.append("    }");
        builder.append("    public void __setReloadTarget__(Object target) {");
        builder.append("        ;");
        builder.append("    }");
        builder.append("    public int add(int x, int y) {");
        builder.append("        return x + y;");
        builder.append("    }");
        builder.append("}");
        return builder.toString();
    }

    public String createSourceString2() {
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



    @Test
    public void testConstructor() {
        HermesClassManager manager = new HermesClassManager();
        assertEquals(manager.getProxyClass(), null);
        manager = new HermesClassManager(Proxy.class);
        assertNotNull(manager.getProxyClass());
        manager.update(Target.class);
        Proxy proxy = (Proxy)manager.createInstance(23);
        assertNotNull(proxy);
        assertEquals(proxy.add(16, 41), 80);        
    }

}