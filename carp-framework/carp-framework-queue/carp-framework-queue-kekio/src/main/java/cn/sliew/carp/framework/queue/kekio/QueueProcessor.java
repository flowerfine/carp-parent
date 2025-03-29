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
package cn.sliew.carp.framework.queue.kekio;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.sliew.carp.framework.queue.kekio.message.AttemptsAttribute;
import cn.sliew.carp.framework.queue.kekio.message.Message;
import cn.sliew.carp.framework.queue.kekio.metrics.EventPublisher;
import cn.sliew.carp.framework.queue.kekio.metrics.QueueEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * The processor that fetches messages from the {@link Queue} and hands them off to
 * the appropriate {@link MessageHandler}.
 */
@Slf4j
public class QueueProcessor implements InitializingBean, DisposableBean {

    private Queue queue;
    private QueueExecutor<?> executor;
    private final Collection<MessageHandler> handlers;
    private EventPublisher publisher;
    private List<Queue.DeadMessageCallback> deadMessageHandlers;
    private Boolean fillExecutorEachCycle;
    private Duration requeueDelay;
    private Duration requeueMaxJitter;

    private final Random random = new Random();
    private final Map<Class<? extends Message>, MessageHandler> handlerCache = new HashMap<>();
    private ScheduledThreadPoolExecutor scheduledExecutor;

    public QueueProcessor(Queue queue,
                          QueueExecutor<?> executor,
                          EventPublisher publisher,
                          List<Queue.DeadMessageCallback> deadMessageHandlers,
                          Boolean fillExecutorEachCycle,
                          Duration requeueDelay,
                          Duration requeueMaxJitter) {
        this.queue = queue;
        this.executor = executor;
        this.handlers = new ArrayList<>(SpringUtil.getBeansOfType(MessageHandler.class).values());
        this.publisher = publisher;
        this.deadMessageHandlers = deadMessageHandlers;
        this.fillExecutorEachCycle = Objects.nonNull(fillExecutorEachCycle) ? fillExecutorEachCycle : true;
        this.requeueDelay = Objects.nonNull(requeueDelay) ? requeueDelay : Duration.ofSeconds(0L);
        this.requeueMaxJitter = Objects.nonNull(requeueMaxJitter) ? requeueMaxJitter : Duration.ofSeconds(0L);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        scheduledExecutor = ThreadUtil.createScheduledExecutor(1);
        ThreadUtil.schedule(scheduledExecutor, () -> poll(), 0, 50L, false);
        log.debug("Start process queue poll: {}", queue.getClass().getSimpleName());
    }

    @Override
    public void destroy() throws Exception {
        if (Objects.nonNull(scheduledExecutor)) {
            scheduledExecutor.shutdown();
            log.info("Stop process queue poll: {}", queue.getClass().getSimpleName());
        }
    }

    private void poll() {
        if (executor.hasCapacity()) {
            if (fillExecutorEachCycle) {
                if (queue.canPollMany()) {
                    queue.poll(executor.availableCapacity(), callback);
                } else {
                    for (int i = executor.availableCapacity(); i > 0; i--) {
                        pollOnce();
                    }
                }
            } else {
                pollOnce();
            }
        } else {
            publisher.publishEvent(QueueEvent.NoHandlerCapacity);
        }
    }

    private void pollOnce() {
        queue.poll(callback);
    }

    private final Queue.QueueCallback callback = (message, ack) -> {
        log.info("Received message {}", message);
        MessageHandler<?> handler = handlerFor(message);
        if (handler != null) {
            try {
                executor.execute(() -> {
                    try {
                        QueueContextHolder.set(message);
                        handler.invoke(message);
                        ack.run();
                    } catch (Throwable e) {
                        log.error("Unhandled throwable from {}", message, e);
                        publisher.publishEvent(new QueueEvent.HandlerThrewError(message));
                    } finally {
                        QueueContextHolder.clear();
                    }
                });
            } catch (RejectedExecutionException e) {
                long requeueDelaySeconds = requeueDelay.getSeconds();
                if (requeueMaxJitter.getSeconds() > 0) {
                    requeueDelaySeconds += random.nextInt((int) requeueMaxJitter.getSeconds());
                }

                Duration requeueDelay = Duration.ofSeconds(requeueDelaySeconds);
                AttemptsAttribute numberOfAttempts = message.getAttribute(AttemptsAttribute.class);

                log.warn("Executor at capacity, re-queuing message {} (delay: {}, attempts: {})", message, requeueDelay, numberOfAttempts, e);
                queue.push(message, requeueDelay);
            }
        } else {
            log.error("Unsupported message type {}: {}", message.getClass().getSimpleName(), message);
            if (CollectionUtils.isNotEmpty(deadMessageHandlers)) {
                for (Queue.DeadMessageCallback deadMessageHandler : deadMessageHandlers) {
                    deadMessageHandler.accept(queue, message);
                }
            }
            publisher.publishEvent(QueueEvent.MessageDead);
        }
    };

    private MessageHandler<?> handlerFor(Message message) {
        return handlerCache.computeIfAbsent(message.getClass(), key -> handlers.stream()
                .filter(handler -> handler.getMessageType().isAssignableFrom(key))
                .findFirst()
                .orElse(null));
    }
}