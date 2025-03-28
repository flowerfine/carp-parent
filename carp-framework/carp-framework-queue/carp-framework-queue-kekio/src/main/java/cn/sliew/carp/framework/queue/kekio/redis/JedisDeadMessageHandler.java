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

import redis.clients.jedis.Jedis;
import redis.clients.jedis.util.Pool;

import java.util.function.Consumer;

/**
 * A dead message handler that writes messages to a sorted set with a score
 * representing the time the message was abandoned.
 */
public class JedisDeadMessageHandler extends RedisDeadMessageHandler<Jedis> {

    private final Pool<Jedis> pool;

    public JedisDeadMessageHandler(String deadLetterQueueName, Pool<Jedis> pool) {
        super(deadLetterQueueName);
        this.pool = pool;
    }

    @Override
    protected void withJedis(Consumer<Jedis> consumer) {
        try (Jedis jedis = pool.getResource()) {
            consumer.accept(jedis);
        }
    }
}