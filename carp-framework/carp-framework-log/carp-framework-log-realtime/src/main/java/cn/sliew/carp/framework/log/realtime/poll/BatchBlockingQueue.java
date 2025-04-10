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
package cn.sliew.carp.framework.log.realtime.poll;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

class BatchBlockingQueue<T> {

    private final ArrayBlockingQueue<T> queue;
    private final Clock clock;

    BatchBlockingQueue(int capacity) {
        this(capacity, Clock.systemDefaultZone());
    }

    BatchBlockingQueue(int capacity, Clock clock) {
        this.queue = new ArrayBlockingQueue(capacity);
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public void put(T element) throws InterruptedException {
        queue.put(element);
    }

    public List<T> pollBatch(int maxBatchSize, Duration timeout) throws InterruptedException {
        ArrayList<T> result = new ArrayList(maxBatchSize);
        Instant deadline = clock.instant().plus(timeout);

        while (true) {
            Instant now = clock.instant();
            Duration timeLeft = Duration.between(now, deadline);
            if (result.size() >= maxBatchSize) {
                return result;
            }

            if (timeLeft.isNegative()) {
                return result;
            }

            T element = queue.poll(timeLeft.toMillis(), TimeUnit.MILLISECONDS);
            if (element != null) {
                result.add(element);
                int spaceLeft = maxBatchSize - result.size();
                queue.drainTo(result, spaceLeft);
            }
        }
    }
}
