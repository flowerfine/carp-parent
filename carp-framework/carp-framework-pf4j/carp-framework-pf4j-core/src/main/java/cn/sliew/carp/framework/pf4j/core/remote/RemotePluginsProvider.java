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
package cn.sliew.carp.framework.pf4j.core.remote;

import cn.sliew.carp.framework.pf4j.core.remote.extension.RemoteExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides remote plugins based on selected criteria - currently by plugin ID or remote extension
 * type.
 */
public class RemotePluginsProvider {

    private final RemotePluginsCache remotePluginsCache;

    public RemotePluginsProvider(RemotePluginsCache remotePluginsCache) {
        this.remotePluginsCache = remotePluginsCache;
    }

    /**
     * Get a {@link RemotePlugin} by its {@code pluginId}.
     */
    public RemotePlugin getById(String pluginId) {
        RemotePlugin plugin = remotePluginsCache.get(pluginId);

        if (plugin != null) {
            return plugin;
        } else {
            throw new RemotePluginNotFoundException(pluginId);
        }
    }

    /**
     * Get a list of {@link RemotePlugin} that have extensions implementing the given {@code type}.
     */
    public List<RemotePlugin> getByExtensionType(String type) {
        List<RemotePlugin> plugins = new ArrayList<>();

        remotePluginsCache.getAll().forEach((pluginId, plugin) -> {
            if (plugin.getRemoteExtensions().stream().anyMatch(ext -> ext.getType().equals(type))) {
                plugins.add(plugin);
            }
        });

        return plugins;
    }

    /**
     * Return remote extensions by type.
     */
    public List<RemoteExtension> getExtensionsByType(String type) {
        List<RemoteExtension> extensions = new ArrayList<>();

        remotePluginsCache.getAll().forEach((pluginId, plugin) -> {
            extensions.addAll(
                    plugin.getRemoteExtensions().stream()
                            .filter(ext -> ext.getType().equals(type))
                            .collect(Collectors.toList())
            );
        });

        return extensions;
    }

    /**
     * Thrown when a remote plugin is not found.
     */
    public static class RemotePluginNotFoundException extends RuntimeException {
        public RemotePluginNotFoundException(String pluginId) {
            super(String.format("Remote plugin '%s' not found.", pluginId));
        }
    }
}
