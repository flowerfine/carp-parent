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
package cn.sliew.carp.framework.pf4j.core.sdks;

import jakarta.annotation.Nullable;
import org.pf4j.PluginWrapper;

/**
 * A Plugin SDK factory, responsible for initializing and configuring SDKs
 * according to an extension and/or plugin configurations.
 */
public interface SdkFactory {

    /**
     * Create the SDK for the provided {@param pluginClass} and {@param pluginWrapper}.
     * <p>
     * TODO(rz): pluginWrapper should never be null. Investigate.
     *
     * @param pluginClass Any class inside of a plugin.
     */
    <T> T create(Class<T> pluginClass, @Nullable PluginWrapper pluginWrapper);
}
