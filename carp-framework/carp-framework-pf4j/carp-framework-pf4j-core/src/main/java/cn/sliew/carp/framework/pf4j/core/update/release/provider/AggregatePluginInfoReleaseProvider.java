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
package cn.sliew.carp.framework.pf4j.core.update.release.provider;

import cn.sliew.carp.framework.pf4j.core.pf4j.status.SpringStrictPluginLoaderStatusProvider;
import cn.sliew.carp.framework.pf4j.core.update.CarpPluginInfo;
import cn.sliew.carp.framework.pf4j.core.update.release.PluginInfoRelease;
import cn.sliew.carp.framework.pf4j.core.update.release.source.PluginInfoReleaseSource;
import org.pf4j.update.PluginInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A composite {@link PluginInfoReleaseSource}, that processes releases as defined by their order.
 */
public class AggregatePluginInfoReleaseProvider implements PluginInfoReleaseProvider {

    private final List<PluginInfoReleaseSource> pluginInfoReleaseSources;
    private final SpringStrictPluginLoaderStatusProvider strictPluginLoaderStatusProvider;

    public AggregatePluginInfoReleaseProvider(
            List<PluginInfoReleaseSource> pluginInfoReleaseSources,
            SpringStrictPluginLoaderStatusProvider strictPluginLoaderStatusProvider) {
        this.pluginInfoReleaseSources = pluginInfoReleaseSources;
        this.strictPluginLoaderStatusProvider = strictPluginLoaderStatusProvider;
    }

    @Override
    public Set<PluginInfoRelease> getReleases(List<CarpPluginInfo> pluginInfo) {
        Set<PluginInfoRelease> pluginInfoReleases = new HashSet<>();

        for (PluginInfoReleaseSource source : pluginInfoReleaseSources) {
            for (PluginInfoRelease release : source.getReleases(pluginInfo)) {
                pluginInfoReleases.removeIf(existing -> existing.getPluginId().equals(release.getPluginId()));
                pluginInfoReleases.add(release);
            }
            // hook
            source.processReleases(pluginInfoReleases);
        }

        for (CarpPluginInfo plugin : pluginInfo) {
            if (missingPluginWithStrictLoading(pluginInfoReleases, plugin)) {
                throw new PluginReleaseNotFoundException(plugin.id, pluginInfoReleaseSources);
            }
        }

        return pluginInfoReleases;
    }

    private boolean missingPluginWithStrictLoading(Set<PluginInfoRelease> pluginInfoReleases, PluginInfo pluginInfo) {
        return pluginInfoReleases.stream()
                .noneMatch(release -> release.getPluginId().equals(pluginInfo.id)) &&
                strictPluginLoaderStatusProvider.isStrictPluginLoading();
    }
}
