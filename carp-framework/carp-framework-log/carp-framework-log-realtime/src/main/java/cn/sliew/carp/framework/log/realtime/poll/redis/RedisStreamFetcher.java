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
package cn.sliew.carp.framework.log.realtime.poll.redis;

import jakarta.annotation.Nonnull;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;

public class RedisStreamFetcher<T> {

    private final StringRedisTemplate redisTemplate;
    private final Class<T> targetClass;
    private final String streamKey;

    private StreamOffset streamOffset;
    private ArrayBlockingQueue<T> buffer = new ArrayBlockingQueue(100);
    private boolean closed;

    public RedisStreamFetcher(@Nonnull StringRedisTemplate redisTemplate,
                              @Nonnull Class<T> targetClass,
                              @Nonnull String streamKey) {
        this.redisTemplate = Objects.requireNonNull(redisTemplate, "redisTemplate");
        this.targetClass = Objects.requireNonNull(targetClass, "targetClass");
        this.streamKey = Objects.requireNonNull(streamKey, "streamKey");
        this.streamOffset = StreamOffset.fromStart(streamKey);
    }

    public T next() throws IOException {
        if (closed) {
            return null;
        }

        do {
            T element = buffer.poll();
            if (element != null) {
                return element;
            } else {
                // 拉取 redis stream 数据，多余数据缓存在本地
                List<ObjectRecord<String, T>> objectRecords = redisTemplate.opsForStream()
                        .read(targetClass, streamOffset);
                if (CollectionUtils.isEmpty(objectRecords) == false) {
                    for (ObjectRecord<String, T> objectRecord : objectRecords) {
                        if (buffer.offer(objectRecord.getValue())) {
                            streamOffset = StreamOffset.from(objectRecord);
                        } else {
                            break;
                        }
                    }
                }
            }
        } while (isStreamExists());
        return null;
    }

    public void close() {
        if (!closed) {
            closed = true;
        }
    }

    private boolean isStreamExists() {
        return redisTemplate.hasKey(streamKey);
    }
}
