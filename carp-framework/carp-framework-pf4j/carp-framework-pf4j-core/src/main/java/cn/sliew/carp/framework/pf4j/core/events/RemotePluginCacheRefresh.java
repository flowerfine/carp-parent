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

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class RemotePluginCacheRefresh extends ApplicationEvent {

    private final String pluginId;

    /**
     * A Spring {@link ApplicationEvent} that is emitted when the remote plugins cache is changed.
     * <p>
     * The remote plugins cache is the cache of resolved remote plugins with remote transport
     * clients.  This can optionally be used in Spinnaker services to ensure a remote
     * extension point implementation has the latest cached version of the remote plugin.
     *
     * @param source   The source of the event
     * @param pluginId The plugin ID that triggered this cache refresh event
     */
    public RemotePluginCacheRefresh(RemotePluginsCache source, String pluginId) {
        super(source);
        this.pluginId = pluginId;
    }

    @Override
    public RemotePluginsCache getSource() {
        return (RemotePluginsCache) super.getSource();
    }
}
