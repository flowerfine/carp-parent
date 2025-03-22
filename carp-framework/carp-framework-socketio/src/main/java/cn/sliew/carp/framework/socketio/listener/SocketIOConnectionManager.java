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

import cn.sliew.milky.common.util.MapUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.DisposableBean;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

public class SocketIOConnectionManager implements DisposableBean {

    private static final ConcurrentMap<String, List<UUID>> USER_SESSIONID_MAP = Maps.newConcurrentMap();

    @Override
    public void destroy() throws Exception {
        USER_SESSIONID_MAP.clear();
    }

    public static void addSessionId(String userId, UUID sessionId) {
        MapUtil.computeIfAbsent(USER_SESSIONID_MAP, userId, k -> Lists.newArrayList()).add(sessionId);
    }

    public static void removeSessionId(String userId, UUID sessionId) {
        if (USER_SESSIONID_MAP.containsKey(userId)) {
            List<UUID> sessionIds = USER_SESSIONID_MAP.get(userId);
            sessionIds.remove(sessionId);
            if (CollectionUtils.isEmpty(sessionIds)) {
                USER_SESSIONID_MAP.remove(userId);
            }
        }
    }

    public static List<UUID> getSessionIds(String userId) {
        return USER_SESSIONID_MAP.getOrDefault(userId, Collections.emptyList());
    }
}
