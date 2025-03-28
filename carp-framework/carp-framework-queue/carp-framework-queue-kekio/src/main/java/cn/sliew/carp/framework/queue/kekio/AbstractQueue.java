/*
 * Copyright 2017 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.sliew.carp.framework.queue.kekio;

import cn.sliew.carp.framework.queue.kekio.metrics.EventPublisher;
import cn.sliew.carp.framework.queue.kekio.metrics.MonitorableQueue;
import cn.sliew.carp.framework.queue.kekio.metrics.QueueMonitor;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public abstract class AbstractQueue implements Queue {

    private AtomicBoolean running = new AtomicBoolean(false);
    private ReentrantLock lifecycleLock = new ReentrantLock();

    private QueueExecutor queueExecutor;
    private Collection<MessageHandler<?>> handlers;
    protected final List<DeadMessageCallback> deadMessageHandlers;
    protected final EventPublisher publisher;
    private MeterRegistry meterRegistry;
    private Boolean fillExecutorEachCycle;
    private Duration requeueDelay;
    private Duration requeueMaxJitter;
    protected final Boolean canPollMany;
    protected final TemporalAmount ackTimeout;

    private QueueProcessor processor;
    private QueueMonitor monitor;

    public AbstractQueue(
            QueueExecutor queueExecutor,
            Collection<MessageHandler<?>> handlers,
            List<DeadMessageCallback> deadMessageHandlers,
            EventPublisher publisher,
            MeterRegistry meterRegistry,
            Boolean fillExecutorEachCycle,
            Duration requeueDelay,
            Duration requeueMaxJitter,
            Boolean canPollMany,
            TemporalAmount ackTimeout
    ) {
        this.queueExecutor = queueExecutor;
        this.handlers = handlers;
        this.deadMessageHandlers = deadMessageHandlers;
        this.publisher = publisher;
        this.meterRegistry = meterRegistry;
        this.fillExecutorEachCycle = Objects.nonNull(fillExecutorEachCycle) ? fillExecutorEachCycle : true;
        this.requeueDelay = Objects.nonNull(requeueDelay) ? requeueDelay : Duration.ofSeconds(0L);
        this.requeueMaxJitter = Objects.nonNull(requeueMaxJitter) ? requeueMaxJitter : Duration.ofSeconds(0L);
        this.canPollMany = Objects.nonNull(canPollMany) ? canPollMany : false;
        this.ackTimeout = Objects.nonNull(ackTimeout) ? ackTimeout : Duration.ofMinutes(1);
    }

    @Override
    public void start() {
        this.lifecycleLock.lock();
        try {
            if (!isRunning()) {
                this.processor = new QueueProcessor(this,
                        queueExecutor,
                        handlers,
                        publisher,
                        deadMessageHandlers,
                        fillExecutorEachCycle,
                        requeueDelay,
                        requeueMaxJitter
                );
                this.processor.afterPropertiesSet();
                if (this instanceof MonitorableQueue monitorableQueue) {
                    this.monitor = new QueueMonitor(meterRegistry, monitorableQueue);
                    this.monitor.afterPropertiesSet();
                }

                running.compareAndSet(false, true);
            }
        } catch (Exception e) {
            log.error("Start queue error", e);
        } finally {
            this.lifecycleLock.unlock();
        }
    }

    @Override
    public void stop() {
        if (isRunning()) {
            this.lifecycleLock.lock();
            try {
                this.processor.destroy();
                if (Objects.nonNull(this.monitor)) {
                    this.monitor.destroy();
                }
                running.compareAndSet(true, false);
            } catch (Exception e) {
                log.error("Stop queue error", e);
            } finally {
                this.lifecycleLock.unlock();
            }
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

}
