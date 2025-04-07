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

import cn.sliew.carp.framework.pf4j.core.proxy.aspects.LogInvocationAspect;
import cn.sliew.carp.framework.pf4j.core.remote.extension.transport.RemoteExtensionPayload;
import cn.sliew.carp.framework.pf4j.core.remote.extension.transport.RemoteExtensionQuery;
import cn.sliew.carp.framework.pf4j.core.remote.extension.transport.RemoteExtensionResponse;
import cn.sliew.carp.framework.pf4j.core.remote.extension.transport.RemoteExtensionTransport;
import lombok.Getter;
import org.slf4j.MDC;

@Getter
public class RemoteExtension {

    private final String id;
    private final String pluginId;
    private final String type;
    private final RemoteExtensionPointConfig config;
    private final RemoteExtensionTransport transport;

    /**
     * The remote extension with the implemented {@link RemoteExtensionTransport} which is based on the
     * remote extension configuration.
     *
     * @param id       Identifier of the remote extension.  Used for tracing.
     * @param pluginId Identifier of the plugin.  Used for tracing.
     * @param type     The remote extension type. Services will have a corresponding remote extension point implementation depending on the type.
     * @param config   Configuration necessary for the extension point - typically specifying something to configure prior to the remote extension invocation.
     */
    public RemoteExtension(
            String id,
            String pluginId,
            String type,
            RemoteExtensionPointConfig config,
            RemoteExtensionTransport transport) {
        this.id = id;
        this.pluginId = pluginId;
        this.type = type;
        this.config = config;
        this.transport = transport;
    }

    /**
     * Return the configuration as the requested type.
     */
    @SuppressWarnings("unchecked")
    public <T extends RemoteExtensionPointConfig> T getTypedConfig() {
        return (T) config;
    }

    /**
     * Invoke the remote extension via the {@link RemoteExtensionTransport} implementation.
     */
    public void invoke(RemoteExtensionPayload payload) {
        decorate(() -> {
            transport.invoke(payload);
            return null;
        });
    }

    /**
     * Write to the remote extension via the {@link RemoteExtensionTransport} implementation. Returns a
     * {@link RemoteExtensionResponse} implementation.
     */
    public <T extends RemoteExtensionResponse> T write(RemoteExtensionPayload payload) {
        return (T) decorate(() -> transport.write(payload));
    }

    /**
     * Read from the remote extension via the {@link RemoteExtensionTransport} implementation. Returns a
     * {@link RemoteExtensionResponse} implementation.
     */
    public <T extends RemoteExtensionResponse> T read(RemoteExtensionQuery query) {
        return (T) decorate(() -> transport.read(query));
    }

    @SuppressWarnings("unchecked")
    private <T> T decorate(TransportCall<T> transportCall) {
        MDC.put(LogInvocationAspect.PLUGIN_ID, pluginId);
        MDC.put(LogInvocationAspect.PLUGIN_EXTENSION, id);

        try {
            return transportCall.call();
        } finally {
            MDC.remove(LogInvocationAspect.PLUGIN_ID);
            MDC.remove(LogInvocationAspect.PLUGIN_EXTENSION);
        }
    }

    @FunctionalInterface
    private interface TransportCall<T> {
        T call();
    }
}
