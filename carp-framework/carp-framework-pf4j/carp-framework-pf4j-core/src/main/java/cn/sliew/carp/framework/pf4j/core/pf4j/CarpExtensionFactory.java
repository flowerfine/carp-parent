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
import org.pf4j.ExtensionFactory;
import org.pf4j.PluginWrapper;

import java.util.List;

/**
 * Creates extensions, injecting in dependencies as needed.
 */
public class CarpExtensionFactory implements ExtensionFactory {

    private CarpPluginManager pluginManager;
    private ConfigFactory configFactory;
    private List<SdkFactory> pluginSdkFactories;

    /**
     * Constructor.
     *
     * @param pluginManager      the plugin manager
     * @param configFactory      the config factory
     * @param pluginSdkFactories the plugin sdk factories
     */
    public CarpExtensionFactory(
            CarpPluginManager pluginManager,
            ConfigFactory configFactory,
            List<SdkFactory> pluginSdkFactories) {
        this.pluginManager = pluginManager;
        this.configFactory = configFactory;
        this.pluginSdkFactories = pluginSdkFactories;
    }

    @Override
    public <T> T create(Class<T> extensionClass) {
        PluginWrapper pluginWrapper = pluginManager.whichPlugin(extensionClass);
        return (T) Util.createWithConstructor(
                extensionClass,
                Util.ClassKind.EXTENSION,
                pluginSdkFactories,
                configFactory, pluginWrapper);
    }
}
