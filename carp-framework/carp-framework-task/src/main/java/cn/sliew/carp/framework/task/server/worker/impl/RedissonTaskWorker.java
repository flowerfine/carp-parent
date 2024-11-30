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
package cn.sliew.carp.framework.task.server.worker.impl;

import cn.sliew.carp.framework.task.TaskExecutor;
import cn.sliew.carp.framework.task.server.detail.TaskDetail;
import cn.sliew.carp.framework.task.server.handler.TaskHandler;
import cn.sliew.carp.framework.task.server.worker.TaskWorker;
import cn.sliew.milky.common.concurrent.RunnableWrapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.redisson.api.annotation.RInject;

@Slf4j
public class RedissonTaskWorker implements TaskWorker, RunnableWrapper {

    private RedissonClient client;
    private TaskHandler taskHandler;
    private TaskExecutor executor;

    private String topic;
    private TaskDetail taskDetail;

    @RInject
    private String taskId;

    public RedissonTaskWorker(String topic, TaskDetail taskDetail) {
        this.topic = topic;
        this.taskDetail = taskDetail;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void onBefore() throws Exception {
        // 保存任务状态
    }

    @Override
    public void doRun() throws Exception {
        // 执行 executor
        log.info("[}", taskDetail);
    }

    @Override
    public void onAfter() throws Exception {
        // 记录任务成功
    }

    @Override
    public void onFailure(Exception e) {
        // 记录任务失败
    }

    @Override
    public void onFinal() {
        // 最后根据任务执行结果
        // 成功，标记任务结束
        // 失败，标记任务失败，如果支持重试，则进行重试
    }

}
