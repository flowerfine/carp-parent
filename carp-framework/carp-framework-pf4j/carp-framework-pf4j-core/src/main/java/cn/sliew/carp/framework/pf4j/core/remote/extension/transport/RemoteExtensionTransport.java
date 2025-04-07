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
package cn.sliew.carp.framework.pf4j.core.remote.extension.transport;

/**
 * The transport on which to address the remote extension.
 */
public interface RemoteExtensionTransport {

    /**
     * Invoke the remote extension.  The expectation is that the remote extension is an async process
     * with a callback, so we do not wait for a particular response here - but the underlying
     * {@link RemoteExtensionTransport} implementation may throw an exception if something unexpected,
     * like a connection error, occurs.
     */
    void invoke(RemoteExtensionPayload remoteExtensionPayload);

    /**
     * Write to the remote extension.  This is a synchronous process which requires a response of
     * {@link RemoteExtensionResponse} type.  This may be used as a substitute for {@link #invoke} when
     * implementations require a synchronous process.
     */
    default RemoteExtensionResponse write(RemoteExtensionPayload remoteExtensionPayload) {
        return new RemoteExtensionResponse.NoOpRemoteExtensionResponse();
    }

    /**
     * Read from the remote extension.  This is a synchronous process which requires a response of
     * {@link RemoteExtensionResponse}.
     */
    default RemoteExtensionResponse read(RemoteExtensionQuery remoteExtensionQuery) {
        return new RemoteExtensionResponse.NoOpRemoteExtensionResponse();
    }
}
