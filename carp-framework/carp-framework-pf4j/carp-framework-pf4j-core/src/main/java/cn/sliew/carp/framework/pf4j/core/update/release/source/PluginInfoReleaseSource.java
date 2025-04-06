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
import org.springframework.core.Ordered;

import java.util.List;
import java.util.Set;

/**
 * Source the desired release(s) from the provided {@link CarpPluginInfo} list.
 * <p>
 * A similar signature as PluginInfoReleaseProvider, but used to model releases from one specific
 * source.
 */
public interface PluginInfoReleaseSource extends Ordered {

    /**
     * Get plugin releases from a list of plugin info objects
     */
    Set<PluginInfoRelease> getReleases(List<CarpPluginInfo> pluginInfo);

    /**
     * Optionally process releases (i.e., POST to another service, modify in-memory set,
     * write to a filesystem, etc).
     */
    default void processReleases(Set<PluginInfoRelease> pluginInfoReleases) {
        /* default implementation */
    }
}
