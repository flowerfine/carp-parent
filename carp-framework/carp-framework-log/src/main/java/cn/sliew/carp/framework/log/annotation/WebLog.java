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
package cn.sliew.carp.framework.log.annotation;

import cn.sliew.carp.framework.log.enums.LogEntity;

import java.lang.annotation.*;

@Inherited
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface WebLog {

    /**
     * 是否忽略日志记录（用于接口方法或类上）
     */
    boolean ignore() default false;

    /**
     * 所属模块（用于接口方法或类上）
     */
    String module() default "";

    /**
     * 日志描述（仅用于接口方法上）
     */
    String desc() default "";

    LogEntity[] includes() default {};
    LogEntity[] excludes() default {};
}
