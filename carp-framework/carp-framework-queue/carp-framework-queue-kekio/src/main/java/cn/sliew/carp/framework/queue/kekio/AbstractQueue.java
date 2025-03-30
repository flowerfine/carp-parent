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

import cn.sliew.carp.framework.queue.kekio.metrics.EventPublisher;
import cn.sliew.carp.framework.queue.kekio.metrics.MonitorableQueue;
import cn.sliew.carp.framework.queue.kekio.metrics.QueueMonitor;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Objects;

@Slf4j
public abstract class AbstractQueue extends AbstractLifecycle implements Queue {

    private final String name;
    private QueueExecutor queueExecutor;
    protected final List<DeadMessageCallback> deadMessageHandlers;
    protected final EventPublisher publisher;
    private MeterRegistry meterRegistry;
    private Boolean fillExecutorEachCycle;
    private Duration requeueDelay;
    private Duration requeueMaxJitter;
    protected final Boolean canPollMany;
    protected final TemporalAmount ackTimeout;

    @Getter
    private QueueProcessor processor;
    private QueueMonitor monitor;

    public AbstractQueue(
            String name,
            QueueExecutor queueExecutor,
            List<DeadMessageCallback> deadMessageHandlers,
            EventPublisher publisher,
            MeterRegistry meterRegistry,
            Boolean fillExecutorEachCycle,
            Duration requeueDelay,
            Duration requeueMaxJitter,
            Boolean canPollMany,
            TemporalAmount ackTimeout
    ) {
        this.name = name;
        this.queueExecutor = queueExecutor;
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
    protected void doStart() throws Exception {
        this.processor = new QueueProcessor(this,
                queueExecutor,
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
    }

    @Override
    protected void doStop() throws Exception {
        this.processor.destroy();
        if (Objects.nonNull(this.monitor)) {
            this.monitor.destroy();
        }
    }

    @Override
    public String getName() {
        return name;
    }
}
