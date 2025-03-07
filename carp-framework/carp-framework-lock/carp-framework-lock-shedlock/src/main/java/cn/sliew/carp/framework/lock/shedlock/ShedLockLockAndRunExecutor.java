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
package cn.sliew.carp.framework.lock.shedlock;

import cn.sliew.carp.framework.common.lock.LockAndRunExecutor;
import cn.sliew.carp.framework.common.lock.LockRunResult;
import cn.sliew.carp.framework.common.util.KeyUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SimpleLock;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.Callable;

@Slf4j
@AllArgsConstructor
public class ShedLockLockAndRunExecutor implements LockAndRunExecutor {

    private final LockProvider lockProvider;

    @Override
    public LockRunResult<Void> execute(Runnable action, String keyName) {
        Optional<SimpleLock> lockOpt = getLock(keyName);
        if (lockOpt.isEmpty()) {
            log.error("Failed to acquire shedlock for key: {}", keyName);
            return new LockRunResult<>(false);
        }

        try {
            log.debug("Executing action with a lock for key: {}", keyName);
            action.run();
            log.debug("Finished action execution with a lock for key: {}", keyName);
            return new LockRunResult(true, true);
        } catch (Exception e) {
            log.error("An exception occurred while executing action with a lock for key: {}", keyName);
            return new LockRunResult(true, e);
        } finally {
            lockOpt.get().unlock();
            log.debug("Released shedlock for key {}", keyName);
        }
    }

    @Override
    public <R> LockRunResult<R> execute(Callable<R> action, String keyName) {
        Optional<SimpleLock> lockOpt = getLock(keyName);
        if (lockOpt.isEmpty()) {
            log.error("Failed to acquire shedlock for key: {}", keyName);
            return new LockRunResult<>(false);
        }

        try {
            log.debug("Executing action with a lock for key: {}", keyName);
            R callableResult = action.call();
            log.debug("Finished action execution with a lock for key: {}", keyName);
            return new LockRunResult(true, true, callableResult);
        } catch (Exception e) {
            log.error("An exception occurred while executing action with a lock for key: {}", keyName);
            return new LockRunResult(true, e);
        } finally {
            lockOpt.get().unlock();
            log.debug("Released shedlock for key {}", keyName);
        }
    }


    private Optional<SimpleLock> getLock(String keyName) {
        String lockKey = KeyUtil.buildLockKey(keyName);
        try {
            log.debug("Attempt to acquire shedlock for key: {}, lock: {}", keyName, lockKey);
            LockConfiguration lockConfiguration = new LockConfiguration(Instant.now(), lockKey, Duration.ofSeconds(1), Duration.ofMillis(200));
            return lockProvider.lock(lockConfiguration);
        } catch (Exception e) {
            log.error("An exception occurred during an attempt to acquire shedlock for key: {}, lock: {}", keyName, lockKey, e);
            throw e;
        }
    }
}
