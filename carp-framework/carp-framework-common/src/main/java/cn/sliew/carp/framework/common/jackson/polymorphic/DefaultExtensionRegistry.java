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
package cn.sliew.carp.framework.common.jackson.polymorphic;

import cn.sliew.milky.common.util.JacksonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.*;

public class DefaultExtensionRegistry implements ExtensionRegistry {

    public static final ExtensionRegistry INSTANCE = new DefaultExtensionRegistry(Collections.singletonList(JacksonUtil.getMapper()));

    private List<ObjectMapper> mappers = new ArrayList<>();
    private Table<Class, String, Class> table = HashBasedTable.create();

    public DefaultExtensionRegistry(List<ObjectMapper> mappers) {
        this.mappers = mappers;
    }

    @Override
    public <Base> void register(Class<Base> baseType, Class<? extends Base> extensionType, String discriminator) {
        Map<String, Class> map = table.row(baseType);
        map.put(discriminator, extensionType);
        for (ObjectMapper mapper : mappers) {
            mapper.registerSubtypes(new NamedType(extensionType, discriminator));
        }
    }

    @Override
    public Map<String, Class> extensionsOf(Class baseType) {
        return table.row(baseType);
    }

    @Override
    public Collection<Class> baseTypes() {
        return table.rowKeySet();
    }
}
