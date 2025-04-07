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
package cn.sliew.carp.framework.pf4j.spring;

import cn.sliew.carp.framework.pf4j.core.update.props.PluginRepositoryProperties;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Root-level configuration properties for plugins.
 *
 * <p>These properties are mapped using a {@link
 * org.springframework.boot.context.properties.bind.Binder} because they are needed before the
 * {@link org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor}
 * has run.
 *
 * @see Pf4jAutoConfiguration
 */
@Data
public class PluginsConfigurationProperties {

    public static final String CONFIG_NAMESPACE = "carp.framework.pf4j";
    public static final String DEFAULT_ROOT_PATH = "plugins";
    public static final String PREFIX = CONFIG_NAMESPACE + "." + DEFAULT_ROOT_PATH;

    /**
     * The root filepath to the directory containing all plugins.
     *
     * <p>If an absolute path is not provided, the path will be calculated relative to the executable.
     */
    private String pluginsRootPath = DEFAULT_ROOT_PATH;

    /**
     * A definition of repositories for use in plugin downloads.
     *
     * <p>The key of this map is the name of the repository.
     */
    private Map<String, PluginRepositoryProperties> repositories = new HashMap<>();

    /**
     * Whether or not to add the plugin repositories from https://github.com/flowerfine/carp-parent/plugins by
     * default.
     */
    private boolean enableDefaultRepositories = false;
}
