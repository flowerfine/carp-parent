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

import cn.sliew.carp.framework.queue.kekio.message.Message;
import org.springframework.context.SmartLifecycle;

import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.util.List;

public interface Queue extends SmartLifecycle {

    /**
     * The maximum number of times an un-acknowledged message will be retried
     * before failing permanently.
     */
    int MAX_RETRIES = 5;

    /**
     * Polls the queue for ready messages.
     * <p>
     * Implementations may invoke [callback] any number of times. Some
     * implementations may deliver a maximum of one message per call, others may
     * deliver all ready messages.
     * <p>
     * If no messages exist on the queue or all messages have a remaining delay
     * [callback] is not invoked.
     * <p>
     * Messages *must* be acknowledged by calling the function passed to
     * [callback] or they will be retried after [ackTimeout]. Acknowledging via a
     * nested callback allows the message to be processed asynchronously.
     *
     * @param callback invoked with the next message from the queue if there is
     *                 one and an _acknowledge_ function to call once processing is complete.
     */
    void poll(QueueCallback callback);

    /**
     * Polls the queue for ready messages, processing up-to [maxMessages].
     */
    default void poll(int maxMessages, QueueCallback callback) {
        for (int i = 0; i < maxMessages; i++) {
            poll(callback);
        }
    }

    /**
     * Push [message] for immediate delivery.
     */
    default void push(Message message) {
        push(message, Duration.ZERO);
    }

    /**
     * Push [message] for delivery after [delay].
     */
    void push(Message message, TemporalAmount delay);

    /**
     * Check for any un-acknowledged messages that are overdue and move them back
     * onto the queue.
     * <p>
     * This method is not intended to be called by clients directly but typically
     * scheduled in some way.
     */
    default void retry() {
    }

    /**
     * Used for testing zombie executions. Wipes all messages from the queue.
     */
    default void clear() {
    }

    /**
     * The expired time after which un-acknowledged messages will be retried.
     */
    TemporalAmount getAckTimeout();

    /**
     * A handler for messages that have failed to acknowledge delivery more than
     * [Queue.ackTimeout] times.
     */
    List<DeadMessageCallback> getDeadMessageHandlers();

    /**
     * Denotes a queue implementation capable of processing multiple messages per poll.
     */
    Boolean canPollMany();

    /**
     * The callback parameter type passed to [Queue.poll]. The queue implementation
     * will invoke the callback passing the next message from the queue and an "ack"
     * function used to acknowledge successful processing of the message.
     */
    @FunctionalInterface
    interface QueueCallback {
        void accept(Message message, Runnable ack);
    }

    @FunctionalInterface
    interface DeadMessageCallback {
        void accept(Queue queue, Message message);
    }
}
