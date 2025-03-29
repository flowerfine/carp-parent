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

import cn.sliew.carp.framework.common.util.KeyUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class SocketIOConnectionManager implements InitializingBean, DisposableBean {

    private static RMap<String, List<UUID>> USER_SESSIONID_MAP = null;

    private RedissonClient redissonClient;

    public SocketIOConnectionManager(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        SocketIOConnectionManager.USER_SESSIONID_MAP = redissonClient.getMap(KeyUtil.buildCacheKey("socket.io.user2sessionIds"));
    }

    @Override
    public void destroy() throws Exception {
        if (Objects.nonNull(USER_SESSIONID_MAP)) {
            USER_SESSIONID_MAP.destroy();
        }
    }

    public static void addSessionId(String userId, UUID sessionId) {
        RMap<String, List<UUID>> sessionMap = getSessionMap();
        if (sessionMap.containsKey(userId)) {
            List<UUID> sessionIds = sessionMap.get(userId);
            sessionIds.add(sessionId);
        } else {
            sessionMap.put(userId, Lists.newArrayList(sessionId));
        }
    }

    public static void removeSessionId(String userId, UUID sessionId) {
        RMap<String, List<UUID>> sessionMap = getSessionMap();
        if (sessionMap.containsKey(userId)) {
            List<UUID> sessionIds = sessionMap.get(userId);
            sessionIds.remove(sessionId);
            if (CollectionUtils.isEmpty(sessionIds)) {
                sessionMap.remove(userId);
            }
        }
    }

    public static List<UUID> getSessionIds(String userId) {
        RMap<String, List<UUID>> sessionMap = getSessionMap();
        return sessionMap.getOrDefault(userId, Collections.emptyList());
    }

    private static RMap<String, List<UUID>> getSessionMap() {
        if (Objects.isNull(USER_SESSIONID_MAP)) {
            throw new IllegalStateException("USER_SESSIONID_MAP not initialized");
        }
        return USER_SESSIONID_MAP;
    }
}
