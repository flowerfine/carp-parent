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
package cn.sliew.carp.framework.common.retry;

import cn.hutool.core.thread.ThreadUtil;
import cn.sliew.carp.framework.common.exception.SliewException;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Supplier;

public enum RetrySupport {
    ;

    public static <T> T retry(Supplier<T> fn, int maxRetries, Duration retryBackoff, boolean exponential) {
        int retries = 0;
        while (true) {
            try {
                return fn.get();
            } catch (Exception e) {
                if (e instanceof SliewException sliewException) {
                    Boolean retryable = sliewException.getRetryable();
                    if (Objects.equals(retryable, Boolean.FALSE)) {
                        throw e;
                    }
                }
                if (retries >= (maxRetries - 1)) {
                    throw e;
                }

                long timeout =
                        !exponential
                                ? retryBackoff.toMillis()
                                : (long) Math.pow(2, retries) * retryBackoff.toMillis();
                ThreadUtil.sleep(timeout);

                retries++;
            }
        }
    }
}
