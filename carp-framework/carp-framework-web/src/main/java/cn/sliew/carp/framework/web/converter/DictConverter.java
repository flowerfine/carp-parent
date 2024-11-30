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

package cn.sliew.carp.framework.web.converter;

import cn.sliew.carp.framework.common.dict.DictInstance;
import org.springframework.core.convert.converter.Converter;

import java.util.HashMap;
import java.util.Map;

public class DictConverter<T extends DictInstance> implements Converter<String, T> {

    private final Map<String, T> dictMap = new HashMap<>();

    public DictConverter(Class<T> dictType) {
        T[] enums = dictType.getEnumConstants();
        for (T e : enums) {
            dictMap.put(e.getValue(), e);
        }
    }

    @Override
    public T convert(String source) {
        return dictMap.get(source);
    }
}
