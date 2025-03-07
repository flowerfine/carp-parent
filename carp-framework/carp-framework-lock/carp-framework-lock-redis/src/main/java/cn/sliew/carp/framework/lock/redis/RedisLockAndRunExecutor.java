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
package cn.sliew.carp.framework.lock.redis;

import cn.sliew.carp.framework.common.lock.LockAndRunExecutor;
import cn.sliew.carp.framework.common.lock.LockRunResult;
import cn.sliew.carp.framework.redis.RedissonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.Callable;

@Slf4j
@AllArgsConstructor
public class RedisLockAndRunExecutor implements LockAndRunExecutor {

    private final RedissonUtil redissonUtil;

    @Override
    public LockRunResult<Void> execute(Runnable action, String keyName) {
        boolean locked = redissonUtil.lock(keyName, Duration.ofSeconds(1L));
        if (!locked) {
            log.error("Failed to acquire redis lock for key: {}", keyName);
            return new LockRunResult<>(false);
        }
        try {
            log.debug("Executing action with a redis lock for key: {}", keyName);
            action.run();
            log.debug("Finished action execution with a redis lock for key: {}", keyName);
            return new LockRunResult(true, true);
        } catch (Exception e) {
            log.error("An exception occurred while executing action with a redis lock for key: {}", keyName, e);
            return new LockRunResult<>(true, e);
        } finally {
            redissonUtil.unlock(keyName);
            log.debug("Released redis lock for key {}", keyName);
        }
    }

    @Override
    public <R> LockRunResult<R> execute(Callable<R> action, String keyName) {
        boolean locked = redissonUtil.lock(keyName, Duration.ofSeconds(1L));
        if (!locked) {
            log.error("Failed to acquire redis lock for key: {}", keyName);
            return new LockRunResult<>(false);
        }
        try {
            log.debug("Executing action with a redis lock for key: {}", keyName);
            R callableResult = action.call();
            log.debug("Finished action execution with a redis lock for key: {}", keyName);
            return new LockRunResult(true, true, callableResult);
        } catch (Exception e) {
            log.error("An exception occurred while executing action with a redis lock for key: {}", keyName, e);
            return new LockRunResult<>(true, e);
        } finally {
            redissonUtil.unlock(keyName);
            log.debug("Released redis lock for key {}", keyName);
        }
    }
}
