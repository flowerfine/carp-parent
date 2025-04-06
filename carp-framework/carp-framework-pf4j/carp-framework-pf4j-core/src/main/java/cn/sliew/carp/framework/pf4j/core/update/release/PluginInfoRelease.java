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
package cn.sliew.carp.framework.pf4j.core.update.release;

import cn.sliew.carp.framework.pf4j.core.update.CarpPluginInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class PluginInfoRelease {

    private final String pluginId;
    private final CarpPluginInfo.CarpPluginRelease props;

    /**
     * A tuple of {@param pluginId} and {@link CarpPluginInfo.CarpPluginRelease}
     *
     * @param pluginId The plugin ID
     * @param props    A {@link CarpPluginInfo.CarpPluginRelease} for the given {@param pluginId}
     */
    public PluginInfoRelease(String pluginId, CarpPluginInfo.CarpPluginRelease props) {
        this.pluginId = pluginId;
        this.props = props;
    }
}
