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
package cn.sliew.carp.framework.pf4j.core.remote.extension.transport.http;

import cn.sliew.carp.framework.pf4j.core.remote.RemoteExtensionConfig;
import cn.sliew.carp.framework.pf4j.core.remote.extension.transport.RemoteExtensionPayload;
import cn.sliew.carp.framework.pf4j.core.remote.extension.transport.RemoteExtensionQuery;
import cn.sliew.carp.framework.pf4j.core.remote.extension.transport.RemoteExtensionResponse;
import cn.sliew.carp.framework.pf4j.core.remote.extension.transport.RemoteExtensionTransport;
import cn.sliew.milky.common.util.JacksonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An HTTP {@link RemoteExtensionTransport}, OkHttp for the client.
 */
public class OkHttpRemoteExtensionTransport implements RemoteExtensionTransport {

    private final MediaType APPLICATION_JSON = MediaType.parse("application/json");

    private final ObjectMapper objectMapper;
    private final OkHttpClient client;
    private final RemoteExtensionConfig.RemoteExtensionTransportConfig.Http httpConfig;
    private final String encodedUrl;

    public OkHttpRemoteExtensionTransport(
            ObjectMapper objectMapper,
            OkHttpClient client,
            RemoteExtensionConfig.RemoteExtensionTransportConfig.Http httpConfig) {
        this.objectMapper = objectMapper;
        this.client = client;
        this.httpConfig = httpConfig;
        this.encodedUrl = buildUrl(Collections.emptyMap());
    }

    @Override
    public void invoke(RemoteExtensionPayload remoteExtensionPayload) {
        Request request = new Request.Builder()
                .url(encodedUrl)
                .headers(buildHeaders(httpConfig.getHeaders().getInvokeHeaders()))
                .post(RequestBody.create(
                        JacksonUtil.toJsonString(objectMapper, remoteExtensionPayload),
                        APPLICATION_JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String reason = response.body() != null ? response.body().string() : "Unknown reason: " + response.code();
                throw new OkHttpRemoteExtensionTransportException(reason);
            }
        } catch (IOException e) {
            throw new OkHttpRemoteExtensionTransportException(e);
        }
    }

    @Override
    public RemoteExtensionResponse write(RemoteExtensionPayload remoteExtensionPayload) {
        Request request = new Request.Builder()
                .url(encodedUrl)
                .headers(buildHeaders(httpConfig.getHeaders().getWriteHeaders()))
                .post(RequestBody.create(
                        JacksonUtil.toJsonString(objectMapper, remoteExtensionPayload),
                        APPLICATION_JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String reason = response.body() != null ? response.body().string() : "Unknown reason: " + response.code();
                throw new OkHttpRemoteExtensionTransportException(reason);
            }
            return JacksonUtil.parseJsonString(objectMapper, response.body().string(), RemoteExtensionResponse.class);
        } catch (IOException e) {
            throw new OkHttpRemoteExtensionTransportException(e);
        }
    }

    @Override
    public RemoteExtensionResponse read(RemoteExtensionQuery remoteExtensionQuery) {
        Request request = new Request.Builder()
                .url(buildUrl(toParams(remoteExtensionQuery)))
                .headers(buildHeaders(httpConfig.getHeaders().getReadHeaders()))
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String reason = response.body() != null ? response.body().string() : "Unknown reason: " + response.code();
                throw new OkHttpRemoteExtensionTransportException(reason);
            }
            return JacksonUtil.parseJsonString(objectMapper, response.body().string(), RemoteExtensionResponse.class);
        } catch (IOException e) {
            throw new OkHttpRemoteExtensionTransportException(e);
        }
    }

    private String buildUrl(Map<String, String> additionalParams) {
        HttpUrl.Builder httpUrlBuilder = HttpUrl.parse(httpConfig.getUrl()).newBuilder();
        if (httpUrlBuilder == null) {
            throw new RuntimeException("Unable to parse url '" + httpConfig.getUrl() + "'");
        }

        Map<String, String> allParams = new HashMap<>(httpConfig.getQueryParams());
        allParams.putAll(additionalParams);
        allParams.forEach(httpUrlBuilder::addQueryParameter);

        return httpUrlBuilder.build().toString();
    }

    private Headers buildHeaders(Map<String, String> headers) {
        Headers.Builder headersBuilder = new Headers.Builder();
        headers.forEach(headersBuilder::add);
        return headersBuilder.build();
    }

    private Map<String, String> toParams(RemoteExtensionQuery query) {
        return objectMapper.convertValue(query, new TypeReference<Map<String, String>>() {
        });
    }

    /**
     * Thrown when there is an issue performing a call to the remote extension.
     */
    class OkHttpRemoteExtensionTransportException extends RuntimeException {
        public OkHttpRemoteExtensionTransportException(String reason) {
            super("Unable to invoke remote extension due to unexpected error: " + reason);
        }

        public OkHttpRemoteExtensionTransportException(Throwable cause) {
            super(cause);
        }
    }
}
