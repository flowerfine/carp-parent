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

import cn.sliew.carp.framework.pf4j.core.update.CarpPluginInfo;
import cn.sliew.carp.framework.pf4j.core.update.CarpUpdateMananger;
import cn.sliew.carp.framework.pf4j.core.update.release.PluginInfoRelease;
import cn.sliew.carp.framework.pf4j.core.update.release.provider.AggregatePluginInfoReleaseProvider;
import cn.sliew.milky.common.util.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.update.PluginInfo.PluginRelease;
import org.springframework.core.Ordered;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Source the last published plugin info release.
 */
@Slf4j
public class LatestPluginInfoReleaseSource implements PluginInfoReleaseSource {

    private final CarpUpdateMananger updateManager;
    private final String serviceName;

    public LatestPluginInfoReleaseSource(CarpUpdateMananger updateManager) {
        this(updateManager, null);
    }

    public LatestPluginInfoReleaseSource(CarpUpdateMananger updateManager, String serviceName) {
        this.updateManager = updateManager;
        this.serviceName = serviceName;
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
        PluginRelease latestRelease;
        if (serviceName == null) {
            latestRelease = updateManager.getLastPluginRelease(pluginInfo.id);
        } else {
            latestRelease = updateManager.getLastPluginRelease(pluginInfo.id, serviceName);
        }

        if (latestRelease != null) {
            log.debug("Latest release version '{}' for plugin '{}'", latestRelease.version, pluginInfo.id);
            return new PluginInfoRelease(
                    pluginInfo.id,
                    JacksonUtil.toObject(latestRelease, CarpPluginInfo.CarpPluginRelease.class)
            );
        } else {
            log.debug("Latest release version not found for plugin '{}'", pluginInfo.id);
            return null;
        }
    }

    /**
     * Ensures this runs first in
     * {@link AggregatePluginInfoReleaseProvider}
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
