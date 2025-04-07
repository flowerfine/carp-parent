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

import cn.sliew.carp.framework.common.jackson.subtype.SubtypeLocator;
import cn.sliew.carp.framework.pf4j.core.remote.RemotePluginConfigChangedListener;
import cn.sliew.carp.framework.pf4j.core.remote.RemotePluginsCache;
import cn.sliew.carp.framework.pf4j.core.remote.RemotePluginsProvider;
import cn.sliew.carp.framework.pf4j.core.remote.extension.RemoteExtensionPointDefinition;
import cn.sliew.milky.common.util.JacksonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Remote plugin beans are not statically loaded as they do not need to be loaded as early in the
 * Spring application lifecycle as beans from {@link Pf4jAutoConfiguration}. Remote plugins use
 * the plugin framework for configuration, versioning, and plugin status but are otherwise
 * completely separate from the in-jvm plugin framework.
 */
@Configuration
public class RemotePluginsConfiguration {

    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = JacksonUtil.getMapper().copy();
        return mapper;
    }

    @Bean
    @ConditionalOnMissingBean(OkHttpClient.class)
    public OkHttpClient okHttpClient(@Autowired(required = false) List<Interceptor> interceptors) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (!CollectionUtils.isEmpty(interceptors)) {
            interceptors.forEach(builder::addInterceptor);
        }
        return builder.build();
    }

    @Bean
    public RemotePluginsCache remotePluginsCache(
            ApplicationEventPublisher applicationEventPublisher) {
        return new RemotePluginsCache(applicationEventPublisher);
    }

    @Bean
    public RemotePluginConfigChangedListener remotePluginConfigChangedListener(
            ObjectMapper objectMapper,
            ObjectProvider<List<SubtypeLocator>> subtypeLocatorsProvider,
            OkHttpClient okHttpClient,
            RemotePluginsCache remotePluginsCache,
            List<RemoteExtensionPointDefinition> remoteExtensionPointDefinitions) {
        return new RemotePluginConfigChangedListener(
                objectMapper,
                subtypeLocatorsProvider,
                okHttpClient,
                remotePluginsCache,
                remoteExtensionPointDefinitions);
    }

    @Bean
    public RemotePluginsProvider remotePluginProvider(RemotePluginsCache remotePluginsCache) {
        return new RemotePluginsProvider(remotePluginsCache);
    }
}
