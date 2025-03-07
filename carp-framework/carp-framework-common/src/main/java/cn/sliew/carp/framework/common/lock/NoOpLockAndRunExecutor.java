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
package cn.sliew.carp.framework.common.lock;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

@Slf4j
public class NoOpLockAndRunExecutor implements LockAndRunExecutor {

    @Override
    public LockRunResult<Void> execute(Runnable action, String keyName) {
        try {
            log.debug("Executing action with no locking for key: {}", keyName);
            action.run();
            log.debug("Execution with no locking for key: {} successful", keyName);
            return new LockRunResult<>(true, true);
        } catch (Exception e) {
            log.error("An exception was thrown while executing action with no locking for key: {}", keyName);
            log.error(e.getMessage());
            return new LockRunResult<>(false, e);
        }
    }

    @Override
    public <R> LockRunResult<R> execute(Callable<R> action, String keyName) {
        try {
            return new LockRunResult<>(true, true, action.call());
        } catch (Exception e) {
            return new LockRunResult<>(false, e);
        }
    }
}
