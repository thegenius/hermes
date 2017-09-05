package com.lvonce.hermes;

public class App {

    public static void main(String[] args) throws Exception {
        Thread.currentThread().setContextClassLoader(HermesClassLoader.getInstance());
        Thread t = new Thread() {
            public void run() {
                Thread.currentThread().setContextClassLoader(HermesClassLoader.getInstance());
                while (true) {
                    try {
                        Hello hello = new Hello();
                        hello.test();
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }
                }
            }
        };
        t.run();
        t.join();
    }
}