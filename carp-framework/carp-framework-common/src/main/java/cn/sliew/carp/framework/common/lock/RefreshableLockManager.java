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
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Lock Manager with heartbeat support.
 */
public interface RefreshableLockManager extends LockManager {

    HeartbeatResponse heartbeat(final HeartbeatLockRequest heartbeatLockRequest);

    void queueHeartbeat(final HeartbeatLockRequest heartbeatLockRequest);

    class HeartbeatLockRequest {
        private final String requestId;
        private AtomicReference<Lock> lock;
        private final AtomicInteger retriesOnFailure;
        @Getter
        private final Duration heartbeatDuration;
        @Getter
        private final Instant startedAt;
        private final Clock clock;
        private final boolean reuseLockVersion;

        public HeartbeatLockRequest(
                Lock lock,
                AtomicInteger retriesOnFailure,
                Clock clock,
                Duration heartbeatDuration,
                boolean reuseLockVersion) {
            this.lock = new AtomicReference<>(lock);
            this.retriesOnFailure = retriesOnFailure;
            this.clock = clock;
            this.startedAt = clock.instant();
            this.heartbeatDuration = heartbeatDuration;
            this.reuseLockVersion = reuseLockVersion;
            this.requestId = lock.getName() + lock.getOwnerName() + clock.millis();
        }

        public Lock getLock() {
            return lock.get();
        }

        public void setLock(final Lock lock) {
            this.lock.set(lock);
        }

        public boolean timesUp() {
            return Duration.between(startedAt, clock.instant()).compareTo(heartbeatDuration) >= 0;
        }

        public Duration getRemainingLockDuration() {
            return Duration.between(startedAt, clock.instant()).minus(heartbeatDuration).abs();
        }

        public boolean shouldRetry() {
            return retriesOnFailure.decrementAndGet() >= 0;
        }

        public boolean reuseVersion() {
            return reuseLockVersion;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            HeartbeatLockRequest that = (HeartbeatLockRequest) o;
            return Objects.equals(requestId, that.requestId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(requestId);
        }

    }

    @Getter
    @EqualsAndHashCode
    @AllArgsConstructor
    class HeartbeatResponse {
        private final Lock lock;
        private final LockHeartbeatStatus lockHeartbeatStatus;
    }

    class LockFailedHeartbeatException extends LockException {
        public LockFailedHeartbeatException(String message) {
            super(message);
        }

        public LockFailedHeartbeatException(String message, Throwable cause) {
            super(message, cause);
        }

        public LockFailedHeartbeatException(Throwable cause) {
            super(cause);
        }
    }

    enum LockHeartbeatStatus {
        SUCCESS, EXPIRED, ERROR, MAX_HEARTBEAT_REACHED
    }
}
