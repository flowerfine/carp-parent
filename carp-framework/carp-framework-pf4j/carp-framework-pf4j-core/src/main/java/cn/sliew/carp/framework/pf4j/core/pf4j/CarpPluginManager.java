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
import cn.sliew.carp.framework.pf4j.core.pf4j.finders.UnsafePluginDescriptorFinder;
import cn.sliew.carp.framework.pf4j.core.pf4j.loaders.PluginRefPluginLoader;
import cn.sliew.carp.framework.pf4j.core.pf4j.loaders.UnsafeDefaultPluginLoader;
import cn.sliew.carp.framework.pf4j.core.pf4j.loaders.UnsafeDevelopmentPluginLoader;
import cn.sliew.carp.framework.pf4j.core.pf4j.loaders.UnsafeJarPluginLoader;
import cn.sliew.carp.framework.pf4j.core.pf4j.repository.PluginRefPluginRepository;
import cn.sliew.carp.framework.pf4j.core.sdks.SdkFactory;
import cn.sliew.carp.framework.spring.version.ServiceVersion;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.*;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class CarpPluginManager extends DefaultPluginManager {

    private ServiceVersion serviceVersion;
    private VersionManager versionManager;
    private PluginStatusProvider statusProvider;
    private ConfigFactory configFactory;
    private List<SdkFactory> sdkFactories;
    private PluginFactory pluginFactory;
    private ExtensionFactory springExtensionFactory;

    /**
     * The primary entry-point to the plugins system from a provider-side (services, libs, CLIs, and so-on).
     *
     * @param versionManager The {@link VersionManager} configured for Carp.
     * @param statusProvider A Spring Environment-aware plugin status provider.
     * @param pluginsRoots   The root path to search for in-process plugin artifacts.
     */
    public CarpPluginManager(
            ServiceVersion serviceVersion,
            VersionManager versionManager,
            PluginStatusProvider statusProvider,
            ConfigFactory configFactory,
            List<SdkFactory> sdkFactories,
            PluginFactory pluginFactory,
            Path... pluginsRoots) {
        super(pluginsRoots);
        this.serviceVersion = serviceVersion;
        this.versionManager = versionManager;
        this.statusProvider = statusProvider;
        this.configFactory = configFactory;
        this.sdkFactories = sdkFactories;
        this.pluginFactory = pluginFactory;
        this.springExtensionFactory = new CarpExtensionFactory(this, configFactory, sdkFactories);
    }

    @Override
    public String getSystemVersion() {
        // todo 提供实现
        return super.getSystemVersion();
    }

    void setPlugins(Collection<PluginWrapper> specifiedPlugins) {
        Map<String, PluginWrapper> pluginsMap = new HashMap<>();
        for (PluginWrapper plugin : specifiedPlugins) {
            pluginsMap.put(plugin.getPluginId(), plugin);
        }
        this.plugins = pluginsMap;
    }

    @Override
    protected PluginLoader createPluginLoader() {
        return new CompoundPluginLoader()
                .add(new PluginRefPluginLoader(this), this::isDevelopment)
                .add(new UnsafeDevelopmentPluginLoader(this), this::isDevelopment)
                .add(new UnsafeDefaultPluginLoader(this))
                .add(new UnsafeJarPluginLoader(this));
    }

    @Override
    protected PluginDescriptorFinder createPluginDescriptorFinder() {
        return new UnsafePluginDescriptorFinder(getRuntimeMode());
    }

    @Override
    protected PluginRepository createPluginRepository() {
        return new CompoundPluginRepository()
                .add(new PluginRefPluginRepository(getPluginsRoot()), this::isDevelopment)
                .add(super.createPluginRepository());
    }

    @Override
    protected PluginFactory getPluginFactory() {
        return pluginFactory;
    }

    @Override
    protected PluginFactory createPluginFactory() {
        return new PluginFactoryDelegate();
    }

    @Override
    protected ExtensionFactory createExtensionFactory() {
        return new ExtensionFactoryDelegate();
    }

    @Override
    protected PluginStatusProvider createPluginStatusProvider() {
        return new PluginStatusProviderDelegate();
    }

    @Override
    protected VersionManager createVersionManager() {
        return new VersionManagerDelegate();
    }

    private class PluginFactoryDelegate implements PluginFactory {
        @Override
        public Plugin create(PluginWrapper pluginWrapper) {
            return pluginFactory.create(pluginWrapper);
        }
    }

    private class ExtensionFactoryDelegate implements ExtensionFactory {
        @Override
        public <T> T create(Class<T> extensionClass) {
            return springExtensionFactory.create(extensionClass);
        }
    }

    private class PluginStatusProviderDelegate implements PluginStatusProvider {
        @Override
        public boolean isPluginDisabled(String pluginId) {
            return statusProvider.isPluginDisabled(pluginId);
        }

        @Override
        public void disablePlugin(String pluginId) {
            statusProvider.disablePlugin(pluginId);
        }

        @Override
        public void enablePlugin(String pluginId) {
            statusProvider.enablePlugin(pluginId);
        }
    }

    private class VersionManagerDelegate implements VersionManager {
        @Override
        public boolean checkVersionConstraint(String version, String constraint) {
            return versionManager.checkVersionConstraint(version, constraint);
        }

        @Override
        public int compareVersions(String v1, String v2) {
            return versionManager.compareVersions(v1, v2);
        }
    }
}
