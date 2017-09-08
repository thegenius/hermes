package com.lvonce.hermes;

import java.lang.reflect.InvocationTargetException;

public class Hermes {

    public static Object create(String className, Object... args) throws ClassNotFoundException {
        return HermesClassLoader.create(className, args);
    }

    public static Object invoke(Object target, String methodName, Object... args)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IllegalArgumentException {
        return ReflectUtils.invoke(target, methodName, args);
    }

    public static void addWatchDir(String dir) {
        HermesClassLoader.addWatcher(dir);
    }

    public static void delWatchDir(String dir) {
        HermesClassLoader.delWatcher(dir);
    }

}