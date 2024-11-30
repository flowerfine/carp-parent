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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MetricsThreadPoolExecutor extends ThreadPoolTaskExecutor {

    /**
     * 任务开始时间
     */
    private final ThreadLocal<Long> startTimeThreadLocal = new ThreadLocal<>();

    private MeterRegistry meterRegistry;

    private List<Tag> tags;

    @Override
    public void initialize() {
        super.initialize();
        enableMetrics(getThreadNamePrefix());
    }

    private void enableMetrics(String poolName) {
        this.tags = Collections.singletonList(Tag.of("pool_name", poolName));
        startWatch();
    }

    private void startWatch() {
        meterRegistry.gauge("job_thread_pool_active_thread_size",
                getTags(),
                this,
                threadPoolExecutor -> (double) threadPoolExecutor.getActiveCount());
        meterRegistry.gauge("job_thread_pool_pool_size",
                getTags(),
                this,
                threadPoolExecutor -> (double) threadPoolExecutor.getPoolSize());
        meterRegistry.gauge("job_thread_pool_core_pool_size",
                getTags(),
                this,
                threadPoolExecutor -> (double) threadPoolExecutor.getCorePoolSize());
        meterRegistry.gauge("job_thread_pool_max_pool_size",
                getTags(),
                this,
                threadPoolExecutor -> (double) threadPoolExecutor.getMaxPoolSize());
        meterRegistry.gauge("job_thread_pool_task_total",
                getTags(),
                this,
                threadPoolExecutor -> (double) threadPoolExecutor.getThreadPoolExecutor().getTaskCount());
        meterRegistry.gauge("job_thread_pool_completed_task_total",
                getTags(),
                this,
                threadPoolExecutor -> (double) threadPoolExecutor.getThreadPoolExecutor().getCompletedTaskCount());
        meterRegistry.gauge("job_thread_pool_queue_size",
                getTags(),
                this,
                threadPoolExecutor -> (double) threadPoolExecutor.getQueueSize());
        meterRegistry.gauge("job_thread_pool_queue_capacity",
                getTags(),
                this,
                threadPoolExecutor -> (double) threadPoolExecutor.getQueueCapacity());
    }

    private List<Tag> getTags() {
        return tags;
    }


    /**
     * 任务执行之前，记录任务开始时间
     */
    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        startTimeThreadLocal.set(System.currentTimeMillis());
    }

    /**
     * 任务执行之后，计算任务结束时间
     */
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        try {
            long costTime = System.currentTimeMillis() - startTimeThreadLocal.get();
            recordTaskCostTime(costTime);
        } finally {
            startTimeThreadLocal.remove();
        }
    }


    private void recordTaskCostTime(long costInMills) {
        try {
            Timer.builder("job_thread_pool_tasks")
                    .tags(getTags())
                    .publishPercentileHistogram(true)
                    .register(meterRegistry)
                    .record(costInMills, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.warn("Fail to record thread pool task timer metrics", e);
        }
    }


}
