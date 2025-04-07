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
package cn.sliew.carp.framework.pf4j.core.remote;

import cn.sliew.carp.framework.common.jackson.subtype.SubtypeLocator;
import cn.sliew.carp.framework.feign.endpoint.DefaultServiceEndpoint;
import cn.sliew.carp.framework.pf4j.core.events.RemotePluginConfigChanged;
import cn.sliew.carp.framework.pf4j.core.remote.extension.RemoteExtension;
import cn.sliew.carp.framework.pf4j.core.remote.extension.RemoteExtensionPointDefinition;
import cn.sliew.carp.framework.spring.jackson.ObjectMapperSubtypeConfigurer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationListener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Listen for remote plugin configuration changes, instantiate {@link RemotePlugin} and {@link RemoteExtension}
 * objects as necessary, and add or remove the plugins from the remote plugin cache.
 */
@Slf4j
public class RemotePluginConfigChangedListener implements ApplicationListener<RemotePluginConfigChanged> {

    private final ObjectMapper objectMapper;
    private final OkHttpClient okHttpClient;
    private final RemotePluginsCache remotePluginsCache;
    private final List<RemoteExtensionPointDefinition> remoteExtensionPointDefinitions;

    public RemotePluginConfigChangedListener(
            ObjectMapper objectMapper,
            ObjectProvider<List<SubtypeLocator>> subtypeLocatorsProvider,
            OkHttpClient okHttpClient,
            RemotePluginsCache remotePluginsCache,
            List<RemoteExtensionPointDefinition> remoteExtensionPointDefinitions) {
        this.objectMapper = objectMapper;
        this.okHttpClient = okHttpClient;
        this.remotePluginsCache = remotePluginsCache;
        this.remoteExtensionPointDefinitions = remoteExtensionPointDefinitions;

        List<SubtypeLocator> subtypeLocators = subtypeLocatorsProvider.getIfAvailable();
        if (subtypeLocators != null && !subtypeLocators.isEmpty()) {
            new ObjectMapperSubtypeConfigurer(true).registerSubtypes(objectMapper, subtypeLocators);
        }
    }

    @Override
    public void onApplicationEvent(RemotePluginConfigChanged event) {
        switch (event.getStatus()) {
            case ENABLED:
            case UPDATED:
                put(event);
                break;
            case DISABLED:
                remotePluginsCache.remove(event.getPluginId());
                break;
        }
    }

    private void put(RemotePluginConfigChanged event) {
        Set<RemoteExtension> remoteExtensions = new HashSet<>();

        event.getRemoteExtensionConfigs().forEach(remoteExtensionConfig -> {
            // TODO(jonsie): Support enabling/disabling transports in the config.
            // Configure HTTP if it is available since it is the only configurable transport right now.
            OkHttpRemoteExtensionTransport remoteExtensionTransport;
            if (!remoteExtensionConfig.getTransport().getHttp().getUrl().isEmpty()) {

                var client = okHttpClientProvider.getObject().getClient(
                        new DefaultServiceEndpoint(
                                remoteExtensionConfig.getId(),
                                remoteExtensionConfig.getTransport().getHttp().getUrl(),
                                remoteExtensionConfig.getTransport().getHttp().getConfig()
                        )
                );
                remoteExtensionTransport = new OkHttpRemoteExtensionTransport(
                        objectMapperProvider.getObject(),
                        client,
                        remoteExtensionConfig.getTransport().getHttp()
                );
            } else {
                throw new RemoteExtensionTransportConfigurationException(event.getPluginId());
            }

            RemoteExtensionPointDefinition remoteExtensionDefinition = remoteExtensionPointDefinitions.stream()
                    .filter(def -> def.type().equals(remoteExtensionConfig.getType()))
                    .findFirst()
                    .orElseThrow(() -> new RemoteExtensionDefinitionNotFound(remoteExtensionConfig.getType()));

            Class<?> configType = remoteExtensionDefinition.configType();

            remoteExtensions.add(
                    new RemoteExtension(
                            remoteExtensionConfig.getId(),
                            event.getPluginId(),
                            remoteExtensionDefinition.type(),
                            objectMapperProvider.getObject().convertValue(remoteExtensionConfig.getConfig(), configType),
                            remoteExtensionTransport
                    )
            );
        });

        RemotePlugin remotePlugin = new RemotePlugin(event.getPluginId(), event.getVersion(), remoteExtensions);
        remotePluginsCache.put(remotePlugin);
        log.debug("Remote plugin '{}' added to cache due to '{}' event", event.getPluginId(), event.getStatus());
    }

    /**
     * Thrown when there is no configuration for the remote transport.
     */
    public static class RemoteExtensionTransportConfigurationException extends RuntimeException {
        public RemoteExtensionTransportConfigurationException(String pluginId) {
            super(String.format("No transport configuration for remote plugin '%s'", pluginId));
        }
    }

    /**
     * Thrown when a remote extension definition is not found.
     */
    public static class RemoteExtensionDefinitionNotFound extends RuntimeException {
        public RemoteExtensionDefinitionNotFound(String type) {
            super(String.format("No remote extension definition found for type '%s'", type));
        }
    }
}
