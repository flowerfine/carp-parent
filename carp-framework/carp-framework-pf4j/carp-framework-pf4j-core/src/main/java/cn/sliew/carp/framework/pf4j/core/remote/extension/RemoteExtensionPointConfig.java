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
package cn.sliew.carp.framework.pf4j.core.remote.extension;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Root type of remote extension point configurations, implemented in Carp services.
 */
public interface RemoteExtensionPointConfig {

    @Getter
    @EqualsAndHashCode
    class NoOpRemoteExtensionPointConfig implements RemoteExtensionPointConfig {
        private final String type;

        public NoOpRemoteExtensionPointConfig() {
            this("noop");
        }

        /**
         * No-op in the case wherein a remote extension does not have any necessary configuration.
         *
         * @param type The type of remote extension, defaults to "noop"
         */
        public NoOpRemoteExtensionPointConfig(String type) {
            this.type = type;
        }
    }
}
