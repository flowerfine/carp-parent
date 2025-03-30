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

import cn.sliew.carp.framework.queue.kekio.QueueExecutor;
import cn.sliew.carp.framework.queue.kekio.metrics.EventPublisher;
import cn.sliew.milky.common.function.CheckedConsumer;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.util.Pool;

import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.function.Function;

@Slf4j
public class JedisQueue extends RedisQueue<Jedis> {

    private final Pool<Jedis> pool;

    public JedisQueue(Pool<Jedis> pool, ObjectMapper mapper, String name, QueueExecutor queueExecutor, List<DeadMessageCallback> deadMessageHandlers, EventPublisher publisher, MeterRegistry meterRegistry, Boolean fillExecutorEachCycle, Duration requeueDelay, Duration requeueMaxJitter, Boolean canPollMany, TemporalAmount ackTimeout, Integer lockTtlSeconds) {
        super(mapper, name, queueExecutor, deadMessageHandlers, publisher, meterRegistry, fillExecutorEachCycle, requeueDelay, requeueMaxJitter, canPollMany, ackTimeout, lockTtlSeconds);
        this.pool = pool;

        cacheScript();
        log.info("Configured {} queue: {}", getClass().getName(), name);
    }

    @Override
    public void cacheScript() {
        try (Jedis redis = pool.getResource()) {
            setReadMessageWithLockScriptSha(redis.scriptLoad(READ_MESSAGE_WITH_LOCK_SRC));
        }
    }

    @Override
    protected <T> T withJedis(Function<Jedis, T> function) {
        try (Jedis jedis = pool.getResource()) {
            return function.apply(jedis);
        }
    }

    @Override
    protected <E extends Throwable> List<Object> multi(CheckedConsumer<Transaction, E> block) {
        try (Jedis jedis = pool.getResource();
             Transaction tx = jedis.multi()) {
            block.accept(tx);
            return tx.exec();
        } catch (Throwable e) {
            throw new RuntimeException("Failed to execute Redis transaction", e);
        }
    }
}