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
import cn.sliew.carp.framework.pf4j.core.update.release.PluginInfoRelease;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.update.PluginInfo.PluginRelease;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Selects a {@link PluginInfoRelease} based on which release has {@link CarpPluginInfo.CarpPluginRelease#isPreferred()}
 * set to true.
 */
@Slf4j
public class PreferredPluginInfoReleaseSource implements PluginInfoReleaseSource {

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
        for (PluginRelease release : pluginInfo.getReleases()) {
            if (release instanceof CarpPluginInfo.CarpPluginRelease carpPluginRelease &&
                    carpPluginRelease.isPreferred()) {
                log.debug("Preferred release version '{}' for plugin '{}'", release.version, pluginInfo.id);
                return new PluginInfoRelease(pluginInfo.id, carpPluginRelease);
            }
        }

        log.debug("No preferred release version found for '{}'", pluginInfo.id);
        return null;
    }

    /**
     * The {@link PluginInfoReleaseSource} chain order.
     */
    @Override
    public int getOrder() {
        return 200;
    }
}
