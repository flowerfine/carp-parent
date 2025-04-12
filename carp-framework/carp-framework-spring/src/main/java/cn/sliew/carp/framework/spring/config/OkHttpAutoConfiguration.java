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
package cn.sliew.carp.framework.spring.config;

import okhttp3.Interceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.util.CollectionUtils;

import java.util.List;

@AutoConfiguration
public class OkHttpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(okhttp3.OkHttpClient.class)
    public okhttp3.OkHttpClient okHttpClient(@Autowired(required = false) List<Interceptor> interceptors) {
        okhttp3.OkHttpClient.Builder builder = new okhttp3.OkHttpClient.Builder();
        if (!CollectionUtils.isEmpty(interceptors)) {
            interceptors.forEach(builder::addInterceptor);
        }
        return builder.build();
    }
}
