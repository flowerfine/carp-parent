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

import cn.sliew.carp.framework.pf4j.core.config.ConfigFactory;
import cn.sliew.carp.framework.pf4j.core.sdks.SdkFactory;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.Plugin;
import org.pf4j.PluginFactory;
import org.pf4j.PluginWrapper;

import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Enables Plugin classes to be injected with SdkFactory and extension configuration.
 * <p>
 * TODO(rz): Add `@PluginConfiguration` annot?
 */
@Slf4j
public class CarpPluginFactory implements PluginFactory {

    private final ConfigFactory configFactory;
    private final List<SdkFactory> pluginSdkFactories;

    /**
     * @param configFactory      the config factory
     * @param pluginSdkFactories the sdk factories
     */
    public CarpPluginFactory(
            ConfigFactory configFactory,
            List<SdkFactory> pluginSdkFactories) {
        this.configFactory = configFactory;
        this.pluginSdkFactories = pluginSdkFactories;
    }

    @Override
    public Plugin create(PluginWrapper pluginWrapper) {
        String pluginClassName = pluginWrapper.getDescriptor().getPluginClass();
        log.debug("Create instance for plugin '{}'", pluginClassName);

        Class<?> pluginClass;
        try {
            pluginClass = pluginWrapper.getPluginClassLoader().loadClass(pluginClassName);
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage(), e);
            return null;
        }

        if (!isValidPlugin(pluginClass)) {
            log.error("The plugin class '{}' is not valid", pluginClass.getCanonicalName());
            return null;
        }

        return (Plugin) Util.createWithConstructor(
                pluginClass,
                Util.ClassKind.PLUGIN,
                pluginSdkFactories,
                configFactory,
                pluginWrapper
        );
    }

    /**
     * Perform checks on the loaded class to ensure that it is a valid implementation of a plugin.
     */
    private boolean isValidPlugin(Class<?> clazz) {
        int modifiers = clazz.getModifiers();
        if (Modifier.isAbstract(modifiers) ||
                Modifier.isInterface(modifiers) ||
                !Plugin.class.isAssignableFrom(clazz)) {
            return false;
        }
        return true;
    }
}
