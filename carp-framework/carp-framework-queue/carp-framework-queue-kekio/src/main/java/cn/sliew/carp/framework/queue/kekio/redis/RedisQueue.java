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

import cn.sliew.carp.framework.common.util.KeyUtil;
import cn.sliew.carp.framework.queue.kekio.Queue;
import cn.sliew.carp.framework.queue.kekio.QueueExecutor;
import cn.sliew.carp.framework.queue.kekio.message.AttemptsAttribute;
import cn.sliew.carp.framework.queue.kekio.message.MaxAttemptsAttribute;
import cn.sliew.carp.framework.queue.kekio.message.Message;
import cn.sliew.carp.framework.queue.kekio.metrics.EventPublisher;
import cn.sliew.carp.framework.queue.kekio.metrics.QueueEvent;
import cn.sliew.milky.common.util.JacksonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.scheduling.annotation.Scheduled;
import redis.clients.jedis.commands.JedisCommands;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.params.ZAddParams;
import redis.clients.jedis.resps.ScanResult;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Slf4j
public abstract class RedisQueue<CLIENT extends JedisCommands> extends AbstractRedisQueue<CLIENT> {

    private final String queueName;

    private final String queueKey;
    private final String unackedKey;
    private final String messagesKey;
    private final String locksKey;
    private final String attemptsKey;

    private String readMessageWithLockScriptSha;

    public RedisQueue(String queueName, ObjectMapper mapper, QueueExecutor queueExecutor, List<DeadMessageCallback> deadMessageHandlers, EventPublisher publisher, MeterRegistry meterRegistry, Boolean fillExecutorEachCycle, Duration requeueDelay, Duration requeueMaxJitter, Boolean canPollMany, TemporalAmount ackTimeout, Integer lockTtlSeconds) {
        super(mapper, queueExecutor, deadMessageHandlers, publisher, meterRegistry, fillExecutorEachCycle, requeueDelay, requeueMaxJitter, canPollMany, ackTimeout, lockTtlSeconds);
        this.queueName = queueName;

        this.queueKey = KeyUtil.buildKey("kekio-queue.v1",queueName, "queue");
        this.unackedKey = KeyUtil.buildKey("kekio-queue.v1",queueName, "unacked");
        this.messagesKey = KeyUtil.buildKey("kekio-queue.v1",queueName, "messages");
        this.locksKey = KeyUtil.buildKey("kekio-queue.v1",queueName, "locks");
        this.attemptsKey =  KeyUtil.buildKey("kekio-queue.v1",queueName, "attempts");
    }

    @Override
    protected String getQueueKey() {
        return queueKey;
    }

    @Override
    protected String getUnackedKey() {
        return unackedKey;
    }

    @Override
    protected String getMessagesKey() {
        return messagesKey;
    }

    @Override
    protected String getLocksKey() {
        return locksKey;
    }

    @Override
    protected String getAttemptsKey() {
        return attemptsKey;
    }

    @Override
    protected String getReadMessageWithLockScriptSha() {
        return readMessageWithLockScriptSha;
    }

    @Override
    protected void setReadMessageWithLockScriptSha(String sha) {
        this.readMessageWithLockScriptSha = sha;
    }

    @Override
    public void poll(QueueCallback callback) {
        Triple<String, Instant, String> result = readMessageWithLock();
        if (result != null) {
            String fingerprint = result.getLeft();
            Instant scheduledTime = result.getMiddle();
            String json = result.getRight();

            Runnable ack = () -> ackMessage(fingerprint);
            readMessage(fingerprint, json, message -> {
                AttemptsAttribute attemptsAttr = message.getAttribute(AttemptsAttribute.class);
                int attempts = attemptsAttr != null ? attemptsAttr.getAttempts() : 0;

                MaxAttemptsAttribute maxAttemptsAttr = message.getAttribute(MaxAttemptsAttribute.class);
                int maxAttempts = maxAttemptsAttr != null ? maxAttemptsAttr.getMaxAttempts() : 0;

                if (maxAttempts > 0 && attempts > maxAttempts) {
                    log.warn("Message {} with payload {} exceeded {} retries", fingerprint, message, maxAttempts);
                    handleDeadMessage(message);
                    removeMessage(fingerprint);
                    fire(QueueEvent.MessageDead);
                } else {
                    fire(new QueueEvent.MessageProcessing(message, scheduledTime, Instant.now()));
                    callback.accept(message, ack);
                }
            });
        }
        fire(QueueEvent.QueuePolled);
    }

    @Override
    public void poll(int maxMessages, QueueCallback callback) {
        poll(callback);
    }

