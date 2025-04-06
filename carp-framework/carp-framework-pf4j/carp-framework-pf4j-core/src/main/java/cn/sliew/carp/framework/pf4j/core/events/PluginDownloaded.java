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

import cn.sliew.carp.framework.pf4j.core.update.CarpUpdateMananger;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PluginDownloaded extends ApplicationEvent {

    private final Status status;
    private final String pluginId;
    private final String version;

    /**
     * An event emitted whenever a plugin has been downloaded (successfully or not).
     *
     * @param source   The {@link CarpUpdateMananger}, which has emitted this event.
     * @param status   Whether or not the plugin download succeeded
     * @param pluginId The ID of the plugin
     * @param version  The version of the plugin downloaded
     */
    public PluginDownloaded(
            CarpUpdateMananger source,
            Status status,
            String pluginId,
            String version) {
        super(source);
        this.status = status;
        this.pluginId = pluginId;
        this.version = version;
    }

    /**
     * The down status of the plugin.
     */
    public enum Status {
        SUCCEEDED,
        FAILED
    }
}
