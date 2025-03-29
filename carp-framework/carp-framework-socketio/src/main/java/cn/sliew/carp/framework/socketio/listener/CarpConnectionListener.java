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
package cn.sliew.carp.framework.socketio.listener;

import cn.sliew.carp.framework.common.security.CarpSecurityContext;
import cn.sliew.carp.framework.common.security.OnlineUserInfo;
import cn.sliew.carp.framework.common.security.SecurityConstants;
import cn.sliew.carp.framework.socketio.util.SecurityUtil;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public interface CarpConnectionListener {

    void setNamespace(SocketIONamespace namespace);

    SocketIONamespace getNamespace();

    void onConnect(SocketIOClient client);

    void onDisConnect(SocketIOClient client);

    default void sendBroadcastMessage(String userId, String name, Object data) {
        List<UUID> sessionIds = SocketIOConnectionManager.getSessionIds(userId);
        getNamespace().getBroadcastOperations()
                .sendEvent(name, client -> !sessionIds.contains(client.getSessionId()), data);
    }

    default void connect(SocketIOClient client) {
        String userId = getUserId(client);
        SocketIOConnectionManager.addSessionId(userId, client.getSessionId());
    }

    default void disconnect(SocketIOClient client) {
        String userId = getUserId(client);
        SocketIOConnectionManager.removeSessionId(userId, client.getSessionId());
    }

    default String getUserId(SocketIOClient client) {
        String userId = null;
        OnlineUserInfo onlineUserInfo = CarpSecurityContext.get();
        if (Objects.nonNull(onlineUserInfo)) {
            userId = onlineUserInfo.getUserId().toString();
        }
        if (StringUtils.isBlank(userId)) {
            userId = SecurityUtil.getUserId(client.getHandshakeData(), SecurityConstants.AUTHORIZATION_USER_ID_KEY);
        }
        if (StringUtils.isBlank(userId)) {
            userId = client.getSessionId().toString();
        }
        return userId;
    }
}