    @Override
    public void push(Message message, TemporalAmount delay) {
        withJedis(commands -> {
            Fingerprint fingerprint = fingerprint(message);
            String existingFingerprint = firstFingerprint(queueKey, fingerprint);

            if (existingFingerprint != null) {
                log.info(
                        "Re-prioritizing message as an identical one is already on the queue: " +
                                "{}, message: {}", existingFingerprint, message
                );
                commands.zadd(queueKey, score(delay), existingFingerprint, ZAddParams.zAddParams().xx());
                fire(new QueueEvent.MessageDuplicate(message));
            } else {
                queueMessage(message, delay);
                fire(new QueueEvent.MessagePushed(message));
            }
        });
    }

    @Scheduled(fixedDelayString = "${queue.retry.frequency.ms:10000}")
    @Override
    public void retry() {
        withJedis(commands -> {
            List<String> fingerprints = commands.zrangeByScore(unackedKey, 0.0, score());

            if (CollectionUtils.isNotEmpty(fingerprints)) {
                String[] lockKeys = fingerprints.stream()
                        .map(fp -> locksKey + ":" + fp)
                        .toArray(String[]::new);
                commands.del(lockKeys);
            }

            for (String fingerprint : fingerprints) {
                int attempts = hgetInt(attemptsKey, fingerprint);
                readMessageWithoutLock(fingerprint, message -> {
                    MaxAttemptsAttribute maxAttemptsAttr = message.getAttribute(MaxAttemptsAttribute.class);
                    int maxAttempts = maxAttemptsAttr != null ? maxAttemptsAttr.getMaxAttempts() : 0;

                /* If maxAttempts attribute is set, let poll() handle max retry logic.
                   If not, check for attempts >= Queue.maxRetries - 1, as attemptsKey is now
                   only incremented when retrying unacked messages vs. by readMessage*() */
                    if (maxAttempts == 0 && attempts >= Queue.MAX_RETRIES - 1) {
                        log.warn("Message {} with payload {} exceeded max retries", fingerprint, message);
                        handleDeadMessage(message);
                        removeMessage(fingerprint);
                        fire(QueueEvent.MessageDead);
                    } else {
                        if (zismember(commands, queueKey, fingerprint)) {
                            multi(tx -> {
                                tx.zrem(unackedKey, fingerprint);
                                tx.zadd(queueKey, score(), fingerprint);
                                tx.hincrBy(attemptsKey, fingerprint, 1L);
                            });
                            log.info(
                                    "Not retrying message {} because an identical message " +
                                            "is already on the queue", fingerprint
                            );
                            fire(new QueueEvent.MessageDuplicate(message));
                        } else {
                            log.warn("Retrying message {} after {} attempts", fingerprint, attempts);
                            commands.hincrBy(attemptsKey, fingerprint, 1L);
                            requeueMessage(fingerprint);
                            fire(QueueEvent.MessageRetried);
                        }
                    }
                });
            }

            fire(QueueEvent.RetryPolled);
        });
    }


    @Override
    public QueueState readState() {
        List<Object> response = multi(tx -> {
            tx.zcard(queueKey);
            tx.zcount(queueKey, 0.0, score());
            tx.zcard(unackedKey);
            tx.hlen(messagesKey);
        });


        int queued = ((Long) response.get(0)).intValue();
        int ready = ((Long) response.get(1)).intValue();
        int processing = ((Long) response.get(2)).intValue();
        int messages = ((Long) response.get(3)).intValue();

        return new QueueState(queued, ready, processing, messages - (queued + processing), 0);
    }

    @Override
    public boolean containsMessage(Predicate<Message> predicate) {
        return withJedis(commands -> {
            String cursor = "0";
            boolean found = false;

            while (!found) {
                ScanResult<Map.Entry<String, String>> scanResult = commands.hscan(messagesKey, cursor);

                for (Map.Entry<String, String> entry : scanResult.getResult()) {
                    try {
                        Message message = JacksonUtil.parseJsonString(entry.getValue(), Message.class);
                        if (predicate.test(message)) {
                            found = true;
                            break;
                        }
                    } catch (Exception e) {
                        log.error("Failed to read message {}", entry.getKey(), e);
                    }
                }

                cursor = scanResult.getCursor();
                if (cursor.equals("0")) {
                    break;
                }
            }

            return found;
        });
    }

