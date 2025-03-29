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
package cn.sliew.carp.framework.pubsub.queue.kekio;

import cn.sliew.carp.framework.pubsub.model.AbstractPubsubChannel;
import cn.sliew.carp.framework.pubsub.model.PubsubChannel;
import cn.sliew.carp.framework.pubsub.model.PubsubSubscriber;
import cn.sliew.carp.framework.queue.kekio.AbstractQueue;
import cn.sliew.carp.framework.queue.kekio.Queue;
import cn.sliew.carp.framework.queue.kekio.QueueProcessor;
import cn.sliew.carp.framework.queue.kekio.message.CommonMessage;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class QueuePubsubChannel extends AbstractPubsubChannel implements PubsubChannel {

    private String name;
    private Queue queue;
    private QueueProcessor processor;

    public QueuePubsubChannel(String name, Queue queue) {
        this.name = name;
        this.queue = queue;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void register(PubsubSubscriber subscriber) {
        if (subscriber instanceof QueuePubsubSubscriber queuePubsubSubscriber) {
            processor.addMessageHandler(queuePubsubSubscriber.getMessageHandler());
        }
    }

    @Override
    public void remove(PubsubSubscriber subscriber) {
        if (subscriber instanceof QueuePubsubSubscriber queuePubsubSubscriber) {
            processor.removeMessageHandler(queuePubsubSubscriber.getMessageHandler());
        }
    }

    @Override
    public void push(String message, Duration delay) {
        CommonMessage commonMessage = CommonMessage.builder()
                .body(message.getBytes(StandardCharsets.UTF_8))
                .build();
        queue.push(commonMessage, delay);
    }

    @Override
    protected void doStart() throws Exception {
        queue.start();
        if (queue instanceof AbstractQueue abstractQueue) {
            this.processor = abstractQueue.getProcessor();
        }
    }

    @Override
    protected void doStop() throws Exception {
        queue.stop();
    }
}
