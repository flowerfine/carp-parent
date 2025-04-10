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
package cn.sliew.carp.framework.log.realtime.poll;

import cn.hutool.core.thread.ThreadUtil;
import cn.sliew.carp.framework.log.realtime.configuration.RealtimeLogPollProperties;
import cn.sliew.milky.common.concurrent.RunnableWrapper;
import com.google.common.util.concurrent.RateLimiter;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.util.CloseableIterator;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class StreamPollerImpl implements StreamPoller {

    private final AtomicBoolean running = new AtomicBoolean(false);

    private final CloseableIterator iterator;
    private final BatchBlockingQueue buffer;

    private final PollTask pollTask;
    private final AsyncTaskExecutor taskExecutor;
    private CompletableFuture pollFuture;

    public StreamPollerImpl(
            @Nonnull CloseableIterator iterator,
            @Nonnull AsyncTaskExecutor taskExecutor,
            @Nonnull RealtimeLogPollProperties properties) {
        this.iterator = Objects.requireNonNull(iterator, "iterator");
        this.buffer = new BatchBlockingQueue(properties.getPollQueueCapacity());
        this.pollTask = new PollTask(properties.getPollRatePerSecond());
        this.taskExecutor = Objects.requireNonNull(taskExecutor, "taskExecutor");
    }

    @Override
    public <T> List<T> poll(int limit, Duration timeout) {
        if (!isRunning()) {
            pollFuture = taskExecutor.submitCompletable(pollTask);
        }

        if (pollFuture.isCompletedExceptionally()) {
            throw new RuntimeException("Failed to poll result", pollTask.exception);
        }

        try {
            return buffer.pollBatch(limit, timeout);
        } catch (InterruptedException e) {
            throw new RuntimeException("Poll thread was interrupted.");
        }
    }

    @Override
    public void close() throws Exception {
        if (Objects.nonNull(pollFuture) && !pollFuture.isDone()) {
            pollFuture.cancel(true);
        }
        pollTask.close();
        iterator.close();
    }

    private boolean isRunning() {
        return running.get();
    }

    private class PollTask implements RunnableWrapper {

        private volatile boolean started = true;
        private final RateLimiter rateLimiter;
        @Nullable
        private volatile Exception exception;

        public PollTask(double pollRatePerSecond) {
            this.rateLimiter = RateLimiter.create(pollRatePerSecond);
        }

        @Override
        public void doRun() throws Exception {
            while (started) {
                rateLimiter.acquire();
                if (!iterator.hasNext()) {
                    ThreadUtil.sleep(100L);
                } else {
                    buffer.put(iterator.next());
                }
            }
        }

        @Override
        public void onFailure(Exception e) {
            exception = e;
            pollFuture.completeExceptionally(e);
        }

        public void close() {
            started = false;
        }
    }
}
