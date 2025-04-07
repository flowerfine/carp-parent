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

/**
 * Define properties of the remote extension. Carp services must implement this and provide it
 * as a Spring bean in order for remote plugin extensions to be resolved from the configuration.
 */
public interface RemoteExtensionPointDefinition {

    /**
     * The remote extension type.
     */
    String type();

    /**
     * The remote extension configuration type.
     */
    default Class<? extends RemoteExtensionPointConfig> configType() {
        return RemoteExtensionPointConfig.NoOpRemoteExtensionPointConfig.class;
    }
}
