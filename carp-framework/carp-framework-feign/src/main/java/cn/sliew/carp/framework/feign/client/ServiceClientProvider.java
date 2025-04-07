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

public interface ServiceClientProvider {

    /**
     * Returns the concrete feign service client
     *
     * @param type            feign interface type
     * @param serviceEndpoint endpoint definition
     * @param <T>             type of client , usually an interface with all the remote method definitions.
     * @return the feign interface implementation
     */
    <T> T getService(Class<T> type, ServiceEndpoint serviceEndpoint);
}
