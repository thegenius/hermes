package com.lvonce.hermes.compilers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompilerOfJavaNative implements Compiler {
    
    private static final Logger logger = LoggerFactory.getLogger(CompilerOfKotlin.class);

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public String getSourceFileSuffix() {
        return ".java";
    }

    @Override
    public String getCompilerCommand() {
        return "javac";
    }
}