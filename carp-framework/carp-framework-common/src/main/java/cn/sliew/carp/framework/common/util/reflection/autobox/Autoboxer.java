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

import java.util.Arrays;
import java.util.List;

public enum Autoboxer {
    ;

    private static final List<TypeAutoboxer> autoboxers = Arrays.asList(
            new BooleanTypeAutoboxer(),
            new InstantTypeAutoboxer(),
            new IntegerTypeAutoboxer(),
            new LongTypeAutoboxer(),
            new DoubleTypeAutoboxer(),
            new FloatTypeAutoboxer(),
            new StringTypeAutoboxer(),
            new UUIDTypeAutoboxer(),
            new EnumAutoboxer(),
            new DurationTypeAutoboxer()
    );

    @SuppressWarnings("unchecked")
    public static <T> T autobox(Object value, Class<T> type) {
        if (value == null) return null;
        if (type.isAssignableFrom(value.getClass())) {
            return ReflectionUtils.cast(value);
        }

        return ReflectionUtils.cast(autoboxers.stream()
                .filter(autoboxer -> autoboxer.supports(type))
                .findFirst()
                .map(autoboxer -> autoboxer.autobox(value, type))
                .orElseThrow(() -> new UnsupportedOperationException(String.format("Cannot autobox %s of type %s to %s", value, value.getClass().getName(), type.getName()))));

    }
}
