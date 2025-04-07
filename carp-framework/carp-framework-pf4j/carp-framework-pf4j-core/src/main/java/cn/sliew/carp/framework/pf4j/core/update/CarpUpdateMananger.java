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
package cn.sliew.carp.framework.pf4j.core.update;

import cn.sliew.carp.framework.pf4j.core.events.PluginDownloaded;
import cn.sliew.carp.framework.pf4j.core.spring.SpringPluginService;
import cn.sliew.carp.framework.pf4j.core.update.release.PluginInfoRelease;
import cn.sliew.milky.common.util.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginManager;
import org.pf4j.PluginRuntimeException;
import org.pf4j.PluginWrapper;
import org.pf4j.VersionManager;
import org.pf4j.update.PluginInfo;
import org.pf4j.update.UpdateManager;
import org.pf4j.update.UpdateRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TODO(jonsie): We have disabled {@link UpdateManager} update, load, and start plugin
 *  logic here. This is now used only to manage the list of {@link UpdateRepository} objects, download
 *  the desired artifact, and check version constraints via an implementation of
 *  {@link VersionManager}. At some point, we may want to consider removing
 *  {@link UpdateManager}.
 */
@Slf4j
public class CarpUpdateMananger extends UpdateManager {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final PluginManager pluginManager;

    public CarpUpdateMananger(
            ApplicationEventPublisher applicationEventPublisher,
            PluginManager pluginManager,
            List<UpdateRepository> repositories) {
        super(pluginManager, repositories);
        this.applicationEventPublisher = applicationEventPublisher;
        this.pluginManager = pluginManager;
    }

    @Override
    public List<PluginInfo> getPlugins() {
        List<PluginInfo> plugins = new ArrayList<>();

        for (UpdateRepository repository : getRepositories()) {
            repository.getPlugins().values().stream()
                    .map(plugin -> JacksonUtil.getMapper().convertValue(plugin, CarpPluginInfo.class))
                    .forEach(plugins::add);
        }

        return plugins;
    }


    public Set<Path> downloadPluginReleases(Set<PluginInfoRelease> pluginInfoReleases) {
        return pluginInfoReleases.stream()
                .map(this::download)
                .filter(path -> path != null)
                .collect(Collectors.toSet());
    }

    private Path download(PluginInfoRelease release) {
        // This is a remote plugin only, do nothing here.
        if (Objects.nonNull(release.getProps())) {
            if (StringUtils.hasText(release.getProps().url) == false &&
                    CollectionUtils.isEmpty(release.getProps().getRemoteExtensions()) == false) {
                log.info("Nothing to download - plugin '{}' is a remote plugin and there is no in-process plugin binary.", release.getPluginId());
                return null;
            }
        }


        String pluginId = release.getPluginId();
        CarpPluginInfo.CarpPluginRelease props = release.getProps();

        PluginWrapper loadedPlugin = pluginManager.getPlugin(pluginId);
        if (loadedPlugin != null) {
            String loadedPluginVersion = loadedPlugin.getDescriptor().getVersion();

            // If a plugin was built without a version specified (via the Plugin-Version MANIFEST.MF
            // attribute), to be safe we always check for the configured plugin version.
            if (loadedPluginVersion.equals("unspecified") ||
                    pluginManager.getVersionManager().compareVersions(props.version, loadedPluginVersion) > 0) {
                log.debug(
                        "Newer version '{}' of plugin '{}' found, deleting previous version '{}'",
                        props.version, pluginId, loadedPluginVersion
                );
                boolean deleted = pluginManager.deletePlugin(loadedPlugin.getPluginId());

                if (!deleted) {
                    throw new RuntimeException(
                            "Unable to update plugin '" + pluginId + "' to version '" + props.version + "', " +
                                    "failed to delete previous version '" + loadedPluginVersion + "'"
                    );
                }
            } else {
                return null;
            }
        }

        log.debug("Downloading plugin '{}' with version '{}'", pluginId, props.version);
        Path tmpPath = downloadPluginRelease(pluginId, props.version);
        Path downloadedPluginPath = write(pluginManager.getPluginsRoot(), pluginId, tmpPath);

        log.debug("Downloaded plugin '{}'", pluginId);
        applicationEventPublisher.publishEvent(
                new PluginDownloaded(this, PluginDownloaded.Status.SUCCEEDED, pluginId, props.version)
        );

        return downloadedPluginPath;
    }

    /**
     * Supports the scenario wherein we want the latest plugin for the specified service (i.e., not
     * necessarily the service that executes this code).
     */
    public PluginInfo.PluginRelease getLastPluginRelease(String id, String applicationName) {
        Map<String, PluginInfo> pluginsMap = getPluginsMap();
        PluginInfo pluginInfo = pluginsMap.get(id);
        if (pluginInfo == null) {
            log.warn("Unable to find plugin info for '{}'", id);
            return null;
        }

        VersionManager versionManager = pluginManager.getVersionManager();
        Map<String, PluginInfo.PluginRelease> lastPluginRelease = new HashMap<>();

        // We're loading a plugin for a different service and we don't know the other service's version
        // We check if the release requirement mentions the service to see if the plugin is applicable,
        // then pick the latest version.
        for (PluginInfo.PluginRelease release : pluginInfo.releases) {
            if (release.requires.toLowerCase().contains(applicationName.toLowerCase())) {
                if (lastPluginRelease.get(id) == null) {
                    lastPluginRelease.put(id, release);
                } else if (versionManager.compareVersions(release.version, lastPluginRelease.get(id).version) > 0) {
                    lastPluginRelease.put(id, release);
                }
            }
        }

        return lastPluginRelease.get(id);
    }

    /**
     * Exists to expose protected {@link #downloadPlugin} - must remain public.
     * <p>
     * TODO(jonsie): This will call {@link UpdateManager#getLastPluginRelease} if `version`
     *  is null.  Shouldn't happen, but it could.  That is potentially problematic if the desired
     *  applicationName is different than the service that executes this code.  Probably another reason
     *  to consider moving away from {@link UpdateManager}.
     */
    public Path downloadPluginRelease(String pluginId, String version) {
        return downloadPlugin(pluginId, version);
    }

    /**
     * Write the plugin, creating the the plugins root directory defined in {@link PluginManager} if
     * necessary.
     */
    private Path write(Path pluginsRoot, String pluginId, Path downloaded) {
        if (pluginManager.getPluginsRoot().equals(pluginsRoot)) {
            Path file = pluginsRoot.resolve(pluginId + "-" + downloaded.getFileName().toString());
            new File(pluginsRoot.toString()).mkdirs();
            try {
                return Files.move(downloaded, file, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new PluginRuntimeException(e, "Failed to write file '{}' to plugins folder", file);
            }
        } else {
            throw new UnsupportedOperationException(
                    "This operation is only supported on the specified " +
                            "plugins root directory."
            );
        }
    }

    /**
     * This method is not supported as it calls pluginManager.loadPlugin and pluginManager.startPlugin.
     * Instead, we only want to install the plugins and leave loading and starting to {@link SpringPluginService}
     */
    @Override
    public synchronized boolean installPlugin(String id, String version) {
        throw new UnsupportedOperationException("UpdateManager installPlugin is not supported");
    }

    /**
     * This method is not supported as it calls pluginManager.loadPlugin and pluginManager.startPlugin.
     * Instead, we only want to install the plugins and leave loading and starting to {@link SpringPluginService}.
     */
    @Override
    public boolean updatePlugin(String id, String version) {
        throw new UnsupportedOperationException("UpdateManager updatePlugin is not supported");
    }
}
