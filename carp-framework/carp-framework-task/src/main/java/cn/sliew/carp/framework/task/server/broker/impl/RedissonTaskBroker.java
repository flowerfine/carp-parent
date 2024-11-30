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
package cn.sliew.carp.framework.task.server.broker.impl;

import cn.sliew.carp.framework.task.server.broker.TaskBroker;
import cn.sliew.carp.framework.task.server.broker.TaskMessage;
import cn.sliew.carp.framework.task.server.detail.TaskDetail;
import cn.sliew.carp.framework.task.server.storage.StorageProvider;
import cn.sliew.carp.framework.task.server.storage.TaskResultStorage;
import cn.sliew.carp.framework.task.server.worker.impl.RedissonTaskWorker;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RScheduledExecutorService;
import org.redisson.api.RScheduledFuture;
import org.redisson.api.RedissonClient;
import org.redisson.api.WorkerOptions;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedissonTaskBroker implements TaskBroker {

    private RedissonClient client;
    private TaskStartedListenerImpl taskStartedListener;;
    private TaskFinishedListenerImpl taskFinishedListener;
    private TaskSuccessListenerImpl taskSuccessListener;
    private TaskFailureListenerImpl taskFailureListener;

    private ConcurrentMap<String, RScheduledExecutorService> executorServiceMap = new ConcurrentHashMap<>();

    @Override
    public TaskMessage sendTask(String topic, TaskDetail taskDetail, Duration delay) {
        RScheduledExecutorService executorService = executorServiceMap.computeIfAbsent(topic, key -> buildExecutorService(key));

        RScheduledFuture<?> future = executorService.schedule(new RedissonTaskWorker(topic, taskDetail), delay.toMillis(), TimeUnit.MILLISECONDS);
        TaskResultStorage taskResultStorage = getStorageProvider().getTaskResultStorage();
        // 写入任务结果
        return getTask(topic, future.getTaskId());
    }

    @Override
    public TaskMessage getTask(String topic, String id) {
        RScheduledExecutorService executorService = executorServiceMap.computeIfAbsent(topic, key -> buildExecutorService(key));
        // 读取任务结果
        TaskResultStorage taskResultStorage = getStorageProvider().getTaskResultStorage();

        return null;
    }

    @Override
    public TaskMessage deleteTask(String topic, String id) {
        RScheduledExecutorService executorService = client.getExecutorService(topic);
        executorService.cancelTask(id);
        TaskMessage taskMessage = getTask(topic, id);

        // 更新为被删除任务信息
        TaskResultStorage taskResultStorage = getStorageProvider().getTaskResultStorage();
        return taskMessage;
    }

    @Override
    public StorageProvider getStorageProvider() {
        return null;
    }

    private RScheduledExecutorService buildExecutorService(String topic) {
        RScheduledExecutorService executorService = client.getExecutorService(topic);
        WorkerOptions workerOptions = WorkerOptions.defaults()
                .workers(2)
                .taskTimeout(60, TimeUnit.SECONDS)
                        .addListener(taskStartedListener)
                        .addListener(taskFinishedListener)
                        .addListener(taskSuccessListener)
                        .addListener(taskFailureListener);
        executorService.registerWorkers(workerOptions);
        return executorService;
    }
}
