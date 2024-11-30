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
package cn.sliew.carp.framework.task.example;

import cn.sliew.carp.framework.common.dict.task.TaskStatus;
import cn.sliew.carp.framework.task.Task;
import cn.sliew.carp.framework.task.TaskContext;
import cn.sliew.carp.framework.task.TaskException;
import cn.sliew.carp.framework.task.TaskResult;

public class LogTask implements Task {

    @Override
    public String getType() {
        return null;
    }

    @Override
    public void init(TaskContext context) {

    }

    @Override
    public TaskResult handle(TaskContext context) throws TaskException {
        return null;
    }

    @Override
    public TaskResult cancel() throws TaskException {
        return TaskResult.builder()
                .status(TaskStatus.FAILURE)
                .message("canceld")
                .build();
    }

    @Override
    public TaskResult onTimeout() throws TaskException {
        return TaskResult.builder()
                .status(TaskStatus.FAILURE)
                .message("timeout")
                .build();
    }
}
