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
import feign.Capability;
import feign.Client;
import feign.Feign;
import feign.QueryMapEncoder;
import feign.codec.Decoder;
import feign.codec.Encoder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FeignServiceClientFactory implements ServiceClientFactory {

    private final Client client;
    private final QueryMapEncoder queryMapEncoder;
    private final Encoder encoder;
    private final Decoder decoder;
    private final Capability capability;

    @Override
    public <T> T create(Class<T> type, ServiceEndpoint serviceEndpoint) {
        return Feign.builder()
                .client(client)
                .queryMapEncoder(queryMapEncoder)
                .encoder(encoder)
                .decoder(decoder)
                .addCapability(capability)
                .target(type, serviceEndpoint.getBaseUrl());
    }
}
