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
package cn.sliew.carp.framework.spring.concurrent;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.core.task.TaskDecorator;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MetricsTaskDecorator implements TaskDecorator {

    private List<Tag> tags;
    private MeterRegistry meterRegistry;

    public MetricsTaskDecorator(List<Tag> tags, MeterRegistry meterRegistry) {
        this.tags = tags;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Runnable decorate(Runnable runnable) {
        return new JobMetricsRunnable(runnable);
    }

    private class JobMetricsRunnable implements Runnable {

        private final Runnable task;

        private JobMetricsRunnable(Runnable task) {
            this.task = task;
        }

        @Override
        public void run() {
            try {
                StopWatch stopWatch = StopWatch.createStarted();
                task.run();
                stopWatch.stop();
                recordTaskCostTime(stopWatch.getTime(TimeUnit.MILLISECONDS));
            } catch (Throwable e) {
                log.error("failed to run! task: {}", task.toString(), e);
            }
        }

        private void recordTaskCostTime(long costInMills) {
            if (meterRegistry != null) {
                try {
                    Timer.builder("job_thread_pool_tasks")
                            .tags(tags)
                            .publishPercentileHistogram(true)
                            .register(meterRegistry)
                            .record(costInMills, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    log.warn("Fail to record thread pool task timer metrics", e);
                }
            }
        }

        @Override
        public int hashCode() {
            return task.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return task.equals(obj);
        }

        @Override
        public String toString() {
            return "[task-executor-wrapper] task: " + task.toString();
        }
    }
}
