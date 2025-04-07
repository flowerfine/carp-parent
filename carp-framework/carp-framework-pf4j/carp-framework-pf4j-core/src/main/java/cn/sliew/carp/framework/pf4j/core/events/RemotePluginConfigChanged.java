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
package cn.sliew.carp.framework.pf4j.core.events;

import cn.sliew.carp.framework.pf4j.core.remote.RemoteExtensionConfig;
import cn.sliew.carp.framework.pf4j.core.update.release.remote.RemotePluginInfoReleaseCache;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class RemotePluginConfigChanged extends ApplicationEvent {

    private final Status status;
    private final String pluginId;
    private final String version;
    private final List<RemoteExtensionConfig> remoteExtensionConfigs;

    /**
     * A Spring {@link ApplicationEvent} that is emitted when a remote plugin configuration is changed.
     *
     * @param source                 The source of the event
     * @param status                 Whether the remote plugin config is {@link Status#ENABLED}, {@link Status#DISABLED}, or {@link Status#UPDATED}.
     *                               {@link Status#ENABLED} and {@link Status#DISABLED} are self-explanatory. {@link Status#UPDATED} occurs when
     *                               a plugin is already enabled and the version changes.
     * @param pluginId               The plugin ID for the remote extension
     * @param version                The plugin release version
     * @param remoteExtensionConfigs A list of remote extension configs associated with the plugin release
     */
    public RemotePluginConfigChanged(
            RemotePluginInfoReleaseCache source,
            Status status,
            String pluginId,
            String version,
            List<RemoteExtensionConfig> remoteExtensionConfigs) {
        super(source);
        this.status = status;
        this.pluginId = pluginId;
        this.version = version;
        this.remoteExtensionConfigs = remoteExtensionConfigs;
    }

    @Override
    public RemotePluginInfoReleaseCache getSource() {
        return (RemotePluginInfoReleaseCache) super.getSource();
    }

    /**
     * The type of config change.
     */
    public enum Status {
        ENABLED,
        DISABLED,
        UPDATED
    }
}