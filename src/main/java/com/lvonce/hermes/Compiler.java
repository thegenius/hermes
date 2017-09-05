package com.lvonce.hermes;

import java.net.URI;
import java.util.Arrays;

import java.io.File;
import java.io.IOException;

import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.tools.Diagnostic;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.DiagnosticCollector;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;

public class Compiler {

    private static final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    private static final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
    private static final StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

    public static byte[] compile(String className, String source) {
        return compile(className, source, ".tmp.compiler");
    }

    public static byte[] compile(String className, String source, String outputDir) {
        Iterable<? extends JavaFileObject> compilationUnits = getCompilationUnits(className, source);
        return compile(compilationUnits, ".tmp.compiler");
    }
    public static byte[] compile(File sourceFile) {
        return compile(sourceFile, ".tmp.compiler");
    }

    public static byte[] compile(File sourceFile, String outputDir) {
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(sourceFile);
        return compile(compilationUnits, outputDir);
    }

    private static void clearDirs(Path root) {
        File file = root.toFile();
        if (!file.exists()) {
            return;
        }
        if (file.isFile()) {
            file.delete();
        }
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            for (File child : childFiles) {
                clearDirs(child.toPath());
                child.delete();
            }
        }
    }

    private static File findClassFile(Path root) {
        if (!root.toFile().exists()) {
            return null;
        }
        File file = root.toFile();
        if (file.isFile()) {
            if (file.toString().endsWith(".class")) {
                return file;
            }
        }
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            for (File child : childFiles) {
                File classFile = findClassFile(child.toPath());
                if (classFile != null) {
                    return classFile;
                }
            }
        }
        return null;
    }

    public static class JavaStringObject extends SimpleJavaFileObject {
        private final String source;

        protected JavaStringObject(String name, String source) {
            super(URI.create("string:///" + name.replaceAll("\\.", "/") + Kind.SOURCE.extension), Kind.SOURCE);
            this.source = source;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return source;
        }
    }

    public static Iterable<? extends JavaFileObject> getCompilationUnits(String className, String source) {
        JavaStringObject stringObject = new JavaStringObject(className, source);
        return Arrays.asList(stringObject);
    }

    public static byte[] compile(Iterable<? extends JavaFileObject> compilationUnits, String outputDir) {
        try {
            Path outPath = Paths.get(outputDir);
            if (!outPath.toFile().exists()) {
                outPath.toFile().mkdirs();
            } else {
                clearDirs(outPath);
            }

            fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(outPath.toFile()));
            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null,
                    compilationUnits);
            Boolean success = task.call();
            fileManager.close();
            if (success) {
                System.out.println("compile success!");
                File classFile = findClassFile(outPath);
                byte[] classData = Files.readAllBytes(classFile.toPath());
                return classData;
            } else {
                System.out.println("compile failed!");
                for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
                    System.out.println("Message: " + diagnostic.getMessage(null));
                    // System.console()
                    //         .printf("Code: %s%n" + "Kind: %s%n" + "Position: %s%n" + "Start Position: %s%n"
                    //                 + "End Position: %s%n" + "Source: %s%n" + "Message:   %s%n", diagnostic.getCode(),
                    //                 diagnostic.getKind(), diagnostic.getPosition(), diagnostic.getStartPosition(),
                    //                 diagnostic.getEndPosition(), diagnostic.getSource(), diagnostic.getMessage(null));
                }
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}