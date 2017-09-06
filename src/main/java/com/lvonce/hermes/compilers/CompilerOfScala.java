
package com.lvonce.hermes.compilers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompilerOfScala implements Compiler {
    
    private static final Logger logger = LoggerFactory.getLogger(CompilerOfScala.class);

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public String getSourceFileSuffix() {
        return ".scala";
    }

    @Override
    public String getCompilerCommand() {
        return "scalac";
    }
}