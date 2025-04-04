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
package cn.sliew.carp.framework.pf4j.core.pf4j;

import cn.hutool.core.util.ArrayUtil;
import cn.sliew.carp.framework.pf4j.api.ExtensionConfiguration;
import cn.sliew.carp.framework.pf4j.api.PluginConfiguration;
import cn.sliew.carp.framework.pf4j.api.PluginSdks;
import cn.sliew.carp.framework.pf4j.core.config.ConfigFactory;
import cn.sliew.carp.framework.pf4j.core.pf4j.finders.UnsafePluginDescriptor;
import cn.sliew.carp.framework.pf4j.core.sdks.PluginSdksImpl;
import cn.sliew.carp.framework.pf4j.core.sdks.SdkFactory;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginWrapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public enum Util {
    ;

    public static boolean isUnsafe(PluginWrapper pluginWrapper) {
        PluginDescriptor descriptor = pluginWrapper.getDescriptor();
        if (descriptor instanceof UnsafePluginDescriptor unsafePluginDescriptor) {
            return unsafePluginDescriptor.getUnsafe();
        } else {
            return false;
        }
    }

    public static boolean validate(PluginDescriptor descriptor) {
        return CanonicalPluginId.isValid(descriptor.getPluginId());
    }

    public static Object createWithConstructor(
            Class<?> clazz,
            ClassKind classKind,
            List<SdkFactory> pluginSdkFactories,
            ConfigFactory configFactory,
            PluginWrapper pluginWrapper) {

        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        if (ArrayUtil.isEmpty(constructors)) {
            log.debug("No injectable constructor found for '{}': Using no-args constructor", clazz.getCanonicalName());
            return newInstanceSafely(clazz, classKind);
        }

        if (constructors.length > 1) {
            throw new RuntimeException(
                    "More than one injectable constructor found for '" +
                            clazz.getCanonicalName() +
                            "': Cannot initialize extension"
            );
        }

        Constructor<?> ctor = constructors[0];
        List<Object> args = Arrays.stream(ctor.getParameterTypes())
                .map(paramType -> {
                    if (paramType == PluginWrapper.class && classKind == ClassKind.PLUGIN) {
                        return pluginWrapper;
                    } else if(paramType == PluginSdks.class) {
                        List<?> sdkServices = pluginSdkFactories.stream().map(sdkFactory -> sdkFactory.create(clazz, pluginWrapper)).toList();
                        return new PluginSdksImpl(sdkServices);
                    } else if (paramType.isAnnotationPresent(PluginConfiguration.class) && classKind == ClassKind.EXTENSION) {
                        return configFactory.createExtensionConfig(
                                paramType,
                                pluginWrapper != null ? pluginWrapper.getDescriptor().getPluginId() : null,
                                paramType.getAnnotation(PluginConfiguration.class).value()
                        );
                    } else if (paramType.isAnnotationPresent(PluginConfiguration.class) && classKind == ClassKind.PLUGIN) {
                        return configFactory.createPluginConfig(
                                paramType,
                                pluginWrapper != null ? pluginWrapper.getDescriptor().getPluginId() : null,
                                paramType.getAnnotation(PluginConfiguration.class).value()
                        );
                    } else if (paramType.isAnnotationPresent(ExtensionConfiguration.class)) {
                        return configFactory.createExtensionConfig(
                                paramType,
                                pluginWrapper != null ? pluginWrapper.getDescriptor().getPluginId() : null,
                                paramType.getAnnotation(ExtensionConfiguration.class).value()
                        );
                    } else {
                        throw new RuntimeException(
                                "'" + clazz.getCanonicalName() + "' has unsupported " +
                                        "constructor argument type '" + paramType.getCanonicalName() + "'.  Expected argument classes " +
                                        "should be annotated with @PluginConfiguration or implement PluginSdks."
                        );
                    }
                })
                .collect(Collectors.toList());

        return newInstanceSafely(ctor, classKind, args);
    }

    private static Object newInstanceSafely(Class<?> clazz, ClassKind kind) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException e) {
            throw new RuntimeException("Failed to instantiate " +
                    kind + " '" +
                    (clazz.getDeclaringClass() != null ?
                            clazz.getDeclaringClass().getSimpleName() :
                            clazz.getSimpleName()) + "'",
                    e);
        }
    }

    private static Object newInstanceSafely(Constructor<?> ctor, ClassKind kind, List<Object> args) {
        try {
            return ctor.newInstance(args.toArray());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException e) {
            throw new RuntimeException("Failed to instantiate " +
                    kind + " '" +
                    ctor.getDeclaringClass().getSimpleName() + "'",
                    e);
        }
    }

    public static enum ClassKind {
        PLUGIN, EXTENSION;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }


}
