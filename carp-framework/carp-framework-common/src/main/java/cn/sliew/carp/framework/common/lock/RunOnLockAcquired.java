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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.Callable;

public interface RunOnLockAcquired {

    /**
     * Executes an action after lock identified by {@code keyName} was obtained
     *
     * @param action  action to execute once lock is acquired
     * @param keyName name of a lock
     * @return result of attempting to acquire a lock
     */
    RunOnLockResult<Void> execute(Runnable action, String keyName);

    /**
     * Executes an action after lock identified by {@code keyName} was obtained
     *
     * @param action  action to execute once lock is acquired
     * @param keyName name of a lock
     * @return result of attempting to acquire a lock and result of action execution
     */
    <R> RunOnLockResult<R> execute(Callable<R> action, String keyName);

    @AllArgsConstructor
    class RunOnRedisLockAcquired implements RunOnLockAcquired {
        private final LockManager lockManager;

        private LockOptions lockOptions(String name) {
            return new LockOptions()
                    .withLockName(name)
                    .withMaximumLockDuration(Duration.ofSeconds(1L));
        }

        @Override
        public RunOnLockResult<Void> execute(Runnable action, String keyName) {
            try {
                LockManager.AcquireLockResponse acquireLock = lockManager.acquireLock(lockOptions(keyName), action);
                if (!acquireLock.getLockStatus().equals(LockManager.LockStatus.ACQUIRED)) {
                    return new RunOnLockResult<>(false);
                }
                return new RunOnLockResult(true, true, null, acquireLock.getOnLockAcquiredCallbackResult());
            } catch (Exception e) {
                return new RunOnLockResult<>(true, e);
            }
        }

        @Override
        public <R> RunOnLockResult<R> execute(Callable<R> action, String keyName) {
            try {
                LockManager.AcquireLockResponse acquireLock = lockManager.acquireLock(lockOptions(keyName), action);
                if (!acquireLock.getLockStatus().equals(LockManager.LockStatus.ACQUIRED)) {
                    return new RunOnLockResult<>(false);
                }
                return new RunOnLockResult(true, true, acquireLock.getOnLockAcquiredCallbackResult());
            } catch (Exception e) {
                return new RunOnLockResult<>(true, e);
            }
        }
    }

    @Slf4j
    class NoOpRunOnLockAcquired implements RunOnLockAcquired {

        @Override
        public RunOnLockResult<Void> execute(Runnable action, String keyName) {
            try {
                log.debug("Executing action with no locking for key: {}", keyName);
                action.run();
                log.debug("Execution with no locking for key: {} successful", keyName);
                return new RunOnLockResult<>(true, true);
            } catch (Exception e) {
                log.error("An exception was thrown while executing action with no locking for key: {}", keyName);
                log.error(e.getMessage());
                return new RunOnLockResult<>(false, e);
            }
        }

        @Override
        public <R> RunOnLockResult<R> execute(Callable<R> action, String keyName) {
            try {
                return new RunOnLockResult<>(true, true, action.call());
            } catch (Exception e) {
                return new RunOnLockResult<>(false, e);
            }
        }
    }

    @Getter
    @AllArgsConstructor
    class RunOnLockResult<R> {
        private final boolean lockAcquired;
        private final boolean actionExecuted;
        private final Exception exception;
        private final R result;

        public RunOnLockResult(boolean lockAcquired) {
            this(lockAcquired, false, null, null);
        }

        public RunOnLockResult(boolean lockAcquired, boolean actionExecuted) {
            this(lockAcquired, actionExecuted, null, null);
        }

        public RunOnLockResult(boolean lockAcquired, Exception exception) {
            this(lockAcquired, false, exception, null);
        }

        public RunOnLockResult(boolean lockAcquired, boolean actionExecuted, R result) {
            this(lockAcquired, actionExecuted, null, result);
        }
    }
}
