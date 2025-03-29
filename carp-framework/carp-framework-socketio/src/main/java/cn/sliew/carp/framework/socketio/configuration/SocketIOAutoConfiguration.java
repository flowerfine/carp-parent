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
package cn.sliew.carp.framework.socketio.configuration;

import cn.sliew.carp.framework.socketio.annotation.CarpSocketIoNamespace;
import cn.sliew.carp.framework.socketio.repository.DefaultSocketIORepository;
import cn.sliew.carp.framework.socketio.repository.SocketIORepository;
import com.corundumstudio.socketio.AuthorizationListener;
import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.SpringAnnotationScanner;
import com.corundumstudio.socketio.store.RedissonStoreFactory;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.Objects;

@AutoConfiguration
@AutoConfigureAfter(RedissonAutoConfigurationV2.class)
@EnableConfigurationProperties(SocketIOProperties.class)
public class SocketIOAutoConfiguration {

    @Autowired
    private SocketIOProperties properties;

    @Bean
    @ConditionalOnBean(RedissonClient.class)
    public RedissonStoreFactory redissonStoreFactory(RedissonClient redissonClient) {
        return new RedissonStoreFactory(redissonClient);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public SocketIOServer socketIOServer(
            @Autowired(required = false) AuthorizationListener authorizationListener,
            RedissonStoreFactory redissonStoreFactory) {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        if (StringUtils.isNotBlank(properties.getHost())) {
            config.setHostname(properties.getHost());
        }
        config.setPort(properties.getPort());
        if (Objects.nonNull(authorizationListener)) {
            config.setAuthorizationListener(authorizationListener);
        }
        config.setStoreFactory(redissonStoreFactory);

        SocketConfig socketConfig = new SocketConfig();
        socketConfig.setReuseAddress(true);
        config.setSocketConfig(socketConfig);
        SocketIOServer socketIOServer = new SocketIOServer(config);

        return socketIOServer;
    }

    @Bean
    @ConditionalOnBean(RedissonClient.class)
    public SocketIORepository socketIORepository(RedissonClient redissonClient, SocketIOServer socketIOServer) {
        return new DefaultSocketIORepository(redissonClient, socketIOServer);
    }

    @Bean
    @ConditionalOnMissingBean(SpringAnnotationScanner.class)
    @ConditionalOnClass(CarpSocketIoNamespace.class)
    public SocketIONamespaceBeanPostProcessor socketIONamespaceBeanPostProcessor(SocketIOServer socketIOServer, SocketIORepository socketIORepository) {
        return new SocketIONamespaceBeanPostProcessor(socketIOServer, socketIORepository);
    }

}
