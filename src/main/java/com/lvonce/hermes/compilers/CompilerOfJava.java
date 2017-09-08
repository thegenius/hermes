package com.lvonce.hermes.compilers;

import java.net.URI;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.tools.Diagnostic;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.DiagnosticCollector;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;

public class CompilerOfJava implements Compiler {
    private static final Logger logger = LoggerFactory.getLogger(CompilerOfJava.class);
    private static final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    private static final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
    private static final StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public String getSourceFileSuffix() {
        return ".java";
    }

    @Override
    public Iterable<?> getCompilationUnits(String className, String source) {
        JavaStringObject stringObject = new JavaStringObject(className, source);
        return Arrays.asList(stringObject);
    }

    @Override
    public Iterable<?> getCompilationUnits(Iterable<File> sourceFiles) {
        logger.debug("getCompilationUnits({})", sourceFiles);
        return fileManager.getJavaFileObjectsFromFiles(sourceFiles);
    }

    @Override
    public boolean execute(Iterable<?> compilationList) {
        logger.debug("execute({})", compilationList);
        try {
            Iterable<? extends JavaFileObject> compilationUnits = (Iterable<? extends JavaFileObject>) compilationList;
            Path outPath = Paths.get(compiledClassDir());
            fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(outPath.toFile()));
            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null,
                    compilationUnits);
            Boolean success = task.call();
            fileManager.close();
            if (success) {
                logger.info("compile success!");
            } else {
                logger.info("compile failed!");
                for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
                    if (diagnostic != null) {
                        logger.debug(
                                "Code: {}%n" + "Kind: {}%n" + "Position: {}%n" + "Start Position: {}%n"
                                        + "End Position: {}%n" + "Source: {}%n" + "Message:   {}%n",
                                diagnostic.getCode(), diagnostic.getKind(), diagnostic.getPosition(),
                                diagnostic.getStartPosition(), diagnostic.getEndPosition(), diagnostic.getSource(),
                                diagnostic.getMessage(null));
                    }
                }
            }
            return success;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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
}