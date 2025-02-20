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

import cn.sliew.carp.framework.common.model.PageResult;
import cn.sliew.carp.framework.common.util.UUIDUtil;
import cn.sliew.carp.framework.dag.repository.entity.DagInstance;
import cn.sliew.carp.framework.dag.repository.mapper.DagInstanceMapper;
import cn.sliew.carp.framework.dag.service.DagConfigComplexService;
import cn.sliew.carp.framework.dag.service.DagInstanceService;
import cn.sliew.carp.framework.dag.service.convert.DagInstanceConvert;
import cn.sliew.carp.framework.dag.service.dto.DagInstanceDTO;
import cn.sliew.carp.framework.dag.service.param.DagInstanceSimplePageParam;
import cn.sliew.carp.framework.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DagInstanceServiceImpl extends ServiceImpl<DagInstanceMapper, DagInstance> implements DagInstanceService {

    @Autowired
    private DagConfigComplexService dagConfigComplexService;

    @Override
    public PageResult<DagInstanceDTO> page(DagInstanceSimplePageParam param) {
        Page<DagInstance> page = PageUtil.buildBasePageParam(param);
        LambdaQueryWrapper<DagInstance> queryWrapper = Wrappers.lambdaQuery(DagInstance.class)
                .eq(DagInstance::getNamespace, param.getNamespace())
                .eq(param.getDagConfigID() != null, DagInstance::getDagConfigId, param.getDagConfigID())
                .eq(StringUtils.hasText(param.getUuid()), DagInstance::getUuid, param.getUuid())
                .eq(StringUtils.hasText(param.getStatus()), DagInstance::getStatus, param.getStatus())
                .orderByDesc(DagInstance::getId);
        Page<DagInstance> dagInstancePage = page(page, queryWrapper);
        return PageUtil.buildPageResult(dagInstancePage, DagInstanceConvert.INSTANCE::toDto);
    }

    @Override
    public DagInstanceDTO get(Long id) {
        DagInstance entity = getOptById(id).orElseThrow(() -> new IllegalArgumentException("dag instance not exists for id: " + id));
        return DagInstanceConvert.INSTANCE.toDto(entity);
    }

    @Override
    public DagInstanceDTO getWithConfig(Long id) {
        DagInstanceDTO dagInstanceDTO = get(id);
        dagInstanceDTO.setDagConfig(dagConfigComplexService.selectOne(dagInstanceDTO.getDagConfig().getId()));
        return dagInstanceDTO;
    }

    @Override
    public Long add(DagInstanceDTO instanceDTO) {
        DagInstance record = DagInstanceConvert.INSTANCE.toDo(instanceDTO);
        if (StringUtils.hasText(record.getBody()) == false) {
            record.setUuid(UUIDUtil.randomUUId());
        }
        save(record);
        return record.getId();
    }

    @Override
    public boolean update(DagInstanceDTO instanceDTO) {
        DagInstance record = DagInstanceConvert.INSTANCE.toDo(instanceDTO);
        return updateById(record);
    }

    @Override
    public boolean updateStatus(Long id, String fromStatus, String toStatus) {
        LambdaUpdateWrapper<DagInstance> wrapper = Wrappers.lambdaUpdate(DagInstance.class)
                .eq(DagInstance::getId, id)
                .eq(StringUtils.hasText(fromStatus), DagInstance::getStatus, fromStatus)
                .set(DagInstance::getStatus, toStatus);
        return update(wrapper);
    }
}
