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
package cn.sliew.carp.framework.common.dict;

import cn.hutool.core.util.ArrayUtil;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.apache.commons.lang3.EnumUtils;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public enum EnumDictRegistry implements DictRegistry {
    INSTANCE;

    private static final Table<DictDefinition, String, DictInstance> DICT_TABLE = HashBasedTable.create();

    static {
        List<DictType> enumList = EnumUtils.getEnumList(DictType.class);
        for (DictType dictType : enumList) {
            List values = EnumUtils.getEnumList(dictType.getInstanceClass());
            register(dictType, values);
        }
    }

    public static void register(DictDefinition definition, List<DictInstance> instances) {
        if (ArrayUtil.isNotEmpty(instances)) {
            for (DictInstance instance : instances) {
                DICT_TABLE.put(definition, instance.getValue(), instance);
            }
        }
    }

    @Override
    public Collection<DictDefinition> getAllDefinitions() {
        return DICT_TABLE.rowKeySet();
    }

    @Override
    public Optional<DictDefinition> getDictDefinition(String code) {
        return getAllDefinitions().stream()
                .filter(dictDefinition -> dictDefinition.getCode().equals(code))
                .findFirst();
    }

    @Override
    public Collection<DictInstance> getAllInstances() {
        return DICT_TABLE.values();
    }

    @Override
    public Collection<DictInstance> getDictInstance(DictDefinition definition) {
        return DICT_TABLE.row(definition).values();
    }

    @Override
    public boolean exists(DictDefinition definition, String instanceName) {
        return DICT_TABLE.contains(DICT_TABLE, instanceName);
    }

    @Override
    public Optional<DictInstance> getDictInstance(DictDefinition definition, String instanceName) {
        return Optional.ofNullable(DICT_TABLE.get(definition, instanceName));
    }
}
