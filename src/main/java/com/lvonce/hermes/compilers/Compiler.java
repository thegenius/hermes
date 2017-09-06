package com.lvonce.hermes.compilers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.LinkedHashMap;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.net.URISyntaxException;
import java.net.URL;

import com.lvonce.hermes.CommandLine;
import com.lvonce.hermes.OSValidator;
import com.lvonce.hermes.ReflectUtils;
import com.lvonce.hermes.RuntimeFileUtils;

public interface Compiler {

    public static final Logger logger = LoggerFactory.getLogger(Compiler.class);
    public static final Map<String, Compiler> compilers = collectCompilers();

    default Logger getLogger() {
        return logger;
    }

    default String compiledClassDir() {
        return ".tmp.compiler";
    }

    default String getCompilerCommand() {
        return null;
    }

    String getSourceFileSuffix();

    default boolean supported() {
        return true;
    }

    public static Class<?>[] compileFile(File sourceFile) {
        return compileFile(Arrays.asList(sourceFile));
    }
    public static Class<?>[] compileFile(Iterable<File> sourceFiles) {
        ArrayList<Class<?>> classes = new ArrayList<>();
        for (File sourceFile : sourceFiles) {
            String fileName = sourceFile.getName();
            int i = fileName.lastIndexOf('.');
            if (i > 0) {
                String suffix = fileName.substring(i);
                logger.debug("suffix -> {}", suffix);
                Compiler compiler = compilers.get(suffix);
                if (compiler != null) {
                    classes.addAll(Arrays.asList(compiler.compile(sourceFile)));
                }
            }
        }
        return classes.toArray(new Class<?>[classes.size()]);
    }

    public static Map<String, Compiler> collectCompilers() {
        Class<?>[] compilerClasses = RuntimeFileUtils.collectChildrenClasses(Compiler.class);
        LinkedHashMap<String, Compiler> compilersMap = new LinkedHashMap<>();
        for (Class<?> compilerClass : compilerClasses) {
            Compiler compiler = (Compiler) ReflectUtils.createInstance(compilerClass);
            compilersMap.put(compiler.getSourceFileSuffix(), compiler);
        }
        return compilersMap;
    }

    default Class<?>[] compile(String fileName, String source) {
        clearClassDir();
        boolean success = execute(getCompilationUnits(fileName, source));
        if (success) {
            return getCompiledClasses();
        } else {
            return new Class<?>[0];
        }
    }

    /**
     * One source file may be compiled to more than one class
     */
    default Class<?>[] compile(File sourceFile) {
        return compile(Arrays.asList(sourceFile));
    }

    default Class<?>[] compile(Iterable<File> sourceFiles) {
        clearClassDir();
        if (getCompilerCommand() != null) {
            try {
                String cmd = String.format("%s %s -classpath %s -d %s", getCompilerCommand(),
                        getFileNameList(sourceFiles), OSValidator.getSystemClassPath("target/classes"),
                        compiledClassDir());
                int retValue = CommandLine.exec(cmd);
                if (retValue == 0) {
                    return getCompiledClasses();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            boolean success = execute(getCompilationUnits(sourceFiles));
            if (success) {
                return getCompiledClasses();
            }
        }
        return new Class<?>[0];
    }

    default boolean execute(Iterable<?> compilationUnits) {
        return false;
    }

    default Iterable<?> getCompilationUnits(String className, String source) {
        logger.warn("compile from string to class is not supported!");
        return null;
    }

    default Iterable<?> getCompilationUnits(Iterable<File> sourceFiles) {
        return sourceFiles;
    }

    default String getFileNameList(Iterable<File> fileList) {
        StringBuilder builder = new StringBuilder();
        for (File file : fileList) {
            builder.append(" ");
            builder.append(file.getName());
            builder.append(" ");
        }
        return builder.toString();
    }

    default Class<?>[] getCompiledClasses() {
        try {
            File[] classFiles = findClassFiles();
            Class<?>[] classTypes = new Class<?>[classFiles.length];
            for (int i = 0; i < classFiles.length; ++i) {
                File classFile = classFiles[i];
                byte[] classData = Files.readAllBytes(classFile.toPath());
                classTypes[i] = ReflectUtils.getClass(classData);
            }
            return classTypes;
        } catch (IOException e) {
            e.printStackTrace();
            return new Class<?>[0];
        }
    }

    default byte[][] getCompiledClassesBytes() {
        try {
            File[] classFiles = findClassFiles();
            byte[][] classBytes = new byte[classFiles.length][];
            for (int i = 0; i < classFiles.length; ++i) {
                File classFile = classFiles[i];
                classBytes[i] = Files.readAllBytes(classFile.toPath());
            }
            return classBytes;
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0][];
        }
    }

    default void clearClassDir() {
        Path classDir = Paths.get(compiledClassDir());
        clearDirs(classDir);
        if (!classDir.toFile().exists()) {
            classDir.toFile().mkdirs();
        }
    }

    default File[] findClassFiles() {
        ArrayList<File> fileList = new ArrayList<>();
        findClassFiles(Paths.get(compiledClassDir()), fileList);
        return fileList.toArray(new File[fileList.size()]);
    }

    default void findClassFiles(Path root, ArrayList<File> fileList) {
        if (!root.toFile().exists()) {
            return;
        }
        File file = root.toFile();
        if (file.isFile()) {
            if (file.toString().endsWith(".class")) {
                fileList.add(file);
            }
        } else if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            for (File child : childFiles) {
                findClassFiles(child.toPath(), fileList);
            }
        }
    }

    default void clearDirs(Path root) {
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
}