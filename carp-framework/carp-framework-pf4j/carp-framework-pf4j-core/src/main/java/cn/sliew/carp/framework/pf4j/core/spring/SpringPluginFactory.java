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
package cn.sliew.carp.framework.pf4j.core.spring;

import cn.sliew.carp.framework.pf4j.api.spring.PrivilegedSpringPlugin;
import cn.sliew.carp.framework.pf4j.core.config.ConfigFactory;
import cn.sliew.carp.framework.pf4j.core.pf4j.Util;
import cn.sliew.carp.framework.pf4j.core.sdks.SdkFactory;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.Plugin;
import org.pf4j.PluginFactory;
import org.pf4j.PluginWrapper;
import org.springframework.context.support.GenericApplicationContext;

import java.util.List;

/**
 * Creates a {@link Plugin}.
 * <p>
 * If the plugin is not a {@link PrivilegedSpringPlugin}, a {@link PluginContainer} will be created instead which
 * initializes and wires up the plugin's Spring ApplicationContext. This {@link PluginContainer} is an
 * implementation detail of the framework itself and hides the fact that Spring is used for plugin
 * configuration, component discovery and creation, and as well as extension promotion to the service.
 */
@Slf4j
public class SpringPluginFactory implements PluginFactory {

    private final List<SdkFactory> sdkFactories;
    private final ConfigFactory configFactory;
    private final GenericApplicationContext serviceApplicationContext;

    public SpringPluginFactory(
            List<SdkFactory> sdkFactories,
            ConfigFactory configFactory,
            GenericApplicationContext serviceApplicationContext) {
        this.sdkFactories = sdkFactories;
        this.configFactory = configFactory;
        this.serviceApplicationContext = serviceApplicationContext;
    }

    @Override
    public Plugin create(PluginWrapper pluginWrapper) {
        String pluginClassName = pluginWrapper.getDescriptor().getPluginClass();
        log.debug("Creating plugin '{}'", pluginClassName);

        Class<?> pluginClass;
        try {
            pluginClass = pluginWrapper.getPluginClassLoader().loadClass(pluginClassName);
        } catch (ClassNotFoundException e) {
            log.error("Failed to load plugin class for '{}'", pluginWrapper.getPluginId(), e);
            return null;
        }

        Plugin actualPlugin = (Plugin) Util.createWithConstructor(
                pluginClass,
                Util.ClassKind.PLUGIN,
                sdkFactories,
                configFactory,
                pluginWrapper
        );

        // PrivilegedSpringPlugin does _kind of_ the same thing as PluginContainer, but the two are incompatible.
        // PluginContainer attempts to offer some of the convenience that PrivilegedSpringPlugin does, but without using
        // Spring itself as an API contract.
        if (!(actualPlugin instanceof PrivilegedSpringPlugin)) {
            return new PluginContainer(actualPlugin, serviceApplicationContext);
        } else {
            return actualPlugin;
        }
    }
}
