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
package cn.sliew.carp.framework.queue.kekio.configuration;

import cn.sliew.carp.framework.queue.kekio.MessageHandler;
import cn.sliew.carp.framework.queue.kekio.Queue;
import cn.sliew.carp.framework.queue.kekio.QueueExecutor;
import cn.sliew.carp.framework.queue.kekio.ThreadPoolQueueExecutor;
import cn.sliew.carp.framework.queue.kekio.memory.InMemoryQueue;
import cn.sliew.carp.framework.queue.kekio.metrics.EventPublisher;
import cn.sliew.carp.framework.queue.kekio.metrics.QueueMetricsPublisher;
import cn.sliew.carp.framework.queue.kekio.redis.JedisClusterQueue;
import cn.sliew.carp.framework.queue.kekio.redis.JedisQueue;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.util.Pool;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

@EnableConfigurationProperties(KekioQueueProperties.class)
public class KekioQueueAutoConfiguration {

    @Autowired
    private KekioQueueProperties properties;

    @Bean
    public QueueExecutor<ThreadPoolTaskExecutor> queueExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("keiko-queue-processor-");
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);
        executor.setQueueCapacity(1024);
        executor.setKeepAliveSeconds((int) Duration.ofMinutes(1L).toSeconds());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return new ThreadPoolQueueExecutor(executor);
    }

    @Bean
    public EventPublisher eventPublisher(MeterRegistry registry) {
        return new QueueMetricsPublisher(registry);
    }

    @Bean
    @ConditionalOnProperty(prefix = KekioQueueProperties.PREFIX, value = "type", havingValue = "MEM", matchIfMissing = true)
    public InMemoryQueue inMemoryKekioQueue(
            QueueExecutor queueExecutor,
            Collection<MessageHandler<?>> handlers,
            List<Queue.DeadMessageCallback> deadMessageHandlers,
            EventPublisher eventPublisher,
            MeterRegistry meterRegistry
    ) {
        return new InMemoryQueue(
                queueExecutor,
                handlers,
                deadMessageHandlers,
                eventPublisher,
                meterRegistry,
                null,
                null,
                null,
                null,
                null);
    }

    @Bean
    @ConditionalOnBean(Pool.class)
    @ConditionalOnProperty(prefix = KekioQueueProperties.PREFIX, value = "type", havingValue = "JEDIS", matchIfMissing = true)
    public JedisQueue jedisKekioQueue(
            Pool<Jedis> jedisPool,
            QueueExecutor queueExecutor,
            Collection<MessageHandler<?>> handlers,
            List<Queue.DeadMessageCallback> deadMessageHandlers,
            EventPublisher eventPublisher,
            MeterRegistry meterRegistry
    ) {
        return new JedisQueue(
                jedisPool,
                properties.getName(),
                new ObjectMapper(),
                queueExecutor,
                handlers,
                deadMessageHandlers,
                eventPublisher,
                meterRegistry,
                null,
                null,
                null,
                null,
                null,
                null);
    }


    @Bean
    @ConditionalOnBean(JedisCluster.class)
    @ConditionalOnProperty(prefix = KekioQueueProperties.PREFIX, value = "type", havingValue = "JEDIS", matchIfMissing = true)
    public JedisClusterQueue jedisClusterKekioQueue(
            JedisCluster jedisCluster,
            QueueExecutor queueExecutor,
            Collection<MessageHandler<?>> handlers,
            List<Queue.DeadMessageCallback> deadMessageHandlers,
            EventPublisher eventPublisher,
            MeterRegistry meterRegistry
    ) {
        return new JedisClusterQueue(
                jedisCluster,
                properties.getName(),
                new ObjectMapper(),
                queueExecutor,
                handlers,
                deadMessageHandlers,
                eventPublisher,
                meterRegistry,
                null,
                null,
                null,
                null,
                null,
                null);
    }

}
