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
package cn.sliew.carp.framework.pubsub.queue.kekio.configuration;

import cn.sliew.carp.framework.pubsub.model.PubsubChannelFactory;
import cn.sliew.carp.framework.pubsub.queue.kekio.QueuePubsubChannelFactory;
import cn.sliew.carp.framework.queue.kekio.Queue;
import cn.sliew.carp.framework.queue.kekio.QueueExecutor;
import cn.sliew.carp.framework.queue.kekio.configuration.KekioObjectMapperConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import redis.clients.jedis.JedisPool;

import java.util.List;

@AutoConfigureAfter(KekioObjectMapperConfiguration.class)
public class QueuePubsubAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(PubsubChannelFactory.class)
    public QueuePubsubChannelFactory queuePubsubChannelFactory(
            JedisPool jedisPool,
            ObjectMapper objectMapper,
            QueueExecutor queueExecutor,
            List<Queue.DeadMessageCallback> deadMessageHandlers,
            MeterRegistry meterRegistry
    ) {
        return new QueuePubsubChannelFactory(
                jedisPool,
                objectMapper,
                queueExecutor,
                deadMessageHandlers,
                meterRegistry);
    }
}
