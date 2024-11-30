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
package cn.sliew.carp.framework.common.util.reflection.autobox;

import cn.sliew.carp.framework.common.util.reflection.ReflectionUtils;

import java.math.BigDecimal;

public class LongTypeAutoboxer implements TypeAutoboxer<Long> {
    @Override
    public boolean supports(Class<?> type) {
        return long.class.equals(type) || Long.class.equals(type);
    }

    @Override
    public Long autobox(Object value, Class<Long> type) {
        if (value instanceof Long) {
            return ReflectionUtils.cast(value);
        } else if (value instanceof BigDecimal) {
            return ReflectionUtils.cast(((BigDecimal) value).longValue());
        } else if (value instanceof Integer) {
            return ReflectionUtils.cast(Long.valueOf((Integer) value));
        } else if (value instanceof String) {
            return ReflectionUtils.cast(Long.valueOf((String) value));
        }
        throw new UnsupportedOperationException(String.format("Cannot autobox %s of type %s to %s", value, value.getClass().getName(), Long.class.getName()));
    }
}
