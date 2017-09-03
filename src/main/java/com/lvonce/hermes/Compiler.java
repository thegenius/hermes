package com.lvonce.hermes;

import java.io.IOException;
import java.util.Arrays;

import javax.tools.Diagnostic;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import javax.tools.JavaFileObject;
import javax.tools.DiagnosticCollector;
import javax.tools.StandardJavaFileManager;

public class Compiler {
    public static void compile() {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager
                .getJavaFileObjectsFromStrings(Arrays.asList("MBeanDemo.java"));
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null,
                compilationUnits);
        Boolean success = task.call();
        for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
            System.console()
                    .printf("Code: %s%n" + "Kind: %s%n" + "Position: %s%n" + "Start Position: %s%n"
                            + "End Position: %s%n" + "Source: %s%n" + "Message:   %s%n", diagnostic.getCode(),
                            diagnostic.getKind(), diagnostic.getPosition(), diagnostic.getStartPosition(),
                            diagnostic.getEndPosition(), diagnostic.getSource(), diagnostic.getMessage(null));
        }
        try {
            fileManager.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Success: " + success);
    }
}