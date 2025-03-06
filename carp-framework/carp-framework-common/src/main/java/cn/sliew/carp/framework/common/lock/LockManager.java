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

import cn.sliew.carp.framework.common.util.KeyUtil;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;

public interface LockManager {

    <R> AcquireLockResponse<R> acquireLock(
            final LockOptions lockOptions, final Callable<R> onLockAcquiredCallback);

    AcquireLockResponse<Void> acquireLock(
            final LockOptions lockOptions, final Runnable onLockAcquiredCallback);

    <R> AcquireLockResponse<R> acquireLock(
            final String lockName, final long maximumLockDurationMillis, final Callable<R> onLockAcquiredCallback);

    AcquireLockResponse<Void> acquireLock(
            final String lockName, final long maximumLockDurationMillis, final Runnable onLockAcquiredCallback);

    boolean releaseLock(final Lock lock, boolean wasWorkSuccessful);

    Lock tryCreateLock(final LockOptions lockOptions);

    String NAME_FALLBACK = UUID.randomUUID().toString();

    /**
     * Used only if an ownerName is not provided in the constructor.
     */
    default String getOwnerName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return NAME_FALLBACK;
        }
    }

    default String lockKey(String name) {
        return KeyUtil.buildCacheKey(name.toLowerCase());
    }

    @Getter
    @AllArgsConstructor
    class AcquireLockResponse<R> {
        private final Lock lock;
        private final R onLockAcquiredCallbackResult;
        private final LockStatus lockStatus;
        private final Exception exception;
        private boolean released;

    }

    enum LockStatus {
        ACQUIRED, TAKEN, ERROR, EXPIRED
    }

    interface LockReleaseStatus {
        String SUCCESS = "SUCCESS";
        String SUCCESS_GONE = "SUCCESS_GONE"; // lock no longer exists
        String FAILED_NOT_OWNER = "FAILED_NOT_OWNER"; // found lock but belongs to someone else
    }

    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class Lock implements Named {
        private final String name;
        private final String ownerName;
        private final long version;
        private final long leaseDurationMillis;
        private final long successIntervalMillis;
        private final long failureIntervalMillis;
        private final long ownerSystemTimestamp;
        private final String attributes; // arbitrary string to store data along side the lock

        @JsonCreator
        public Lock(
                @JsonProperty("name") String name,
                @JsonProperty("ownerName") String ownerName,
                @JsonProperty("version") long version,
                @JsonProperty("leaseDurationMillis") long leaseDurationMillis,
                @JsonProperty("successIntervalMillis") Long successIntervalMillis,
                @JsonProperty("failureIntervalMillis") Long failureIntervalMillis,
                @JsonProperty("ownerSystemTimestamp") long ownerSystemTimestamp,
                @JsonProperty("attributes") String attributes) {
            this.name = name;
            this.ownerName = ownerName;
            this.leaseDurationMillis = leaseDurationMillis;
            this.successIntervalMillis = Optional.ofNullable(successIntervalMillis).orElse(0L);
            this.failureIntervalMillis = Optional.ofNullable(failureIntervalMillis).orElse(0L);
            this.ownerSystemTimestamp = ownerSystemTimestamp;
            this.version = version;
            this.attributes = attributes;
        }

        public long nextVersion() {
            return version + 1;
        }

        @Override
        public boolean equals(Object o) {
            // Two locks are identical if the lock name, owner and version match.
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Lock lock = (Lock) o;
            return Objects.equals(name, lock.name)
                    && Objects.equals(ownerName, lock.ownerName)
                    && Objects.equals(version, lock.version);
        }

        @Override
        public int hashCode() {
            // Two locks are equal if lockName, ownerName and version match.
            return Objects.hash(name, ownerName, version);
        }
    }

    interface Named {
        String getName();
    }

    interface LockMetricsConstants {
        String ACQUIRE = "kork.lock.acquire";
        String RELEASE = "kork.lock.release";
        String HEARTBEATS = "kork.lock.heartbeat";
        String ACQUIRE_DURATION = "kork.lock.acquire.duration";
    }

    class LockException extends RuntimeException {
        public LockException(String message) {
            super(message);
        }

        public LockException(String message, Throwable cause) {
            super(message, cause);
        }

        public LockException(Throwable cause) {
            super(cause);
        }
    }

    class LockCallbackException extends LockException {
        public LockCallbackException(String message) {
            super(message);
        }

        public LockCallbackException(String message, Throwable cause) {
            super(message, cause);
        }

        public LockCallbackException(Throwable cause) {
            super(cause);
        }
    }

    class LockNotAcquiredException extends LockException {
        public LockNotAcquiredException(String message) {
            super(message);
        }

        public LockNotAcquiredException(String message, Throwable cause) {
            super(message, cause);
        }

        public LockNotAcquiredException(Throwable cause) {
            super(cause);
        }
    }

    class LockExpiredException extends LockException {
        public LockExpiredException(String message) {
            super(message);
        }

        public LockExpiredException(String message, Throwable cause) {
            super(message, cause);
        }

        public LockExpiredException(Throwable cause) {
            super(cause);
        }
    }
}
