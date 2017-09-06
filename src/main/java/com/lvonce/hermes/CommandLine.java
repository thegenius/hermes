package com.lvonce.hermes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandLine {
    private static final Logger logger = LoggerFactory.getLogger(CommandLine.class);

    public static int exec(String cmd) {
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            logger.info("{}", cmd);
            int exitValue = process.waitFor();
            logger.info("{} : return {} ", cmd, exitValue);
            return exitValue;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}