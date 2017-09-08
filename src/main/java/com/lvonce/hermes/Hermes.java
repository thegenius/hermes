package com.lvonce.hermes;

public class Hermes {

    public static Object create(String className, Object... args) {
        return HermesClassLoader.create(className, args);
    }

    public static Object invoke(Object target, String methodName, Object... args) {
        return ReflectUtils.invoke(target, methodName, args);
    }

    public static void addWatchDir(String dir) {
        HermesClassLoader.addWatcher(dir);
    }

    public static void delWatchDir(String dir) {
        HermesClassLoader.delWatcher(dir);
    }

}