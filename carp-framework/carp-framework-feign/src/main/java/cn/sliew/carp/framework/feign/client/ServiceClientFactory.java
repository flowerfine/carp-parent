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
package cn.sliew.carp.framework.feign.client;

import cn.sliew.carp.framework.feign.endpoint.ServiceEndpoint;

/**
 * Factory to build a client for a service.
 */
public interface ServiceClientFactory {

    /**
     * Builds a concrete client capable of making HTTP calls.
     *
     * @param type            client type
     * @param serviceEndpoint endpoint configuration
     * @param <T>             type of client , usually an interface with all the remote method definitions.
     * @return an implementation of the type of client given.
     */
    <T> T create(Class<T> type, ServiceEndpoint serviceEndpoint);

    /**
     * Decide if this factory can support the endpoint provided.
     *
     * @param type            client type
     * @param serviceEndpoint endpoint configuration
     * @return a client builder
     */
    default boolean supports(Class<?> type, ServiceEndpoint serviceEndpoint) {
        return true;
    }
}
