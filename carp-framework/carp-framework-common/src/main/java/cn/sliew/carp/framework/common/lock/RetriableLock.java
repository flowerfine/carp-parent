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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * This class is a wrapper for the RunOnLockAcquired implementation. If the implementation fails to
 * obtain a lock, this class keeps a count of the failed attempts and tries as many times as
 * specified by the user.
 */
@Slf4j
public class RetriableLock {

    private final RunOnLockAcquired lock;
    private final RetrySupport retrySupport;

    public RetriableLock(RunOnLockAcquired lock, RetrySupport retrySupport) {
        this.lock = lock;
        this.retrySupport = retrySupport;
    }

    /**
     * This method blocks the current thread and delegates locking and function execution to
     * RunOnLockAcquired. If the lock cannot be acquired, this method will try a number of times
     * specified by the user in the {@code options.maxRetries} object. If the lock cannot be obtained
     * within the specified number of attempts, either an exception will be thrown (if {@code
     * options.throwOnAcquireFailure} is true) or false will be returned.
     *
     * @param rlOptions aggregates all the parameters that influence the process of obtaining a lock.
     * @param action    action to execute once lock is acquired
     * @return true, if lock was acquired and action was executed correctly; false if not obtained
     * @throws FailedToGetLockException if lock was not obtained and {@code
     *                                  options.throwOnAcquireFailure} is true
     */
    public Boolean lock(RetriableLockOptions rlOptions, Runnable action) {
        try {
            retrySupport.retry(
                    new LockAndRun(rlOptions, action, lock),
                    rlOptions.getMaxRetries(),
                    rlOptions.getInterval(),
                    rlOptions.isExponential());
            return true;

        } catch (FailedToGetLockException e) {
            log.error(
                    "Tried {} times to acquire the lock {} and failed.",
                    rlOptions.getMaxRetries(),
                    rlOptions.getLockName());
            if (rlOptions.isThrowOnAcquireFailure()) {
                throw e;
            }
            return false;
        }
    }

    public static class FailedToGetLockException extends RuntimeException {
        public FailedToGetLockException(String lockName) {
            super("Failed to acquire lock: " + lockName);
        }
    }

    /***
     *   Wrapper class for Supplier<Boolean> required by the RetrySupplier::retry method
     */
    @RequiredArgsConstructor
    private static final class LockAndRun implements Supplier<Boolean> {

        private static final Duration MAX_LOCK_DURATION = Duration.ofSeconds(2L);

        private final RetriableLockOptions options;
        private final Runnable action;
        private final RunOnLockAcquired lockManager;

        /***
         * Method tries to acquire lock via {@code lockManager} and execute an action once lock is acquired,
         * Throws {@code FailedToGetLockException} when failed to acquire lock in specified number of times,
         * It is up to client to handle the exception.
         *
         * @return true, when lock was successfully acquired
         * @throws FailedToGetLockException when failed to acquire lock in maxRetries times
         */
        @Override
        public Boolean get() {
            var options =
                    new LockOptions()
                            .withLockName(this.options.getLockName())
                            .withMaximumLockDuration(MAX_LOCK_DURATION);

            String lockName = options.getLockName();
            RunOnLockAcquired.RunOnLockResult response = lockManager.execute(action, lockName);
            if (response.isLockAcquired()) {
                log.debug("Successfully acquired lock: {}", lockName);
                // The result of this method is nowhere used - we need it to satisfy RetrySupport contract
                return true;
            } else {
                // This exception is caught inside the retrySupport.retry method $maxRetries times.
                log.debug("Failed to acquired lock: {}", lockName);
                throw new FailedToGetLockException(lockName);
            }
        }
    }
}
