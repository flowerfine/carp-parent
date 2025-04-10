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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.util.CloseableIterator;

import java.io.IOException;
import java.util.Objects;

public class RedisStreamIterator<T> implements CloseableIterator<T> {

    private final RedisStreamFetcher<T> fetcher;
    private T bufferedResult;

    public RedisStreamIterator(@Nonnull StringRedisTemplate redisTemplate,
                               @Nonnull Class<T> targetClass,
                               @Nonnull String streamKey) {
        this.fetcher = new RedisStreamFetcher<>(
                Objects.requireNonNull(redisTemplate, "redisTemplate"),
                Objects.requireNonNull(targetClass, "targetClass"),
                Objects.requireNonNull(streamKey, "streamKey"));
    }

    @Override
    public boolean hasNext() {
        // we have to make sure that the next result exists
        // it is possible that there is no more result but the job is still running
        if (bufferedResult == null) {
            bufferedResult = nextResultFromFetcher();
        }
        return bufferedResult != null;
    }

    @Override
    public T next() {
        if (bufferedResult == null) {
            bufferedResult = nextResultFromFetcher();
        }
        T ret = bufferedResult;
        bufferedResult = null;
        return ret;
    }

    @Override
    public void close() {
        fetcher.close();
    }

    private T nextResultFromFetcher() {
        try {
            return fetcher.next();
        } catch (IOException e) {
            fetcher.close();
            throw new RuntimeException("Failed to fetch next result", e);
        }
    }
}
