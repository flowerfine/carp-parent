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

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;

import static java.time.ZoneId.systemDefault;

public class InstantTypeAutoboxer implements TypeAutoboxer<Instant> {
    @Override
    public boolean supports(Class<?> type) {
        return Instant.class.equals(type);
    }

    @Override
    public Instant autobox(Object value, Class<Instant> type) {
        if (value instanceof Timestamp) {
            return ReflectionUtils.cast(((Timestamp) value).toInstant());
        } else if (value instanceof Long) {
            return ReflectionUtils.cast(new Timestamp((Long) value).toInstant());
        } else if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).atZone(systemDefault()).toInstant();
        } else if (value instanceof CharSequence) {
            return Instant.parse((CharSequence) value);
        }
        throw new UnsupportedOperationException(String.format("Cannot autobox %s of type %s to %s", value, value.getClass().getName(), Instant.class.getName()));
    }
}
