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
package cn.sliew.carp.framework.pf4j.core.actuator;

import lombok.AllArgsConstructor;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginManager;
import org.pf4j.PluginNotFoundException;
import org.pf4j.PluginWrapper;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;

import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@Endpoint(id = "pf4j")
public class Pf4jPluginsEndpoint {

    private PluginManager pluginManager;

    @ReadOperation
    public List<PluginDescriptor> plugins() {
        return pluginManager.getPlugins().stream()
                .map(plugin -> plugin.getDescriptor()).toList();
    }

    @ReadOperation
    public PluginDescriptor plugin(@Selector String pluginId) {
        PluginWrapper pluginWrapper = pluginManager.getPlugin(pluginId);
        if (Objects.isNull(pluginWrapper)) {
            throw new PluginNotFoundException("Plugin not found: " + pluginId);
        }
        return pluginWrapper.getDescriptor();
    }
}
