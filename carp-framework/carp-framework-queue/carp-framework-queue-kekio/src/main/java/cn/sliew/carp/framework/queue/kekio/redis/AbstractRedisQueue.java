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

import cn.sliew.carp.framework.queue.kekio.AbstractQueue;
import cn.sliew.carp.framework.queue.kekio.MessageHandler;
import cn.sliew.carp.framework.queue.kekio.QueueExecutor;
import cn.sliew.carp.framework.queue.kekio.message.Message;
import cn.sliew.carp.framework.queue.kekio.metrics.EventPublisher;
import cn.sliew.carp.framework.queue.kekio.metrics.MonitorableQueue;
import cn.sliew.milky.common.function.CheckedConsumer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.hash.Hashing;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.commands.JedisCommands;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public abstract class AbstractRedisQueue<CLIENT extends JedisCommands> extends AbstractQueue implements MonitorableQueue {

    protected final Integer lockTtlSeconds;
    protected final ObjectMapper mapper;
    // Internal ObjectMapper that enforces deterministic property ordering for use only in hashing.
    private final ObjectMapper hashObjectMapper;

    protected abstract String getQueueKey();

    protected abstract String getUnackedKey();

    protected abstract String getMessagesKey();

    protected abstract String getLocksKey();

    protected abstract String getAttemptsKey();

    public abstract void cacheScript();

    protected abstract String getReadMessageWithLockScriptSha();

    protected abstract void setReadMessageWithLockScriptSha(String sha);

    protected abstract <T> T withJedis(Function<CLIENT, T> function);

    protected void withJedis(Consumer<CLIENT> consumer) {
        withJedis(commands -> {
            consumer.accept(commands);
            return null;
        });
    }

    protected AbstractRedisQueue(ObjectMapper mapper, QueueExecutor queueExecutor, Collection<MessageHandler<?>> handlers, List<DeadMessageCallback> deadMessageHandlers, EventPublisher publisher, MeterRegistry meterRegistry, Boolean fillExecutorEachCycle, Duration requeueDelay, Duration requeueMaxJitter, Boolean canPollMany, TemporalAmount ackTimeout, Integer lockTtlSeconds) {
        super(queueExecutor, handlers, deadMessageHandlers, publisher, meterRegistry, fillExecutorEachCycle, requeueDelay, requeueMaxJitter, canPollMany, ackTimeout);
        this.lockTtlSeconds = Objects.nonNull(lockTtlSeconds) ? lockTtlSeconds : 10;
        this.mapper = mapper;

        this.hashObjectMapper = mapper.copy();
        this.hashObjectMapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
    }

    @Override
    public TemporalAmount getAckTimeout() {
        return ackTimeout;
    }

    @Override
    public List<DeadMessageCallback> getDeadMessageHandlers() {
        return deadMessageHandlers;
    }

    @Override
    public Boolean canPollMany() {
        return canPollMany;
    }

    @Override
    public EventPublisher getPublisher() {
        return publisher;
    }

    protected void handleDeadMessage(Message message) {
        if (CollectionUtils.isNotEmpty(deadMessageHandlers)) {
            deadMessageHandlers.forEach(callback -> callback.accept(this, message));
        } else {
            try {
                log.error("Handle dead message error, empty deadMessageHandlers. message: {}", SerDerUtil.serializeAsJsonString(mapper, message));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * @return current time (plus optional delay) converted to a score for a
     * Redis sorted set.
     */
    protected double score() {
        return score(Duration.ZERO);
    }

    protected double score(TemporalAmount delay) {
        delay = Objects.nonNull(delay) ? delay : Duration.ZERO;
        return Instant.now().plus(delay).toEpochMilli();
    }

    protected abstract <E extends Throwable> List<Object> multi(CheckedConsumer<Transaction, E> block);

    protected int hgetInt(String key, String field) {
        return hgetInt(key, field, 0);
    }

    protected int hgetInt(String key, String field, int defaultValue) {
        return withJedis(commands -> {
            String value = commands.hget(key, field);
            return value != null ? Integer.parseInt(value) : defaultValue;
        });
    }

    protected boolean zismember(JedisCommands commands, String key, String member) {
        return commands.zrank(key, member) != null;
    }

    protected boolean anyZismember(JedisCommands commands, String key, Set<String> members) {
        return members.stream().anyMatch(member -> zismember(commands, key, member));
    }

    protected String firstFingerprint(String key, Fingerprint fingerprint) {
        return withJedis(commands -> {
            return fingerprint.getAll().stream()
                    .filter(fp -> zismember(commands, key, fp))
                    .findFirst().orElse(null);
        });
    }

    /**
     * @deprecated Hashes the attributes property, which is mutable
     */
    @Deprecated
    protected String hashV1(Message message) {
        try {
            return Hashing
                    .murmur3_128()
                    .hashString(SerDerUtil.serializeAsJsonString(mapper, message), Charset.defaultCharset())
                    .toString();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    protected String hashV2(Message message) {
        try {
            @SuppressWarnings("unchecked")
            HashMap<String, Object> map = hashObjectMapper.convertValue(message, HashMap.class);
            map.remove("attributes");
            return Hashing
                    .murmur3_128()
                    .hashString("v2:" + SerDerUtil.serializeAsJsonString(hashObjectMapper, map), StandardCharsets.UTF_8)
                    .toString();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    protected Fingerprint fingerprint(Message message) {
        String hashV2 = hashV2(message);
        Set<String> all = new HashSet<>();
        all.add(hashV2);
        all.add(hashV1(message));
        return new Fingerprint(hashV2, all);
    }

    @Getter
    @EqualsAndHashCode
    @AllArgsConstructor
    protected static class Fingerprint {
        private final String latest;
        private final Set<String> all;
    }

    protected static final String READ_MESSAGE_SRC =
            """
                    local java_scientific = function(x)
                      return string.format("%.12E", x):gsub("\\+", "")
                    end
                                
                    -- get the message, move the fingerprint to the unacked queue and return
                    local message = redis.call("HGET", messagesKey, fingerprint)
                                
                    -- check for an ack timeout override on the message
                    local unackScore = unackDefaultScore
                    if type(message) == "string" and message ~= nil then
                      local ackTimeoutOverride = tonumber(cjson.decode(message)["ackTimeoutMs"])
                      if ackTimeoutOverride ~= nil and unackBaseScore ~= nil then
                        unackScore = unackBaseScore + ackTimeoutOverride
                      end
                    end
                                
                    unackScore = java_scientific(unackScore)
                                
                    redis.call("ZREM", queueKey, fingerprint)
                    redis.call("ZADD", unackKey, unackScore, fingerprint)
                    """;

    protected static final String READ_MESSAGE_WITH_LOCK_SRC =
            """
                    local queueKey = KEYS[1]
                    local unackKey = KEYS[2]
                    local lockKey = KEYS[3]
                    local messagesKey = KEYS[4]
                    local maxScore = ARGV[1]
                    local peekFingerprintCount = ARGV[2]
                    local lockTtlSeconds = ARGV[3]
                    local unackDefaultScore = ARGV[4]
                    local unackBaseScore = ARGV[5]
                                
                    local not_empty = function(x)
                      return (type(x) == "table") and (not x.err) and (#x ~= 0)
                    end
                                
                    local acquire_lock = function(fingerprints, locksKey, lockTtlSeconds)
                      if not_empty(fingerprints) then
                        local i=1
                        while (i <= #fingerprints) do
                          redis.call("ECHO", "attempting lock on " .. fingerprints[i])
                          if redis.call("SET", locksKey .. ":" .. fingerprints[i], "\\uD83D\\uDD12", "EX", lockTtlSeconds, "NX") then
                            redis.call("ECHO", "acquired lock on " .. fingerprints[i])
                            return fingerprints[i], fingerprints[i+1]
                          end
                          i=i+2
                        end
                      end
                      return nil, nil
                    end
                                
                    -- acquire a lock on a fingerprint
                    local fingerprints = redis.call("ZRANGEBYSCORE", queueKey, 0.0, maxScore, "WITHSCORES", "LIMIT", 0, peekFingerprintCount)
                    local fingerprint, fingerprintScore = acquire_lock(fingerprints, lockKey, lockTtlSeconds)
                                
                    -- no lock could be acquired
                    if fingerprint == nil then
                      if #fingerprints == 0 then
                        return "NoReadyMessages"
                      end
                      return "AcquireLockFailed"
                    end
                                        
                    """
                    + READ_MESSAGE_SRC +
                    """
                            return {fingerprint, fingerprintScore, message}
                            """;
}