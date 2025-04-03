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
package cn.sliew.carp.framework.pf4j.api;

import cn.sliew.carp.framework.pf4j.api.sdks.ServiceSdk;
import jakarta.annotation.Nonnull;

/**
 * A convenience interface for accessing plugin SDK services.
 *
 * <p>If an extension needs any services, this interface can be included as a constructor parameter
 * and the implementation will be injected into the extension.
 *
 * <pre>{@code
 * public class MyExtension {
 *
 *   private final PluginSdks pluginSdks;
 *
 *   public MyExtension(PluginSdks pluginSdks) {
 *     this.pluginSdks = pluginSdks;
 *   }
 * }
 * }</pre>
 */
public interface PluginSdks {

    /**
     * Entry point for service-specific SDKs.
     *
     * <p>A service may register its own specialized SDK to help plugin developers write extensions.
     *
     * @param <T> The service SDK type. There will only be one of these per-service.
     */
    @Nonnull
    <T extends ServiceSdk> T serviceSdk(@Nonnull Class<T> type);
}
