/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.sliew.carp.framework.common.util.reflection;

import cn.sliew.carp.framework.common.util.reflection.autobox.Autoboxer;
import cn.sliew.milky.common.exception.Rethrower;
import cn.sliew.milky.common.primitives.Strings;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.stream;

public enum ReflectionUtils {
    ;

    private static final String ROOT_PACKAGE_NAME = "org/jobrunr/";
    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_TYPE_MAPPING = new HashMap<>();

    static {
        PRIMITIVE_TO_TYPE_MAPPING.put(boolean.class, Boolean.class);
        PRIMITIVE_TO_TYPE_MAPPING.put(byte.class, Byte.class);
        PRIMITIVE_TO_TYPE_MAPPING.put(short.class, Short.class);
        PRIMITIVE_TO_TYPE_MAPPING.put(char.class, Character.class);
        PRIMITIVE_TO_TYPE_MAPPING.put(int.class, Integer.class);
        PRIMITIVE_TO_TYPE_MAPPING.put(long.class, Long.class);
        PRIMITIVE_TO_TYPE_MAPPING.put(float.class, Float.class);
        PRIMITIVE_TO_TYPE_MAPPING.put(double.class, Double.class);
    }

    public static boolean classExists(String className) {
        try {
            loadClass(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static <T> Class<T> toClassFromPath(Path path) {
        final String classFile = path.toString().substring(path.toString().indexOf(ROOT_PACKAGE_NAME));
        String className = toClassNameFromFileName(classFile);
        return toClass(className);
    }

    public static String toClassNameFromFileName(String classFile) {
        return classFile.replace(".class", "").replace("/", ".");
    }

    public static <T> Class<T> toClass(String className) {
        switch (className) {
            case "boolean":
                return cast(boolean.class);
            case "byte":
                return cast(byte.class);
            case "short":
                return cast(short.class);
            case "int":
                return cast(int.class);
            case "long":
                return cast(long.class);
            case "float":
                return cast(float.class);
            case "double":
                return cast(double.class);
            case "char":
                return cast(char.class);
            case "void":
                return cast(void.class);
            default:
                try {
                    return cast(loadClass(className));
                } catch (ClassNotFoundException ex) {
                    throw new IllegalArgumentException("Class not found: " + className, ex);
                }
        }
    }

    public static Class<?> loadClass(String className) throws ClassNotFoundException {
        // why: support for quarkus:dev (see https://github.com/quarkusio/quarkus/issues/2809) and Spring Boot Live reload
        // Jackson uses this order also
        try {
            ClassLoader classLoader = currentThread().getContextClassLoader();
            if (classLoader != null) {
                return Class.forName(className, true, classLoader);
            }
        } catch (ClassNotFoundException e) {
            // support for Spring Boot Executable jar. See https://github.com/jobrunr/jobrunr/issues/81
        }
        return Class.forName(className);
    }

    public static boolean hasDefaultNoArgConstructor(String clazzName) {
        return Stream.of(toClass(clazzName).getConstructors())
                .anyMatch(c -> c.getParameterCount() == 0);
    }

    public static boolean hasDefaultNoArgConstructor(Class<?> clazz) {
        return Stream.of(clazz.getConstructors())
                .anyMatch(c -> c.getParameterCount() == 0);
    }

    public static <T> T newInstanceAndSetFieldValues(Class<T> clazz, Map<String, String> fieldValues) {
        T t = newInstance(clazz);
        fieldValues.forEach((key, value) -> findField(clazz, key)
                .ifPresent(f -> setFieldUsingAutoboxing(f, t, value)));
        return t;
    }

    public static <T> T newInstance(String className, Object... params) {
        return newInstance(toClass(className), params);
    }

    public static <T> T newInstance(Class<T> clazz, Object... params) {
        try {
            return newInstanceCE(clazz, params);
        } catch (ReflectiveOperationException e) {
            Rethrower.throwAs(e);
            return null;
        }
    }

    public static <T> T newInstanceCE(Class<T> clazz, Object... params) throws ReflectiveOperationException {
        final Constructor<T> declaredConstructor = getConstructorForArgs(clazz, Stream.of(params).map(Object::getClass).toArray(Class[]::new));
        makeAccessible(declaredConstructor);
        return declaredConstructor.newInstance(params);
    }

    public static <T> T newInstanceCE(Class<T> clazz) throws ReflectiveOperationException {
        Constructor<T> defaultConstructor = clazz.getDeclaredConstructor();
        makeAccessible(defaultConstructor);
        return defaultConstructor.newInstance();
    }

    public static <T> T newInstance(Class<T> clazz) {
        try {
            Constructor<T> defaultConstructor = clazz.getDeclaredConstructor();
            makeAccessible(defaultConstructor);
            return defaultConstructor.newInstance();
        } catch (ReflectiveOperationException e) {
            Rethrower.throwAs(e);
            return null;
        }
    }

    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        return findMethod(clazz, methodName, parameterTypes)
                .orElseThrow(() -> new RuntimeException());
    }

    public static Optional<Method> findMethod(Object object, String methodName, Class<?>... parameterTypes) {
        return findMethod(object.getClass(), new MethodFinderPredicate(methodName, parameterTypes));
    }

    public static Optional<Method> findMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        return findMethod(clazz, new MethodFinderPredicate(methodName, parameterTypes));
    }

    public static Optional<Method> findMethod(Class<?> clazz, Predicate<Method> predicate) {
        final Optional<Method> optionalMethod = stream(clazz.getDeclaredMethods())
                .filter(predicate)
                .findFirst();
        if (optionalMethod.isPresent()) {
            return optionalMethod;
        } else if (clazz.isInterface()) {
            return Stream.of(clazz.getInterfaces())
                    .map(superInterface -> findMethod(superInterface, predicate))
                    .filter(Optional::isPresent)
                    .findFirst()
                    .orElse(Optional.empty());
        } else if (!Object.class.equals(clazz.getSuperclass())) {
            return findMethod(clazz.getSuperclass(), predicate);
        } else {
            return Optional.empty();
        }
    }

    public static Field getField(Class<?> clazz, String fieldName) {
        return findField(clazz, fieldName)
                .orElseThrow(() -> new RuntimeException());
    }

    public static Optional<Field> findField(Class<?> clazz, String fieldName) {
        return findField(clazz, f -> fieldName.equals(f.getName()));
    }

    public static Optional<Field> findField(Class<?> clazz, Predicate<Field> predicate) {
        final Optional<Field> optionalField = stream(clazz.getDeclaredFields())
                .filter(predicate)
                .findFirst();
        if (optionalField.isPresent()) {
            return optionalField;
        } else if (!Object.class.equals(clazz.getSuperclass())) {
            return findField(clazz.getSuperclass(), predicate);
        } else {
            return Optional.empty();
        }
    }

    public static boolean isClassAssignableToObject(Class<?> clazz, Object object) {
        return isClassAssignable(clazz, object.getClass());
    }

    public static boolean isClassAssignable(Class<?> clazz1, Class<?> clazz2) {
        return clazz1.equals(clazz2)
                || clazz1.isAssignableFrom(clazz2)
                || (clazz1.isPrimitive() && PRIMITIVE_TO_TYPE_MAPPING.get(clazz1).equals(clazz2))
                || (clazz1.isPrimitive() && Boolean.TYPE.equals(clazz1) && Integer.class.equals(clazz2));
    }

    public static boolean objectContainsFieldOrProperty(Object object, String fieldName) {
        if (object == null) return false;
        return objectContainsField(object, fieldName) || objectContainsProperty(object, fieldName);
    }

    public static Object getValueFromFieldOrProperty(Object object, String paramName) {
        Class<?> aClass = object.getClass();
        final Optional<Field> optionalField = findField(aClass, paramName);
        if (optionalField.isPresent()) {
            return getValueFromField(optionalField.get(), object);
        }

        final Optional<Method> optionalGetMethod = findMethod(aClass, "get" + Strings.capitalize(paramName));
        if (optionalGetMethod.isPresent()) {
            return getValueFromGetMethod(optionalGetMethod.get(), object);
        }

        throw new IllegalArgumentException(String.format("Could not get value '%s' from object with class %s", paramName, object.getClass()));
    }

    public static Object getValueFromField(Field field, Object object) {
        try {
            makeAccessible(field);
            return field.get(object);
        } catch (ReflectiveOperationException willNotHappen) {
            throw new IllegalArgumentException(String.format("Could not get value '%s' from object with class %s", field.getName(), object.getClass()));
        }
    }

    public static Object getValueFromGetMethod(Method getter, Object object) {
        try {
            makeAccessible(getter);
            return getter.invoke(object);
        } catch (ReflectiveOperationException willNotHappen) {
            throw new IllegalArgumentException(String.format("Could not get value '%s' from object with class %s", getter.getName(), object.getClass()));
        }
    }

    public static void setFieldUsingAutoboxing(String fieldName, Object object, Object value) {
        if (value == null) return;

        setFieldUsingAutoboxing(getField(object.getClass(), fieldName), object, value);
    }

    public static void setFieldUsingAutoboxing(Field field, Object object, Object value) {
        try {
            if (value == null) return;

            makeAccessible(field);
            final Class<?> type = field.getType();

            Object fieldValue = autobox(value, type);
            field.set(object, fieldValue);
        } catch (ReflectiveOperationException e) {
            Rethrower.throwAs(e);
        }
    }

    public static <T> T autobox(Object value, Class<T> type) {
        return Autoboxer.autobox(value, type);
    }

    public static void makeAccessible(AccessibleObject accessibleObject) {
        accessibleObject.setAccessible(true);
    }

    private static <T> Constructor<T> getConstructorForArgs(Class<T> clazz, Class<?>[] args) throws NoSuchMethodException {
        Constructor<?>[] constructors = clazz.getConstructors();

        for (Constructor<?> constructor : constructors) {
            Class<?>[] types = constructor.getParameterTypes();
            if (types.length == args.length) {
                boolean argumentsMatch = true;
                for (int i = 0; i < args.length; i++) {
                    if (!types[i].isAssignableFrom(args[i])) {
                        argumentsMatch = false;
                        break;
                    }
                }

                if (argumentsMatch) return cast(constructor);
            }
        }

        return clazz.getConstructor(args);
    }

    /**
     * Why: less warnings and @SuppressWarnings("unchecked")
     */
    @SuppressWarnings("unchecked")
    private static <T> Class<T> cast(Class<?> aClass) {
        return (Class<T>) aClass;
    }

    /**
     * Why: less warnings and @SuppressWarnings("unchecked")
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object anObject) {
        return (T) anObject;
    }

    private static boolean objectContainsField(Object object, String fieldName) {
        return findField(object.getClass(), fieldName).isPresent();
    }

    private static boolean objectContainsProperty(Object object, String fieldName) {
        return findMethod(object.getClass(), "get" + Strings.capitalize(fieldName)).isPresent();
    }
}
