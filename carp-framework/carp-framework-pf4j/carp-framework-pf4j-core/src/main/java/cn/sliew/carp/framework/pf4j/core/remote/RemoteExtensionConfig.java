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
package cn.sliew.carp.framework.pf4j.core.remote;

import cn.sliew.carp.framework.pf4j.core.update.CarpPluginInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * A Carp plugin's remote extension configuration.
 *
 * <p>This model is used by Carp to determine which extension points and services require
 * remote extension point configuration.
 *
 * <p>The plugin release {@link CarpPluginInfo.CarpPluginRelease#getRemoteExtensions()} field is used to inform
 * Carp which service to use in configuring the extension point {@link #type} and additionally
 * if the remote extension is compatible with the running version of the Carp service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemoteExtensionConfig {

    /**
     * The remote extension type. The remote extension is configured in the service that implements
     * this extension type.
     */
    @Nonnull
    private String type;

    /**
     * Identifier of the remote extension. Used for tracing.
     */
    @Nonnull
    private String id;

    /**
     * Outbound transport configuration for the remote extension point; the protocol to address it
     * with and the necessary configuration.
     */
    @Nonnull
    private RemoteExtensionTransportConfig transport = new RemoteExtensionTransportConfig();

    /**
     * Configures the remote extension point.
     */
    @Nullable
    private Map<String, Object> config;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RemoteExtensionTransportConfig {

        @Nonnull
        private Http http = new Http();

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Http {

            /**
             * URL for remote extension invocation.
             */
            @Nonnull
            private String url;

            /**
             * Any query parameters necessary to invoke the extension.
             */
            @Nonnull
            private Map<String, String> queryParams = new HashMap<>();

            /**
             * A placeholder for misc. configuration for the underlying HTTP client.
             */
            @Nonnull
            private Map<String, String> config = new HashMap<>();

            /**
             * Headers for the various invocation types.
             */
            @Nonnull
            private Headers headers = new Headers();

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class Headers {
                @Nonnull
                private Map<String, String> invokeHeaders = new HashMap<>();
                @Nonnull
                private Map<String, String> writeHeaders = new HashMap<>();
                @Nonnull
                private Map<String, String> readHeaders = new HashMap<>();
            }
        }
    }
}
