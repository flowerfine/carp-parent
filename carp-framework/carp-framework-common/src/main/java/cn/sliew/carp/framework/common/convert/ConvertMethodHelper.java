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
package cn.sliew.carp.framework.common.convert;

import cn.sliew.milky.common.util.JacksonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.Objects;

@Mapper
public interface ConvertMethodHelper {

    @Named("toJsonNode")
    default JsonNode toJsonNode(Object object) {
        if (Objects.nonNull(object)) {
            if (object instanceof String string) {
                return JacksonUtil.toJsonNode(string);
            }
            return JacksonUtil.toJsonNode(object);
        }
        return null;
    }

    @Named("toJsonString")
    default String toJsonString(Object object) {
        if (Objects.nonNull(object)) {
            return JacksonUtil.toJsonString(object);
        }
        return null;
    }
}
