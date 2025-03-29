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
package cn.sliew.carp.framework.pubsub.queue.kekio;

import cn.sliew.carp.framework.pubsub.model.AbstractPubsubChannelFactory;
import cn.sliew.carp.framework.pubsub.model.PubsubChannel;
import cn.sliew.carp.framework.pubsub.model.PubsubChannelFactory;
import cn.sliew.carp.framework.queue.kekio.Queue;
import cn.sliew.carp.framework.queue.kekio.QueueExecutor;
import cn.sliew.carp.framework.queue.kekio.metrics.EventPublisher;
import cn.sliew.carp.framework.queue.kekio.redis.JedisQueue;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import redis.clients.jedis.JedisPool;

import java.util.Collection;
import java.util.List;

@Slf4j
public class QueuePubsubChannelFactory extends AbstractPubsubChannelFactory implements PubsubChannelFactory {

    public static final String SYSTEM = "kekio-queue";

    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper;
    private final QueueExecutor queueExecutor;
    private final List<Queue.DeadMessageCallback> deadMessageHandlers;
    private final EventPublisher publisher;
    private final MeterRegistry meterRegistry;

    public QueuePubsubChannelFactory(
            JedisPool jedisPool,
            ObjectMapper objectMapper,
            QueueExecutor queueExecutor,
            List<Queue.DeadMessageCallback> deadMessageHandlers,
            EventPublisher publisher,
            MeterRegistry meterRegistry
    ) {
        this.jedisPool = jedisPool;
        this.objectMapper = objectMapper;
        this.queueExecutor = queueExecutor;
        this.deadMessageHandlers = deadMessageHandlers;
        this.publisher = publisher;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public String getSystem() {
        return SYSTEM;
    }

    @Override
    protected void doStart() throws Exception {

    }

    @Override
    protected void doStop() throws Exception {
        Collection<PubsubChannel> pubsubChannels = getAll();
        if (CollectionUtils.isNotEmpty(pubsubChannels)) {
            for (PubsubChannel pubsubChannel : pubsubChannels) {
                try {
                    pubsubChannel.stop();
                } catch (Exception e) {
                    log.error("Stop pubsub channel error", e);
                }
            }
        }
    }

    @Override
    protected PubsubChannel doCreate(String name) {
        JedisQueue jedisQueue = new JedisQueue(
                jedisPool,
                name,
                objectMapper,
                queueExecutor,
                deadMessageHandlers,
                publisher,
                meterRegistry,
                null,
                null,
                null,
                null,
                null,
                null);
        QueuePubsubChannel pubsubChannel = new QueuePubsubChannel(name, jedisQueue);
        pubsubChannel.start();
        return pubsubChannel;
    }
}
