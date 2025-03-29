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
package cn.sliew.carp.framework.socketio.repository;

import cn.sliew.carp.framework.common.util.KeyUtil;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import org.apache.commons.collections4.CollectionUtils;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public class DefaultSocketIORepository implements SocketIORepository {

    private RedissonClient redissonClient;
    private SocketIOServer socketIOServer;

    public DefaultSocketIORepository(RedissonClient redissonClient, SocketIOServer socketIOServer) {
        this.redissonClient = redissonClient;
        this.socketIOServer = socketIOServer;
    }

    @Override
    public SocketIONamespace getNamespace(String namespace) {
        return socketIOServer.getNamespace(namespace);
    }

    @Override
    public SocketIOClient getClient(String namespace, UUID sessionId) {
        return getNamespace(namespace).getClient(sessionId);
    }

    @Override
    public void addSessionId(String namespace, String key, UUID sessionId) {
        RList<UUID> sessionList = getSessionList(namespace, key);
        sessionList.add(sessionId);
    }

    @Override
    public void removeSessionId(String namespace, String key, UUID sessionId) {
        RList<UUID> sessionList = getSessionList(namespace, key);
        if (CollectionUtils.isEmpty(sessionList)) {
            sessionList.deleteAsync();
        }
        sessionList.removeAsync(sessionId);
    }

    @Override
    public List<UUID> getSessionIds(String namespace, String key) {
        return getSessionList(namespace, key);
    }

    private RList<UUID> getSessionList(String namespace, String key) {
        String redisKey = getSessionListKey(namespace, key);

        RList<UUID> list = redissonClient.getList(redisKey);
        list.expireAsync(Duration.ofDays(1L));
        return list;
    }

    private String getSessionListKey(String namespace, String key) {
        return KeyUtil.buildCacheKey("socket.io.sessionIds", namespace, key);
    }
}
