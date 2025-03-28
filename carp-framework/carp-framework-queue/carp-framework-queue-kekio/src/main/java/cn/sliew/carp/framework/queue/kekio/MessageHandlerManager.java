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
package cn.sliew.carp.framework.queue.kekio;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageHandlerManager {

    // row, column, values
    private Table<String, String, List<MessageHandler>> registry = HashBasedTable.create();

    public Map<String, List<MessageHandler>> get(String topic) {
        return registry.row(topic);
    }

    public void register(String topic, String consumerGroup, MessageHandler handler) {
        Map<String, List<MessageHandler>> handlerGroup = registry.row(topic);
        List<MessageHandler> handlers = handlerGroup.computeIfAbsent(consumerGroup, key -> new ArrayList<>());
        if (handlers.contains(handler) == false) {
            handlers.add(handler);
        }
    }

    public void remove(String topic, String consumerGroup, MessageHandler handler) {
        Map<String, List<MessageHandler>> handlerGroup = registry.row(topic);
        List<MessageHandler> handlers = handlerGroup.computeIfAbsent(consumerGroup, key -> new ArrayList<>());
        if (handlers.contains(handler)) {
            handlers.remove(handler);
        }
    }
}
