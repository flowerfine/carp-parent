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
package cn.sliew.carp.framework.queue.kekio.memory;

import cn.hutool.core.thread.ThreadUtil;
import cn.sliew.carp.framework.common.util.UUIDUtil;
import cn.sliew.carp.framework.queue.kekio.AbstractQueue;
import cn.sliew.carp.framework.queue.kekio.MessageHandler;
import cn.sliew.carp.framework.queue.kekio.Queue;
import cn.sliew.carp.framework.queue.kekio.QueueExecutor;
import cn.sliew.carp.framework.queue.kekio.message.Message;
import cn.sliew.carp.framework.queue.kekio.metrics.EventPublisher;
import cn.sliew.carp.framework.queue.kekio.metrics.MonitorableQueue;
import cn.sliew.carp.framework.queue.kekio.metrics.QueueEvent;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

@Slf4j
public class InMemoryQueue extends AbstractQueue implements MonitorableQueue, InitializingBean, DisposableBean {

    private ScheduledThreadPoolExecutor scheduledExecutor;

    private final DelayQueue<Envelope> queue = new DelayQueue<>();
    private final DelayQueue<Envelope> unacked = new DelayQueue<>();

    public InMemoryQueue(QueueExecutor queueExecutor, Collection<MessageHandler<?>> handlers, List<DeadMessageCallback> deadMessageHandlers, EventPublisher eventPublisher, MeterRegistry meterRegistry, Boolean fillExecutorEachCycle, Duration requeueDelay, Duration requeueMaxJitter, Boolean canPollMany, TemporalAmount ackTimeout) {
        super(queueExecutor, handlers, deadMessageHandlers, eventPublisher, meterRegistry, fillExecutorEachCycle, requeueDelay, requeueMaxJitter, canPollMany, ackTimeout);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        scheduledExecutor = ThreadUtil.createScheduledExecutor(1);
        ThreadUtil.schedule(scheduledExecutor, () -> retry(), 0, 10000L, false);
        log.debug("Start process queue retry: {}", queue.getClass().getSimpleName());
    }

    @Override
    public void destroy() throws Exception {
        if (Objects.nonNull(scheduledExecutor)) {
            scheduledExecutor.shutdown();
            log.info("Stop process queue retry: {}", queue.getClass().getSimpleName());
        }
    }

    @Override
    public void poll(QueueCallback callback) {
        fire(QueueEvent.QueuePolled);

        Envelope envelope = queue.poll();
        if (envelope != null) {
            TemporalAmount messageAckTimeout = envelope.getPayload().getAckTimeoutMs() == null
                    ? ackTimeout
                    : Duration.ofMillis(envelope.getPayload().getAckTimeoutMs());

            if (unacked.stream().anyMatch(e -> Objects.equals(e.getPayload(), envelope.getPayload()))) {
                queue.put(envelope);
            } else {
                unacked.put(new Envelope(envelope.getId(), envelope.getPayload(), Instant.now().plus(messageAckTimeout), envelope.getCount()));
                fire(new QueueEvent.MessageProcessing(envelope.getPayload(), envelope.getScheduledTime(), Instant.now()));
                callback.accept(envelope.getPayload(), () -> {
                    ack(envelope.getId());
                    fire(QueueEvent.MessageAcknowledged);
                });
            }
        }
    }

    @Override
    public void poll(int maxMessages, QueueCallback callback) {
        poll(callback);
    }

    @Override
    public void push(Message message, TemporalAmount delay) {
        boolean existed = queue.removeIf(e -> Objects.equals(e.getPayload(), message));
        queue.put(new Envelope(message, Instant.now().plus(delay)));
        if (existed) {
            fire(new QueueEvent.MessageDuplicate(message));
        } else {
            fire(new QueueEvent.MessagePushed(message));
        }
    }

    @Override
    public void retry() {
        Instant now = Instant.now();
        fire(QueueEvent.RetryPolled);

        Envelope message;
        while ((message = unacked.poll()) != null) {
            Envelope messageVal = message;
            if (messageVal.getCount() >= Queue.MAX_RETRIES) {
                if (CollectionUtils.isNotEmpty(deadMessageHandlers)) {
                    for (DeadMessageCallback handler : deadMessageHandlers) {
                        handler.accept(this, messageVal.getPayload());
                    }
                }
                fire(QueueEvent.MessageDead);
            } else {
                boolean existed = queue.removeIf(e -> Objects.equals(e.getPayload(), messageVal.getPayload()));
                log.warn("Redelivering unacked message {}", messageVal.getPayload());
                queue.put(new Envelope(messageVal.getId(), messageVal.getPayload(), now, messageVal.getCount() + 1));
                if (existed) {
                    fire(new QueueEvent.MessageDuplicate(messageVal.getPayload()));
                } else {
                    fire(QueueEvent.MessageRetried);
                }
            }
        }
    }

    @Override
    public void clear() {
        queue.removeIf(s -> true);
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

    @Override
    public QueueState readState() {
        return new QueueState(
                queue.size(),
                (int) queue.stream().filter(e -> e.getDelay(TimeUnit.NANOSECONDS) <= 0).count(),
                unacked.size()
        );
    }

    @Override
    public boolean containsMessage(Predicate<Message> predicate) {
        return queue.stream()
                .map(Envelope::getPayload)
                .anyMatch(predicate);
    }

    private void ack(String messageId) {
        unacked.removeIf(e -> e.getId().equals(messageId));
    }

    @Getter
    @AllArgsConstructor
    public static class Envelope implements Delayed {

        private final String id;
        private final Message payload;
        private final Instant scheduledTime;
        private final int count;

        public Envelope(Message payload, Instant scheduledTime) {
            this(UUIDUtil.randomUUId(), payload, scheduledTime, 1);
        }

        @Override
        public int compareTo(Delayed other) {
            return Long.compare(getDelay(TimeUnit.MILLISECONDS), other.getDelay(TimeUnit.MILLISECONDS));
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return Instant.now().until(scheduledTime, unit.toChronoUnit());
        }
    }
}