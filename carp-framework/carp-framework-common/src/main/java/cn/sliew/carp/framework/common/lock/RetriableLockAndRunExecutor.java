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

import cn.sliew.carp.framework.common.retry.RetrySupport;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.Callable;

/**
 * This class is a wrapper for the LockAndRunExecutor implementation. If the implementation fails to
 * obtain a lock, this class keeps a count of the failed attempts and tries as many times as
 * specified by the user.
 */
@Slf4j
public class RetriableLockAndRunExecutor implements LockAndRunExecutor {

    private final LockAndRunExecutor executor;
    private final int maxRetries;
    private final Duration retryBackoff;
    private final boolean exponential;

    public RetriableLockAndRunExecutor(LockAndRunExecutor executor) {
        this(executor, 5, Duration.ofMillis(500), false);
    }

    public RetriableLockAndRunExecutor(LockAndRunExecutor executor, int maxRetries, Duration retryBackoff, boolean exponential) {
        this.executor = executor;
        this.maxRetries = maxRetries;
        this.retryBackoff = retryBackoff;
        this.exponential = exponential;
    }

    @Override
    public LockRunResult<Void> execute(Runnable action, String keyName) {
        return RetrySupport.retry(() -> {
            LockRunResult<Void> result = executor.execute(action, keyName);
            if (result.isLockAcquired() == false) {
                // This exception is caught inside the retrySupport.retry method $maxRetries times.
                log.debug("Failed to acquired lock: {}", keyName);
                throw new FailedToGetLockException(keyName);
            }
            return result;
        }, maxRetries, retryBackoff, exponential);
    }

    @Override
    public <R> LockRunResult<R> execute(Callable<R> action, String keyName) {
        return RetrySupport.retry(() -> {
            LockRunResult<R> result = executor.execute(action, keyName);
            if (result.isLockAcquired() == false) {
                // This exception is caught inside the retrySupport.retry method $maxRetries times.
                log.debug("Failed to acquired lock: {}", keyName);
                throw new FailedToGetLockException(keyName);
            }
            return result;
        }, maxRetries, retryBackoff, exponential);
    }

    public static class FailedToGetLockException extends RuntimeException {
        public FailedToGetLockException(String lockName) {
            super("Failed to acquire lock: " + lockName);
        }
    }
}
