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
package cn.sliew.carp.framework.common.jackson.subtype;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * Parses {@link JsonTypeName} annotations on classes to determine the type discriminator.
 */
@Slf4j
@RequiredArgsConstructor
public class JsonTypeNameParser implements NamedTypeParser {
    private final boolean strictSerialization;

    @Nullable
    @Override
    public NamedType parse(@Nonnull Class<?> type) {
        JsonTypeName nameAnnotation = type.getAnnotation(JsonTypeName.class);
        if (Objects.isNull(nameAnnotation) || StringUtils.isBlank(nameAnnotation.value())) {
            String message =
                    "Subtype " + type.getSimpleName() + " does not have a JsonTypeName annotation";
            if (strictSerialization) {
                throw new InvalidSubtypeConfigurationException(message);
            }
            log.warn(message);
            return null;
        }

        return new NamedType(type, nameAnnotation.value());
    }
}
