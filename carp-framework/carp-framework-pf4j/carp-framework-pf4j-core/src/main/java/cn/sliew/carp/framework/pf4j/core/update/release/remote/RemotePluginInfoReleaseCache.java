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
package cn.sliew.carp.framework.pf4j.core.update.release.remote;

import cn.sliew.carp.framework.pf4j.core.pf4j.CarpPluginManager;
import cn.sliew.carp.framework.pf4j.core.pf4j.status.SpringPluginStatusProvider;
import cn.sliew.carp.framework.pf4j.core.update.CarpPluginInfo;
import cn.sliew.carp.framework.pf4j.core.update.CarpUpdateMananger;
import cn.sliew.carp.framework.pf4j.core.update.release.PluginInfoRelease;
import cn.sliew.carp.framework.pf4j.core.update.release.provider.PluginInfoReleaseProvider;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provides a {@link PluginInfoRelease} cache of enabled plugins that contain remote extensions.
 * <p>
 * Emits {@link RemotePluginConfigChanged.Status#ENABLED}, {@link RemotePluginConfigChanged.Status#DISABLED},
 * and {@link RemotePluginConfigChanged.Status#UPDATED} events that contain the plugin ID, version, and remote
 * extensions when a corresponding change is detected in the cache (added, updated, or removed).
 */
@Slf4j
public class RemotePluginInfoReleaseCache {

    private final PluginInfoReleaseProvider pluginInfoReleaseProvider;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CarpUpdateMananger updateManager;
    private final CarpPluginManager pluginManager;
    private final SpringPluginStatusProvider springPluginStatusProvider;

    // unbounded cache because we need an in-memory reference to all enabled plugins with remote extensions
    private final Cache<String, PluginInfoRelease> pluginCache = CacheBuilder.newBuilder().build();

    public RemotePluginInfoReleaseCache(
            PluginInfoReleaseProvider pluginInfoReleaseProvider,
            ApplicationEventPublisher applicationEventPublisher,
            CarpUpdateMananger updateManager,
            CarpPluginManager pluginManager,
            SpringPluginStatusProvider springPluginStatusProvider) {
        this.pluginInfoReleaseProvider = pluginInfoReleaseProvider;
        this.applicationEventPublisher = applicationEventPublisher;
        this.updateManager = updateManager;
        this.pluginManager = pluginManager;
        this.springPluginStatusProvider = springPluginStatusProvider;
    }

    /**
     * Refresh cache process for plugin releases.
     */
    @Scheduled(
            fixedDelayString = "${spinnaker.extensibility.remote-plugins.cache-refresh-interval-ms:60000}",
            initialDelay = 0
    )
    public void refresh() {
        updateManager.refresh();

        List<CarpPluginInfo> enabledPlugins = updateManager.getPlugins().stream()
                .filter(plugin -> springPluginStatusProvider.isPluginEnabled(plugin.id))
                .map(plugin -> (CarpPluginInfo) plugin)
                .collect(Collectors.toList());

        List<PluginInfoRelease> pluginsWithRemoteExtensions = pluginInfoReleaseProvider.getReleases(enabledPlugins)
                .stream()
                .filter(release -> !release.getProps().getRemoteExtensions().isEmpty())
                .collect(Collectors.toList());

        remove(pluginsWithRemoteExtensions);
        addOrUpdate(pluginsWithRemoteExtensions);

        log.info("Cached {} remote plugin configurations.", pluginCache.size());
    }

    /**
     * Get a specific plugin ID from the cache.
     */
    public PluginInfoRelease get(String pluginId) {
        return pluginCache.getIfPresent(pluginId);
    }

    private void remove(List<PluginInfoRelease> enabledPlugins) {
        Map<String, PluginInfoRelease> disabledPlugins = pluginCache.asMap().entrySet().stream()
                .filter(entry -> enabledPlugins.stream()
                        .noneMatch(plugin -> plugin.getPluginId().equals(entry.getKey())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (!disabledPlugins.isEmpty()) {
            disabledPlugins.forEach((pluginId, plugin) -> {
                log.debug("Removing remote plugin configuration '{}' from cache.", pluginId);
                pluginCache.invalidate(pluginId);
                applicationEventPublisher.publishEvent(
                        new RemotePluginConfigChanged(
                                this,
                                RemotePluginConfigChanged.Status.DISABLED,
                                pluginId,
                                plugin.getProps().version,
                                plugin.getProps().getRemoteExtensions()
                        )
                );
            });
        }
    }

    private void addOrUpdate(List<PluginInfoRelease> enabledPlugins) {
        enabledPlugins.forEach(enabledPlugin -> {
            PluginInfoRelease cachedRelease = pluginCache.getIfPresent(enabledPlugin.getPluginId());

            if (cachedRelease == null && versionConstraint(enabledPlugin.getPluginId(), enabledPlugin.getProps().requires)) {
                log.debug("Adding remote plugin configuration '{}' to cache.", enabledPlugin.getPluginId());
                pluginCache.put(enabledPlugin.getPluginId(), enabledPlugin);
                applicationEventPublisher.publishEvent(
                        new RemotePluginConfigChanged(
                                this,
                                RemotePluginConfigChanged.Status.ENABLED,
                                enabledPlugin.getPluginId(),
                                enabledPlugin.getProps().version,
                                enabledPlugin.getProps().getRemoteExtensions()
                        )
                );
            } else if (cachedRelease != null && !cachedRelease.getProps().version.equals(enabledPlugin.getProps().version) &&
                    versionConstraint(enabledPlugin.getPluginId(), enabledPlugin.getProps().requires)) {
                log.debug("Updating remote plugin configuration '{}' in cache.", enabledPlugin.getPluginId());
                pluginCache.put(enabledPlugin.getPluginId(), enabledPlugin);
                applicationEventPublisher.publishEvent(
                        new RemotePluginConfigChanged(
                                this,
                                RemotePluginConfigChanged.Status.UPDATED,
                                enabledPlugin.getPluginId(),
                                enabledPlugin.getProps().version,
                                enabledPlugin.getProps().getRemoteExtensions()
                        )
                );
            } else {
                log.debug("No remote plugin versions found that need to be enabled or updated.");
            }
        });
    }

    private boolean versionConstraint(String pluginId, String requires) {
        if (pluginManager.getVersionManager().checkVersionConstraint(pluginManager.getSystemVersion(), requires)) {
            return true;
        } else {
            log.warn("Requested enabled remote plugin '{}' is not compatible with system version '{}', requires '{}'",
                    pluginId, pluginManager.getSystemVersion(), requires);
            return false;
        }
    }
}
