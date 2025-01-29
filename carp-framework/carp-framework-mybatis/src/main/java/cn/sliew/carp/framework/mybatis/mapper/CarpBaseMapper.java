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
package cn.sliew.carp.framework.mybatis.mapper;

import cn.hutool.core.util.ClassUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import com.baomidou.mybatisplus.extension.toolkit.Db;

import java.util.Collection;

public interface CarpBaseMapper<T> extends BaseMapper<T> {

    default boolean insertBatch(Collection<T> entityList) {
        return Db.saveBatch(entityList);
    }

    default QueryChainWrapper<T> query() {
        return ChainWrappers.queryChain(this);
    }

    default LambdaQueryChainWrapper<T> lambdaQuery() {
        return ChainWrappers.lambdaQueryChain(this, this.currentEntityClass());
    }

    default LambdaQueryChainWrapper<T> lambdaQuery(T entity) {
        return ChainWrappers.lambdaQueryChain(this, entity);
    }

    default UpdateChainWrapper<T> update() {
        return ChainWrappers.updateChain(this);
    }

    default LambdaUpdateChainWrapper<T> lambdaUpdate() {
        return ChainWrappers.lambdaUpdateChain(this);
    }

    default Class<T> currentEntityClass() {
        return (Class<T>) ClassUtil.getTypeArgument(this.getClass(), 0);
    }
}
