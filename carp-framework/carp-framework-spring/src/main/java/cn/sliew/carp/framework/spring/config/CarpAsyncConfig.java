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
package cn.sliew.carp.framework.spring.config;

import cn.sliew.carp.framework.spring.concurrent.MetricsThreadPoolExecutor;
import cn.sliew.carp.framework.spring.event.DelegatingApplicationEventMulticaster;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Collections;

@Configuration
public class CarpAsyncConfig implements AsyncConfigurer {

    private static final String NAME = "carpApplicationEventTaskExecutor";

    @Bean(name = NAME)
    public ThreadPoolTaskExecutor carpApplicationEventTaskExecutor(MeterRegistry registry) {
        ThreadPoolTaskExecutor threadPool = new MetricsThreadPoolExecutor(registry, Collections.emptyList());
        threadPool.setThreadNamePrefix("spring-events-async-thread-");
        int processors = Runtime.getRuntime().availableProcessors();
        threadPool.setCorePoolSize(processors);
        threadPool.setMaxPoolSize(processors * 3);
        return threadPool;
    }

    @Bean
    public ApplicationEventMulticaster applicationEventMulticaster(
            @Qualifier(NAME) ThreadPoolTaskExecutor taskExecutor) {
        // TODO rz - Add error handlers
        SimpleApplicationEventMulticaster async = new SimpleApplicationEventMulticaster();
        async.setTaskExecutor(taskExecutor);
        SimpleApplicationEventMulticaster sync = new SimpleApplicationEventMulticaster();

        return new DelegatingApplicationEventMulticaster(sync, async);
    }
}
