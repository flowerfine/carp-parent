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
package cn.sliew.carp.framework.task.listener;

import cn.sliew.carp.framework.task.TaskContext;
import cn.sliew.carp.framework.task.TaskResult;

import java.util.ArrayList;
import java.util.List;

public class TaskListenerManager implements TaskListener {

    private final List<TaskListener> listeners = new ArrayList();

    public void addListener(TaskListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeListener(TaskListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    @Override
    public void beforeTask(TaskContext context) {
        synchronized (listeners) {
            for (TaskListener listener : listeners) {
                listener.beforeTask(context);
            }
        }
    }

    @Override
    public void onSuccess(TaskContext context, TaskResult result) {
        synchronized (listeners) {
            for (TaskListener listener : listeners) {
                listener.onSuccess(context, result);
            }
        }
    }

    @Override
    public void onException(TaskContext context, Exception e) {
        synchronized (listeners) {
            for (TaskListener listener : listeners) {
                listener.onException(context, e);
            }
        }
    }

    @Override
    public void afterAll(TaskContext context) {
        synchronized (listeners) {
            for (TaskListener listener : listeners) {
                listener.afterAll(context);
            }
        }
    }

}
