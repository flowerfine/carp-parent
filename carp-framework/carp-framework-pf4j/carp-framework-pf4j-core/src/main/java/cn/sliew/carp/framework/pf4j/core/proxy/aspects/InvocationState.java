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
package cn.sliew.carp.framework.pf4j.core.proxy.aspects;

import io.micrometer.core.instrument.Meter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Interface representing invocation state, used with {@link InvocationAspect} to process method
 * invocation.  All subclasses of {@link InvocationState} are expected to be immutable - properties must
 * be declared with `val`.
 */
public interface InvocationState {

    /**
     * Tracks the state of a method invocation for the purposes of metrics collection.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class MetricInvocationState implements InvocationState {
        private String extensionName;
        private Long startTimeMs;
        // todo 获取指标
        private Meter.Id timingId;
    }

    /**
     * Tracks the state of a method invocation for the purposes of logging.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class LogInvocationState implements InvocationState {
        private String extensionName;
        private String methodName;
    }
}
