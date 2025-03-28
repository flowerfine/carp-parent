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
package cn.sliew.carp.framework.queue.kekio.redis;

import cn.sliew.carp.framework.queue.kekio.Queue;
import cn.sliew.carp.framework.queue.kekio.message.Message;
import cn.sliew.milky.common.util.JacksonUtil;
import redis.clients.jedis.commands.JedisCommands;

import java.time.Instant;
import java.util.function.Consumer;

/**
 * A dead message handler that writes messages to a sorted set with a score
 * representing the time the message was abandoned.
 */
public abstract class RedisDeadMessageHandler<CLIENT extends JedisCommands> implements Queue.DeadMessageCallback {

    private final String dlqKey;

    public RedisDeadMessageHandler(String deadLetterQueueName) {
        this.dlqKey = deadLetterQueueName + ".messages";
    }

    protected abstract void withJedis(Consumer<CLIENT> consumer);

    @Override
    public void accept(Queue queue, Message message) {
        withJedis(commands -> {
            double score = (double) Instant.now().toEpochMilli();
            commands.zadd(dlqKey, score, JacksonUtil.toJsonString(message));
        });
    }

}