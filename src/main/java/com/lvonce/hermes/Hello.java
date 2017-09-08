package com.lvonce.hermes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hello {
    // private static final Logger logger = LoggerFactory.getLogger(Hello.class);
    private static int count = 0;
    public synchronized int test() {
        // logger.info("test2()");
        count ++;
        return 36;
    }
}