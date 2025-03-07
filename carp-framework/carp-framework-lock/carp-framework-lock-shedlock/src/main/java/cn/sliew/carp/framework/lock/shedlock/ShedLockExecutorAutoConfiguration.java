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
package cn.sliew.carp.framework.lock.shedlock;

import cn.sliew.carp.framework.common.lock.LockAndRunExecutor;
import com.zaxxer.hikari.HikariDataSource;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@ConditionalOnMissingBean(LockAndRunExecutor.class)
@ConditionalOnProperty(value = "carp.framework.lock.shedlock.enabled", havingValue = "true", matchIfMissing = false)
public class ShedLockExecutorAutoConfiguration {

    public static final String DATA_SOURCE_FACTORY = "cn.sliew.carp.lock.shedlock.DataSource";

    @Bean(ShedLockExecutorAutoConfiguration.DATA_SOURCE_FACTORY)
    @ConfigurationProperties(prefix = "spring.datasource.shedlock")
    public DataSource shedLockDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(LockProvider.class)
    public JdbcTemplateLockProvider jdbcTemplateLockProvider(@Qualifier(ShedLockExecutorAutoConfiguration.DATA_SOURCE_FACTORY) DataSource shedLockDataSource) {
        return new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .withTableName("shedlock")
                        .withJdbcTemplate(new JdbcTemplate(shedLockDataSource))
                        .usingDbTime()
                        .build()
        );
    }

    @Bean
    public ShedLockLockAndRunExecutor shedLockLockAndRunExecutor(LockProvider lockProvider) {
        return new ShedLockLockAndRunExecutor(lockProvider);
    }
}