    protected void queueMessage(Message message, TemporalAmount delay) {
        TemporalAmount nonnullDelay = delay != null ? delay : Duration.ZERO;

        String fingerprint = fingerprint(message).getLatest();

        // ensure the message has the attempts tracking attribute
        AttemptsAttribute attemptsAttr = message.getAttribute(AttemptsAttribute.class);
        if (attemptsAttr == null) {
            attemptsAttr = new AttemptsAttribute(0);
            message.setAttribute(attemptsAttr);
        }

        try {
            multi(tx -> {
                tx.hset(messagesKey, fingerprint, SerDerUtil.serializeAsJsonString(mapper, message));
                tx.zadd(queueKey, score(nonnullDelay), fingerprint);
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to queue message: " + message, e);
        }
    }

    protected void requeueMessage(String fingerprint) {
        multi(tx -> {
            tx.zrem(unackedKey, fingerprint);
            tx.zadd(queueKey, score(), fingerprint);
        });
    }

    protected void removeMessage(String fingerprint) {
        multi(tx -> {
            tx.zrem(queueKey, fingerprint);
            tx.zrem(unackedKey, fingerprint);
            tx.hdel(messagesKey, fingerprint);
            tx.del(locksKey + ":" + fingerprint);
            tx.hdel(attemptsKey, fingerprint);
        });
    }

    protected void readMessageWithoutLock(String fingerprint, Consumer<Message> block) {
        withJedis(commands -> {
            try {
                String json = commands.hget(messagesKey, fingerprint);
                if (json != null) {
                    Message message = SerDerUtil.deserializeFromJsonString(mapper, json, Message.class);
                    block.accept(message);
                }
            } catch (JsonProcessingException e) {
                log.error("Payload for unacked message {} is missing or corrupt", fingerprint, e);
                removeMessage(fingerprint);
            } catch (Exception e) {
                // fixme 会无限地重试，直到成功
                log.error("Failed to read unacked message {}, requeuing...", fingerprint, e);
                commands.hincrBy(attemptsKey, fingerprint, 1L);
                requeueMessage(fingerprint);
            }
        });
    }

    protected Triple<String, Instant, String> readMessageWithLock() {
        return withJedis(commands -> {
            try {
                Object response = commands.evalsha(
                        readMessageWithLockScriptSha,
                        Arrays.asList(queueKey, unackedKey, locksKey, messagesKey),
                        Arrays.asList(
                                String.valueOf(score()),
                                "10", // TODO rz - make this configurable.
                                String.valueOf(lockTtlSeconds),
                                String.format(Locale.US, "%f", score(getAckTimeout())),
                                String.format(Locale.US, "%f", score())
                        )
                );

                if (response instanceof List result) {
                    if (result.size() >= 3) {
                        return Triple.of(
                                result.get(0).toString(), // fingerprint
                                Instant.ofEpochMilli(Long.parseLong(result.get(1).toString())), // fingerprintScore
                                result.get(2) != null ? result.get(2).toString() : null // message
                        );
                    }
                }

                if (Objects.equals(response, "ReadLockFailed")) {
                    // This isn't a "bad" thing, but means there's more work than keiko can process in a cycle
                    // in this case, but may be a signal to tune `peekFingerprintCount`
                    fire(QueueEvent.LockFailed);
                }
            } catch (JedisDataException e) {
                if ((e.getMessage() != null && e.getMessage().startsWith("NOSCRIPT"))) {
                    cacheScript();
                    return readMessageWithLock();
                }
                throw e;
            }
            return null;
        });
    }


    protected void readMessage(String fingerprint, String json, Consumer<Message> block) {
        withJedis(commands -> {
            if (json == null) {
                log.error("Payload for message {} is missing", fingerprint);
                // clean up what is essentially an unrecoverable message
                removeMessage(fingerprint);
            } else {
                try {
                    Message message = SerDerUtil.deserializeFromJsonString(mapper, json, Message.class);

                    // Apply the attempts attribute
                    AttemptsAttribute currentAttempts = message.getAttribute(AttemptsAttribute.class);
                    if (currentAttempts == null) {
                        currentAttempts = new AttemptsAttribute(0);
                    }
                    AttemptsAttribute newAttempts = new AttemptsAttribute(currentAttempts.getAttempts() + 1);
                    message.setAttribute(newAttempts);

                    commands.hset(messagesKey, fingerprint, mapper.writeValueAsString(message));

                    block.accept(message);
                } catch (IOException e) {
                    log.error("Failed to read message {}, requeuing...", fingerprint, e);
                    commands.hincrBy(attemptsKey, fingerprint, 1L);
                    requeueMessage(fingerprint);
                }
            }
        });
    }

    private void ackMessage(String fingerprint) {
        withJedis(commands -> {
            if (zismember(commands, queueKey, fingerprint)) {
                // only remove this message from the unacked queue as a matching one has
                // been put on the main queue
                multi(tx -> {
                    tx.zrem(unackedKey, fingerprint);
                    tx.del(locksKey + ":" + fingerprint);
                });
            } else {
                removeMessage(fingerprint);
            }
            fire(QueueEvent.MessageAcknowledged);
        });
    }
}
