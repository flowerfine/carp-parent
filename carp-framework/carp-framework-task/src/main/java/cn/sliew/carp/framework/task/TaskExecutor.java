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
package cn.sliew.carp.framework.task;

import cn.sliew.carp.framework.task.listener.TaskListenerManager;
import cn.sliew.milky.common.concurrent.RunnableWrapper;

public class TaskExecutor implements RunnableWrapper {

    private Task task;
    private TaskContext context;
    private TaskListenerManager listenerManager;
    private TaskResult result;

    public TaskExecutor(Task task, TaskContext context, TaskListenerManager listenerManager, TaskResult result) {
        this.task = task;
        this.context = context;
        this.listenerManager = listenerManager;
        this.result = result;
    }

    @Override
    public void onBefore() throws Exception {
        task.init(context);
        listenerManager.beforeTask(context);
    }

    @Override
    public void doRun() throws Exception {
        result = task.handle(context);
    }

    @Override
    public void onAfter() throws Exception {
        listenerManager.onSuccess(context, result);
    }

    @Override
    public void onFailure(Exception e) {
        listenerManager.onException(context, e);
    }

    @Override
    public void onFinal() {
        listenerManager.afterAll(context);
    }
}
