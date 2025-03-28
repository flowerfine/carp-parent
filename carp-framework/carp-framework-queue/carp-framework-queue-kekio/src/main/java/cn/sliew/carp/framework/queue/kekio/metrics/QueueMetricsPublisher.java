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

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class QueueMetricsPublisher implements EventPublisher {

    private final MeterRegistry registry;

    private final AtomicReference<Instant> _lastQueuePoll;
    private final AtomicReference<Instant> _lastRetryPoll;

    public QueueMetricsPublisher(MeterRegistry registry) {
        this.registry = registry;
        this._lastQueuePoll = new AtomicReference<>(Instant.now());
        this._lastRetryPoll = new AtomicReference<>(Instant.now());

        registry.gauge("queue.last.poll.age",
                this,
                self -> Duration.between(self.getLastQueuePoll(), Instant.now()).toMillis());

        registry.gauge("queue.last.retry.check.age",
                this,
                self -> Duration.between(self.getLastRetryPoll(), Instant.now()).toMillis());
    }

    @Override
    public void publishEvent(QueueEvent event) {
        if (event == QueueEvent.QueuePolled) {
            _lastQueuePoll.set(Instant.now());
        } else if (event instanceof QueueEvent.MessageProcessing mp) {
            registry.timer("queue.message.lag").record(mp.getLag().toMillis(), TimeUnit.MILLISECONDS);
        } else if (event == QueueEvent.RetryPolled) {
            _lastRetryPoll.set(Instant.now());
        } else if (event instanceof QueueEvent.MessagePushed) {
            getMessagePushedCounter().increment();
        } else if (event == QueueEvent.MessageAcknowledged) {
            getMessageAcknowledgedCounter().increment();
        } else if (event == QueueEvent.MessageRetried) {
            getMessageRetriedCounter().increment();
        } else if (event == QueueEvent.MessageDead) {
            getMessageDeadCounter().increment();
        } else if (event instanceof QueueEvent.MessageDuplicate md) {
            getMessageDuplicateCounter(md).increment();
        } else if (event == QueueEvent.LockFailed) {
            getLockFailedCounter().increment();
        } else if (event instanceof QueueEvent.MessageRescheduled) {
            getMessageRescheduledCounter().increment();
        } else if (event instanceof QueueEvent.MessageNotFound) {
            getMessageNotFoundCounter().increment();
        }
    }


    private Counter getMessagePushedCounter() {
        return registry.counter("queue.pushed.messages");
    }

    private Counter getMessageAcknowledgedCounter() {
        return registry.counter("queue.acknowledged.messages");
    }

    private Counter getMessageRetriedCounter() {
        return registry.counter("queue.retried.messages");
    }

    private Counter getMessageDeadCounter() {
        return registry.counter("queue.dead.messages");
    }

    private Counter getMessageDuplicateCounter(QueueEvent.MessageDuplicate event) {
        return registry.counter("queue.duplicate.messages",
                "messageType", event.getPayload().getClass().getSimpleName());
    }

    private Counter getLockFailedCounter() {
        return registry.counter("queue.lock.failed");
    }

    private Counter getMessageRescheduledCounter() {
        return registry.counter("queue.reschedule.succeeded");
    }

    private Counter getMessageNotFoundCounter() {
        return registry.counter("queue.message.notfound");
    }

    public Instant getLastQueuePoll() {
        return _lastQueuePoll.get();
    }

    public Instant getLastRetryPoll() {
        return _lastRetryPoll.get();
    }
}
