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
package cn.sliew.carp.framework.queue.kekio.metrics;

import cn.hutool.core.thread.ThreadUtil;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;

/**
 * publishes gauges based on regular polling of the queue state
 */
@Slf4j
public class QueueMonitor implements InitializingBean, DisposableBean {

    static final String METRICS_PREFIX = "carp.kekio.";

    private final MonitorableQueue queue;
    private final Iterable<Tag> tags;
    private final AtomicReference<MonitorableQueue.QueueState> _lastState;

    private ScheduledThreadPoolExecutor scheduledExecutor;

    public QueueMonitor(MeterRegistry registry, MonitorableQueue queue) {
        this.queue = queue;
        this.tags = Tags.of("queue", queue.getName());
        this._lastState = new AtomicReference<>(new MonitorableQueue.QueueState(0, 0, 0));

        // 设置各种监控指标
        registry.gauge(METRICS_PREFIX + "queue.depth", tags, this, monitor -> monitor.getLastState().getDepth());
        registry.gauge(METRICS_PREFIX + "queue.unacked.depth", tags, this, monitor -> monitor.getLastState().getUnacked());
        registry.gauge(METRICS_PREFIX + "queue.ready.depth", tags, this, monitor -> monitor.getLastState().getReady());
        registry.gauge(METRICS_PREFIX + "queue.orphaned.messages", tags, this, monitor -> monitor.getLastState().getOrphaned());
    }

    public MonitorableQueue.QueueState getLastState() {
        return _lastState.get();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        scheduledExecutor = ThreadUtil.createScheduledExecutor(1);
        ThreadUtil.schedule(scheduledExecutor, () -> pollQueueState(), 0, 30000L, false);
        log.debug("Start monitor queue: {}", queue.getClass().getSimpleName());
    }

    @Override
    public void destroy() throws Exception {
        if (Objects.nonNull(scheduledExecutor)) {
            scheduledExecutor.shutdown();
            log.debug("Stop monitor queue: {}", queue.getClass().getSimpleName());
        }
    }

    private void pollQueueState() {
        _lastState.set(queue.readState());
    }
}
