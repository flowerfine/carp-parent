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
package cn.sliew.carp.framework.pf4j.core.config;

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

/**
 * Provides an extension with a typed configuration class object.
 */
public class ConfigFactory {

    private ConfigResolver configResolver;

    public ConfigFactory(ConfigResolver configResolver) {
        this.configResolver = configResolver;
    }

    /**
     * Create the extension configuration given the {@param configClass}, {@param extensionConfigId} and {@param pluginId}.
     */
    public Object createExtensionConfig(Class<?> configClass, String pluginId, String extensionConfigId) {
        ConfigCoordinates coordinates;
        if (StringUtils.isEmpty(pluginId)) {
            coordinates = new ConfigCoordinates.SystemExtensionConfigCoordinates(extensionConfigId);
        } else {
            coordinates = new ConfigCoordinates.ExtensionConfigCoordinates(pluginId, extensionConfigId);
        }
        return resolveConfiguration(coordinates, configClass);
    }

    /**
     * Create the plugin configuration given the {@param configClass} and {@param pluginId}.
     */
    public Object createPluginConfig(Class<?> configClass, String pluginId, String pluginConfigId) {
        if (StringUtils.isNotBlank(pluginId)) {
            return resolveConfiguration(
                    new ConfigCoordinates.PluginConfigCoordinates(pluginId, pluginConfigId),
                    configClass
            );
        }
        return null;
    }

    private Object resolveConfiguration(ConfigCoordinates coordinates, Class<?> configClass) {
        return Optional.ofNullable(configClass)
                .map(clazz -> configResolver.resolve(coordinates, clazz))
                .orElseThrow(() -> new RuntimeException(
                        String.format("Could not resolve configuration '%s' with " +
                                "coordinates '%s'", configClass.getSimpleName(), coordinates.toPointer())
                ));
    }
}
