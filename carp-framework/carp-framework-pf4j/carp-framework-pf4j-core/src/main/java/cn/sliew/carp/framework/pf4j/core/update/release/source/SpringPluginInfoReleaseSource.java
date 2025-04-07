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
package cn.sliew.carp.framework.pf4j.core.update.release.source;

import cn.sliew.carp.framework.pf4j.core.pf4j.status.SpringPluginStatusProvider;
import cn.sliew.carp.framework.pf4j.core.update.CarpPluginInfo;
import cn.sliew.carp.framework.pf4j.core.update.release.PluginInfoRelease;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Sources plugin releases from Spring properties via {@link SpringPluginStatusProvider}.
 */
@Slf4j
public class SpringPluginInfoReleaseSource implements PluginInfoReleaseSource {

    private final SpringPluginStatusProvider pluginStatusProvider;

    public SpringPluginInfoReleaseSource(SpringPluginStatusProvider pluginStatusProvider) {
        this.pluginStatusProvider = pluginStatusProvider;
    }

    @Override
    public Set<PluginInfoRelease> getReleases(List<CarpPluginInfo> pluginInfo) {
        Set<PluginInfoRelease> releases = new HashSet<>();
        for (CarpPluginInfo info : pluginInfo) {
            PluginInfoRelease release = pluginInfoRelease(info);
            if (release != null) {
                releases.add(release);
            }
        }
        return releases;
    }

    private PluginInfoRelease pluginInfoRelease(CarpPluginInfo pluginInfo) {
        String pluginVersion;
        try {
            pluginVersion = pluginStatusProvider.pluginVersion(pluginInfo.id);
        } catch (IllegalArgumentException e) {
            log.error("Unable to read configured plugin version from Spring property due to: {}", e.getMessage());
            return null;
        }

        for (CarpPluginInfo.CarpPluginRelease release : pluginInfo.getReleases()) {
            if (release.version.equals(pluginVersion)) {
                log.debug("Spring configured release version '{}' for plugin '{}'", release.version, pluginInfo.id);
                return new PluginInfoRelease(pluginInfo.id, release);
            }
        }

        log.debug("Spring configured release version not found for plugin '{}'", pluginInfo.id);
        return null;
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
