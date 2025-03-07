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
package cn.sliew.carp.framework.lock.redis;

import cn.sliew.carp.framework.common.lock.LockAndRunExecutor;
import cn.sliew.carp.framework.redis.RedissonUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@ConditionalOnMissingBean(LockAndRunExecutor.class)
@ConditionalOnProperty(value = "carp.framework.lock.redis.enabled", havingValue = "true", matchIfMissing = false)
public class RedisLockAndRunAutoConfiguration {

    @Bean
    @ConditionalOnBean(RedissonUtil.class)
    public RedisLockAndRunExecutor redisLockAndRunExecutor(RedissonUtil redissonUtil) {
        return new RedisLockAndRunExecutor(redissonUtil);
    }
}
