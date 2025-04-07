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

import cn.sliew.carp.framework.pf4j.core.events.RemotePluginCacheRefresh;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Map;

/**
 * Provides a read/write remote plugin cache used by either the {@link RemotePluginConfigChangedListener}
 * to add plugins based on configuration changes or the {@link RemotePluginsProvider} to retrieve plugins
 * for use in services.
 */
@Slf4j
public class RemotePluginsCache {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final Cache<String, RemotePlugin> cache;

    public RemotePluginsCache(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.cache = CacheBuilder.newBuilder().build();
    }

    /**
     * Get the remote plugin by plugin ID, returns null if the plugin is not found.
     */
    RemotePlugin get(String pluginId) {
        return cache.getIfPresent(pluginId);
    }

    /**
     * Return all plugins in the remote plugin cache.
     */
    Map<String, RemotePlugin> getAll() {
        return cache.asMap();
    }

    /**
     * Put a plugin in the cache, emits a {@link RemotePluginCacheRefresh} event.
     */
    void put(RemotePlugin remotePlugin) {
        cache.put(remotePlugin.getId(), remotePlugin);
        applicationEventPublisher.publishEvent(new RemotePluginCacheRefresh(this, remotePlugin.getId()));
        log.debug("Put remote plugin '{}'.", remotePlugin.getId());
    }

    /**
     * Remove the specified plugin from the cache, emits a {@link RemotePluginCacheRefresh} event.
     */
    void remove(String pluginId) {
        cache.invalidate(pluginId);
        applicationEventPublisher.publishEvent(new RemotePluginCacheRefresh(this, pluginId));
        log.debug("Removed remote plugin '{}'.", pluginId);
    }
}
