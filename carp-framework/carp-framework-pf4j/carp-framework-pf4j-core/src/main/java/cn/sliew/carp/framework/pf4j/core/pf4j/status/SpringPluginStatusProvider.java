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
package cn.sliew.carp.framework.pf4j.core.pf4j.status;

import cn.sliew.carp.framework.spring.dynamicconfig.DynamicConfigService;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginStatusProvider;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SpringPluginStatusProvider implements
        PluginStatusProvider,
        ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private final Map<String, Object> propertySourceBackingStore = new HashMap<>();
    private final MapPropertySource propertySource = new MapPropertySource("plugins", propertySourceBackingStore);

    private final DynamicConfigService dynamicConfigService;
    private final String rootConfig;

    public SpringPluginStatusProvider(DynamicConfigService dynamicConfigService, String rootConfig) {
        this.dynamicConfigService = dynamicConfigService;
        this.rootConfig = rootConfig;
    }

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        log.debug("Adding {} as new PropertySource", this.getClass().getSimpleName());
        event.getEnvironment().getPropertySources().addFirst(propertySource);
    }

    @Override
    public void disablePlugin(String pluginId) {
        log.info("Disabling plugin: {}", pluginId);
        propertySourceBackingStore.put(enabledPropertyName(pluginId), false);
    }

    @Override
    public void enablePlugin(String pluginId) {
        log.info("Enabling plugin: {}", pluginId);
        propertySourceBackingStore.put(enabledPropertyName(pluginId), true);
    }

    @Override
    public boolean isPluginDisabled(String pluginId) {
        return !isEnabled(pluginId);
    }

    /**
     * Returns whether or not a plugin is enabled or not.
     */
    public boolean isPluginEnabled(String pluginId) {
        return isEnabled(pluginId);
    }

    private boolean isEnabled(String pluginId) {
        return dynamicConfigService.isEnabled(enabledPropertyName(pluginId), false);
    }

    private String enabledPropertyName(String pluginId) {
        return rootConfig + "." + pluginId;
    }

    /**
     * Provides the plugin version a plugin should be running.
     */
    public String pluginVersion(String pluginId) {
        return dynamicConfigService.getConfig(String.class, versionPropertyName(pluginId), "unspecified");
    }

    private String versionPropertyName(String pluginId) {
        return rootConfig + "." + pluginId + ".version";
    }
}
