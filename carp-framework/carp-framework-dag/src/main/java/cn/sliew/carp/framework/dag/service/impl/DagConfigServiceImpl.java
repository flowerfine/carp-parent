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
package cn.sliew.carp.framework.dag.service.impl;

import cn.sliew.carp.framework.common.util.UUIDUtil;
import cn.sliew.carp.framework.dag.repository.entity.DagConfig;
import cn.sliew.carp.framework.dag.repository.mapper.DagConfigMapper;
import cn.sliew.carp.framework.dag.service.DagConfigService;
import cn.sliew.carp.framework.dag.service.convert.DagConfigConvert;
import cn.sliew.carp.framework.dag.service.dto.DagConfigDTO;
import cn.sliew.carp.framework.mybatis.DataSourceConstants;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
public class DagConfigServiceImpl extends ServiceImpl<DagConfigMapper, DagConfig> implements DagConfigService {

    @Override
    public DagConfigDTO get(Long id) {
        DagConfig entity = getOptById(id).orElseThrow(() -> new IllegalArgumentException("dag config not exists for id: " + id));
        return DagConfigConvert.INSTANCE.toDto(entity);
    }

    @Override
    public Long add(DagConfigDTO configDTO) {
        DagConfig record = DagConfigConvert.INSTANCE.toDo(configDTO);
        record.setUuid(UUIDUtil.randomUUId());
        record.setVersion(0);
        save(record);
        return record.getId();
    }

    @Override
    public boolean update(DagConfigDTO configDTO) {
        DagConfig record = DagConfigConvert.INSTANCE.toDo(configDTO);
        return updateById(record);
    }

    @Override
    public void upsert(DagConfigDTO configDTO) {
        if (configDTO.getId() != null) {
            update(configDTO);
        } else {
            add(configDTO);
        }
    }

    @Override
    public boolean delete(Long id) {
        return removeById(id);
    }

    @Transactional(rollbackFor = {Exception.class}, transactionManager = DataSourceConstants.TRANSACTION_MANAGER_FACTORY)
    @Override
    public boolean deleteBatch(Collection<Long> ids) {
        return removeBatchByIds(ids);
    }

    @Override
    public Long clone(Long id) {
        DagConfigDTO configDTO = get(id);
        configDTO.setId(null);
        configDTO.setCreator(null);
        configDTO.setCreateTime(null);
        configDTO.setEditor(null);
        configDTO.setUpdateTime(null);
        configDTO.setName(configDTO.getName() + "_copy_" + UUIDUtil.randomUUId());
        configDTO.setUuid(UUIDUtil.randomUUId());
        return add(configDTO);
    }
}
