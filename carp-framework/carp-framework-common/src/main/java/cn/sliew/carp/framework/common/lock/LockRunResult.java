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

@Getter
@AllArgsConstructor
public class LockRunResult<R> {
    private final boolean lockAcquired;
    private final boolean actionExecuted;
    private final Exception exception;
    private final R result;

    public LockRunResult(boolean lockAcquired) {
        this(lockAcquired, false, null, null);
    }

    public LockRunResult(boolean lockAcquired, boolean actionExecuted) {
        this(lockAcquired, actionExecuted, null, null);
    }

    public LockRunResult(boolean lockAcquired, Exception exception) {
        this(lockAcquired, false, exception, null);
    }

    public LockRunResult(boolean lockAcquired, boolean actionExecuted, R result) {
        this(lockAcquired, actionExecuted, null, result);
    }
}