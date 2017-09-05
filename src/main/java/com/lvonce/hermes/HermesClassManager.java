package com.lvonce.hermes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.WeakHashMap;

import java.lang.SecurityException;
import java.lang.NoSuchMethodException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class HermesClassManager {
    private static final Logger logger = LoggerFactory.getLogger(HermesClassManager.class);
    private final Map<Object, Object[]> objectRefs;
    private final Constructor<?> constructorWithNoArgs;
    private final Constructor<?> constructorWithObject;
    private final Method getTargetMethod;
    private final Method setTargetMethod;
    private final Class<?> proxyClass;
    private Class<?> implementClass;
    private Map<Integer, Constructor<?>[]> implementConstructors;

    public HermesClassManager() {
        this.proxyClass = null;
        this.objectRefs = null;
        this.constructorWithNoArgs = null;
        this.constructorWithObject = null;
        this.setTargetMethod = null;
        this.getTargetMethod = null;
    }

    public HermesClassManager(Class<?> proxyClass) {
        try {
            Method getTargetMethod = proxyClass.getDeclaredMethod("__getReloadTarget__");
            Method setTargetMethod = proxyClass.getDeclaredMethod("__setReloadTarget__",
                    new Class<?>[] { Object.class });
        } catch (NoSuchMethodException e) {
            proxyClass = null;
        }
        if (proxyClass == null) {
            this.proxyClass = null;
            this.objectRefs = null;
            this.constructorWithNoArgs = null;
            this.constructorWithObject = null;
            this.setTargetMethod = null;
            this.getTargetMethod = null;
        } else {
            Constructor<?> constructorWithNoArgs = null;
            Constructor<?> constructorWithObject = null;
            Method getTargetMethod = null;
            Method setTargetMethod = null;
            WeakHashMap<Object, Object[]> objectRefs = new WeakHashMap<>();
            try {
                constructorWithNoArgs = proxyClass.getDeclaredConstructor();
            } catch (NoSuchMethodException | SecurityException e) {
            }
            try {
                constructorWithObject = proxyClass.getDeclaredConstructor(new Class<?>[] { Object.class });
            } catch (NoSuchMethodException | SecurityException e) {
            }
            if (constructorWithNoArgs != null || constructorWithObject != null) {
                try {
                    getTargetMethod = proxyClass.getDeclaredMethod("__getReloadTarget__");
                    setTargetMethod = proxyClass.getDeclaredMethod("__setReloadTarget__",
                            new Class<?>[] { Object.class });
                } catch (NoSuchMethodException | SecurityException e) {
                    constructorWithNoArgs = null;
                    constructorWithObject = null;
                    getTargetMethod = null;
                    setTargetMethod = null;
                    objectRefs = null;
                    proxyClass = null;
                }
            } else {
                logger.debug("HermesClassManager(): no proxy constructor");
            }

            this.proxyClass = proxyClass;
            this.objectRefs = objectRefs;
            this.constructorWithNoArgs = constructorWithNoArgs;
            this.constructorWithObject = constructorWithObject;
            this.getTargetMethod = getTargetMethod;
            this.setTargetMethod = setTargetMethod;
        }
    }

    public boolean isReloadable() {
        return this.proxyClass != null;
    }

    public Class<?> getProxyClass() {
        return this.proxyClass;
    }

    public Class<?> getImplementClass() {
        return this.implementClass;
    }

    public boolean matchType(Class<?> dstType, Class<?>srcType) {
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

    public boolean matchAssignableTypes(Class<?>[] dstTypes, Class<?>[] srcTypes) {
        if (dstTypes.length != srcTypes.length) {
            return false;
        }
        for (int i = 0; i < dstTypes.length; ++i) {
            if (!matchType(dstTypes[i],srcTypes[i])) {
                return false;
            }
        }
        return true;
    }

    public Constructor<?> matchConstructor(Object... args) {
        if (this.implementConstructors == null) {
            return null;
        }
        Constructor<?>[] constructors = this.implementConstructors.get(args.length);
        if (constructors == null) {
            return null;
        }
        if (args.length == 0) {
            return constructors[0];
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
        return null;
    }

    public Object createInstance(Object... args) {
        logger.debug("createInstance({})", args);
        try {
            if (this.implementClass == null) {
                logger.warn("try createInstance(), but the implement class is null");
                return null;
            }
            Constructor<?> constructor = this.matchConstructor(args);
            if (constructor == null) {
                logger.debug("try createInstance(), but no suitable constructor");
                return null;
            }
            Object target = constructor.newInstance(args);
            if (this.constructorWithObject != null) {
                return constructorWithObject.newInstance(target);
            } else if (this.constructorWithNoArgs != null) {
                Object proxy = this.constructorWithNoArgs.newInstance();
                this.setTargetMethod.invoke(proxy, target);
                return proxy;
            } else {
                logger.debug("createInstance({}) return target", args);
                return target;
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
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

    public Map<Integer, Constructor<?>[]> collectConstructors(Class<?> classType) {
        Constructor<?>[] constructors = classType.getDeclaredConstructors();
        LinkedHashMap<Integer, ArrayList<Constructor<?>>> constructorMap = new LinkedHashMap<>();
        for (Constructor<?> constructor : constructors) {
            int paramCount = constructor.getParameterCount();
            ArrayList<Constructor<?>> constructorList = constructorMap.get(paramCount);
            if (constructorList == null) {
                constructorList = new ArrayList<Constructor<?>>();
                constructorMap.put(paramCount, constructorList);
            }
            constructorList.add(constructor);
        }

        LinkedHashMap<Integer, Constructor<?>[]> map = new LinkedHashMap<>();
        for (Map.Entry<Integer, ArrayList<Constructor<?>>> entry : constructorMap.entrySet()) {
            ArrayList<Constructor<?>> list = entry.getValue();
            Constructor<?>[] array = list.toArray(new Constructor<?>[list.size()]);
            map.put(entry.getKey(), array);
        }
        return map;
    }

    public void update(Class<?> newClass) {
        logger.debug("update({})", newClass.getName());
        this.implementClass = newClass;
        this.implementConstructors = collectConstructors(newClass);
        if (this.proxyClass != null) {
            Iterator<Map.Entry<Object, Object[]>> it = this.objectRefs.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Object, Object[]> entry = it.next();
                Object proxy = entry.getKey();
                try {
                    Object oldObject = this.getTargetMethod.invoke(proxy);
                    Object newObject = createInstance(entry.getValue());
                    mergeObject(newObject, oldObject);
                    setTargetMethod.invoke(proxy, newObject);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
