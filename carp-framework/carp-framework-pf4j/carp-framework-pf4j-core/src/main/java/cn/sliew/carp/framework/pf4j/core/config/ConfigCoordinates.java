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
package cn.sliew.carp.framework.pf4j.core.config;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Represents the config path coordinates for a particular loading strategy.
 */
public abstract class ConfigCoordinates {

    private static final String ROOT_PATH = "/carp/extensibility";

    /**
     * Converts a coordinate class into a Jackson JsonNode pointer.
     */
    public abstract String toPointer();

    /**
     * Config coordinates for a plugin's extension.
     */
    public static class ExtensionConfigCoordinates extends ConfigCoordinates {
        private String pluginId;
        private String extensionConfigId;
        private String extensionsNamespace = "extensions";

        public ExtensionConfigCoordinates(String pluginId, String extensionConfigId) {
            this.pluginId = pluginId;
            this.extensionConfigId = extensionConfigId;
        }

        @Override
        public String toPointer() {
            List<String> coords;
            if (StringUtils.isEmpty(extensionConfigId)) {
                coords = List.of(pluginId, extensionsNamespace);
            } else {
                coords = List.of(pluginId, extensionsNamespace, extensionConfigId);
            }
            return String.format("%s/plugins/%s/config", ROOT_PATH, String.join("/", coords).replace(".", "/"));
        }
    }

    /**
     * Config coordinates for a plugin.
     */
    @AllArgsConstructor
    public static class PluginConfigCoordinates extends ConfigCoordinates {
        private String pluginId;
        private String pluginConfigId;

        @Override
        public String toPointer() {
            List<String> coords;
            if (StringUtils.isEmpty(pluginConfigId)) {
                coords = List.of(pluginId);
            } else {
                coords = List.of(pluginId, pluginConfigId);
            }

            return String.format("%s/plugins/%s/config", ROOT_PATH, String.join("/", coords).replace(".", "/"));
        }
    }

    /**
     * Config coordinates for a system extension.
     */
    @AllArgsConstructor
    public static class SystemExtensionConfigCoordinates extends ConfigCoordinates {
        private String extensionConfigId;

        @Override
        public String toPointer() {
            return String.format("%s/extensions/%s/config", ROOT_PATH, extensionConfigId.replace(".", "/"));
        }
    }

    /**
     * Config coordinates for plugin repository configs.
     */
    public static class RepositoryConfigCoordinates extends ConfigCoordinates {

        @Override
        public String toPointer() {
            return String.format("%s/repositories", ROOT_PATH);
        }
    }

}
