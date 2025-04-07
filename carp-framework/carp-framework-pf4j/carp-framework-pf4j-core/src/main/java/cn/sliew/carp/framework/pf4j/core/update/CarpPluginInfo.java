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

import cn.sliew.carp.framework.pf4j.core.remote.RemoteExtensionConfig;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;
import org.pf4j.update.PluginInfo;
import org.pf4j.update.UpdateRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Carp-specific {@link PluginInfo}.
 * <p>
 * Required to expose {@link CarpPluginRelease} to the rest of the framework.
 */
public class CarpPluginInfo extends PluginInfo {

    /**
     * Get all known releases for a plugin.
     */
    @SuppressWarnings("unchecked")
    public List<CarpPluginRelease> getReleases() {
        return super.releases.stream().map(release -> (CarpPluginRelease) release).toList();
    }

    /**
     * Set all known releases for a plugin.
     */
    @JsonSetter("releases")
    public void setReleases(List<CarpPluginRelease> spinnakerReleases) {
        this.releases = spinnakerReleases.stream().map(release -> (PluginRelease) release).toList();
    }

    /**
     * It is not guaranteed that the {@link UpdateRepository} implementation returns a
     * CarpPluginInfo object.  Therefore, additional fields defined here must provide a default
     * value.
     */
    @Data
    public static class CarpPluginRelease extends PluginRelease {

        /**
         * Whether or not the plugin release is preferred. Preferred plugin releases will be more
         * likely to be loaded than other releases.
         */
        private boolean preferred = false;

        /**
         * Any remote extension configs attached to the plugin.
         */
        private List<RemoteExtensionConfig> remoteExtensions = new ArrayList<>();

    }
}
