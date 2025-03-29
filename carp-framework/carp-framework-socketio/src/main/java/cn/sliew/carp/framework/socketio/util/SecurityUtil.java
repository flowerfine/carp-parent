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
package cn.sliew.carp.framework.socketio.util;

import com.corundumstudio.socketio.HandshakeData;
import com.google.common.base.Splitter;
import io.netty.handler.codec.http.HttpHeaders;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Objects;

public enum SecurityUtil {
    ;

    public static String getAuthToken(HandshakeData data, String tokenKey) {
        String authTokenString = null;
        // authToken
        Object authToken = data.getAuthToken();
        if (Objects.nonNull(authToken)) {
            if (authToken instanceof Map<?, ?> authTokenMap) {
                authTokenString = (String) authTokenMap.get(tokenKey);
            }
        }
        if (StringUtils.hasText(authTokenString)) {
            return authTokenString;
        }

        HttpHeaders headers = data.getHttpHeaders();
        // cookie
        String cookie = headers.get("Cookie");
        if (StringUtils.hasText(cookie)) {
            Map<String, String> cookieMap = Splitter.on(";").withKeyValueSeparator("=").split(cookie);
            authTokenString = cookieMap.get(tokenKey);
            if (StringUtils.hasText(authTokenString)) {
                return authTokenString;
            }
        }

        // header
        authTokenString = headers.get(tokenKey);
        if (StringUtils.hasText(authTokenString)) {
            return authTokenString;
        }

        // query
        authTokenString = data.getSingleUrlParam(tokenKey);
        if (StringUtils.hasText(authTokenString)) {
            return authTokenString;
        }

        return null;
    }

    public static String getUserId(HandshakeData data, String userIdKey) {
        String userIdString = null;
        // authToken
        Object authToken = data.getAuthToken();
        if (Objects.nonNull(authToken)) {
            userIdString = getUserIdFromAuthToken(authToken, userIdKey);
        }
        if (StringUtils.hasText(userIdString)) {
            return userIdString;
        }
        return null;
    }


    public static String getUserIdFromAuthToken(Object authToken, String userIdKey) {
        String userIdString = null;
        // authToken
        if (Objects.nonNull(authToken)) {
            if (authToken instanceof Map<?, ?> authTokenMap) {
                userIdString = (String) authTokenMap.get(userIdKey);
            }
        }
        if (StringUtils.hasText(userIdString)) {
            return userIdString;
        }
        return null;
    }

}
