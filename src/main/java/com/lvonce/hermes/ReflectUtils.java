package com.lvonce.hermes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.IllegalAccessError;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

public class ReflectUtils {
    private static final Logger logger = LoggerFactory.getLogger(ReflectUtils.class);

    public static boolean matchType(Class<?> dstType, Class<?> srcType) {
        if (dstType.isPrimitive() && srcType.isPrimitive()) {
            return dstType.isAssignableFrom(srcType);
        }
        if ((!dstType.isPrimitive()) && (!srcType.isPrimitive())) {
            return dstType.isAssignableFrom(srcType);
        }
        if (!dstType.isPrimitive()) {
            Class<?> tmpType = dstType;
            dstType = srcType;
            srcType = tmpType;
        }
        if (dstType == byte.class) {
            return srcType == Byte.class;
        }
        if (dstType == short.class) {
            return srcType == Short.class;
        }
        if (dstType == int.class) {
            return srcType == Integer.class;
        }
        if (dstType == long.class) {
            return srcType == Long.class;
        }
        if (dstType == float.class) {
            return srcType == Float.class;
        }
        if (dstType == double.class) {
            return srcType == Double.class;
        }
        if (dstType == boolean.class) {
            return srcType == Boolean.class;
        }
        if (dstType == char.class) {
            return srcType == Character.class;
        }
        return false;
    }

    public static boolean matchAssignableTypes(Class<?>[] dstTypes, Class<?>[] srcTypes) {
        if (dstTypes.length != srcTypes.length) {
            return false;
        }
        for (int i = 0; i < dstTypes.length; ++i) {
            if (!matchType(dstTypes[i], srcTypes[i])) {
                return false;
            }
        }
        return true;
    }

    public static Constructor<?> matchConstructor(Class<?> classType, Object... args) throws NoSuchMethodException {
        Constructor<?>[] constructors = classType.getDeclaredConstructors();
        try {
            if (args.length == 0) {
                return classType.getDeclaredConstructor();
            }
            Class<?>[] paramTypes = new Class[args.length];
            for (int i = 0; i < args.length; ++i) {
                paramTypes[i] = args[i].getClass();
            }
            for (Constructor<?> constructor : constructors) {
                Class<?>[] constructorParamTypes = constructor.getParameterTypes();
                if (matchAssignableTypes(constructorParamTypes, paramTypes)) {
                    return constructor;
                }
            }
            throw new NoSuchMethodException(args.toString());
        } catch (SecurityException e) {
            e.printStackTrace();
            throw new NoSuchMethodException(args.toString());
        }
    }

    public static Method matchMethod(Class<?> classType, String methodName, Object... args) throws NoSuchMethodException {
        Method[] methods = classType.getDeclaredMethods();
        try {
            if (args.length == 0) { // make the common case faster
                return classType.getDeclaredMethod(methodName);
            }
            Class<?>[] paramTypes = new Class[args.length];
            for (int i = 0; i < args.length; ++i) {
                paramTypes[i] = args[i].getClass();
            }
            for (Method method : methods) {
                if (method.getName() != methodName) {
                    continue;
                }
                Class<?>[] methodParamTypes = method.getParameterTypes();
                if (matchAssignableTypes(methodParamTypes, paramTypes)) {
                    return method;
                }
            }
            throw new NoSuchMethodException(args.toString());
        } catch (SecurityException e) {
            e.printStackTrace();
            throw new NoSuchMethodException(args.toString());
        }
    }

    public static Object createInstance(Class<?> classType, Object... args) {
        try {
            logger.debug("createInstance({})", args);
            Constructor<?> constructor = matchConstructor(classType, args);
            if (constructor == null) {
                logger.debug("try createInstance(), but no suitable constructor");
                return null;
            }
            return constructor.newInstance(args);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void mergeObject(Object dst, Object src) {
        Field[] fields = dst.getClass().getDeclaredFields();
        Class<?> srcClass = src.getClass();
        for (Field dstField : fields) {
            try {
                String name = dstField.getName();
                Field srcField = srcClass.getDeclaredField(name);
                boolean srcAccessible = srcField.isAccessible();
                boolean dstAccessible = dstField.isAccessible();
                srcField.setAccessible(true);
                dstField.setAccessible(true);
                dstField.set(dst, srcField.get(src));
                srcField.setAccessible(srcAccessible);
                dstField.setAccessible(dstAccessible);
            } catch (NoSuchFieldException | IllegalAccessException e) {
            }
        }
    }

    public static Class<?> getClass(byte[] classData) {
        try {
            Class<?> classType = new ClassLoader() {
                public Class<?> defineClass(byte[] bytes) {
                    return super.defineClass(null, bytes, 0, bytes.length);
                }
            }.defineClass(classData);
            return classType;
        } catch (Exception | Error e) {
            logger.debug("getClass Error -> {}", e.getMessage());
            return null;
        }
    }

    public static Object invoke(Object target, String methodName, Object... args)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IllegalArgumentException {
        Method method = matchMethod(target.getClass(), methodName, args);
        return method.invoke(target, args);
    }
}