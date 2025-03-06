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

import lombok.Getter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class LockOptions {
    private String lockName;
    private Duration maximumLockDuration;
    private Duration successInterval = Duration.ZERO;
    private Duration failureInterval = Duration.ZERO;
    private long version;
    // the list will be joined with a ';' delimiter for brevity
    private List<String> attributes = new ArrayList<>();
    private boolean reuseVersion;

    public LockOptions withLockName(String name) {
        this.lockName = name;
        return this;
    }

    public LockOptions withMaximumLockDuration(Duration maximumLockDuration) {
        this.maximumLockDuration = maximumLockDuration;
        return this;
    }

    public LockOptions withSuccessInterval(Duration successInterval) {
        this.successInterval = successInterval;
        return this;
    }

    public LockOptions withFailureInterval(Duration failureInterval) {
        this.failureInterval = failureInterval;
        return this;
    }

    public LockOptions withAttributes(List<String> attributes) {
        this.attributes = attributes;
        return this;
    }

    public LockOptions withVersion(Long version) {
        this.version = version;
        this.reuseVersion = true;
        return this;
    }

    public void validateInputs() {
        if (!this.lockName.matches("^[a-zA-Z0-9.-]+$")) {
            throw new RuntimeException("Lock name must be alphanumeric, may contain dots");
        }

        Objects.requireNonNull(this.lockName, "Lock name must be provided");
        Objects.requireNonNull(this.maximumLockDuration, "Lock max duration must be provided");
    }

    public void setVersion(long version) {
        this.version = version;
    }
}