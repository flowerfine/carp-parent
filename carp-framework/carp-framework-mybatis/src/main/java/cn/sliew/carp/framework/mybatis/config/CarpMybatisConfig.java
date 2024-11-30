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
package cn.sliew.carp.framework.mybatis.config;

import cn.sliew.carp.framework.common.security.CarpSecurityContext;
import cn.sliew.carp.framework.common.security.OnlineUserInfo;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Date;

@Configuration
public class CarpMybatisConfig {

    @Bean
    public MybatisPlusInterceptor carpPaginationInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * fixme delete and deleteTime
     */
    @Component
    public static class CarpMetaHandler implements MetaObjectHandler {

        @Override
        public void insertFill(MetaObject metaObject) {
            String userName = getUserNameOrDefault();
            this.strictInsertFill(metaObject, "creator", () -> userName, String.class);
            this.strictInsertFill(metaObject, "createTime", () -> new Date(), Date.class);
            this.strictInsertFill(metaObject, "editor", () -> userName, String.class);
            this.strictInsertFill(metaObject, "updateTime", () -> new Date(), Date.class);
        }

        @Override
        public void updateFill(MetaObject metaObject) {
            String userName = getUserNameOrDefault();
            this.strictUpdateFill(metaObject, "editor", () -> userName, String.class);
            this.strictUpdateFill(metaObject, "updateTime", () -> new Date(), Date.class);
        }

        private String getUserNameOrDefault() {
            OnlineUserInfo userInfo = CarpSecurityContext.get();
            if (userInfo != null) {
                return userInfo.getNickName();
            }
            return "sys";
        }

    }
}
