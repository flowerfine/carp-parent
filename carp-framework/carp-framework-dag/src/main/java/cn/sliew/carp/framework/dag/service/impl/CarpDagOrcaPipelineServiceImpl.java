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
import cn.sliew.carp.framework.dag.repository.entity.orca.CarpDagOrcaPipeline;
import cn.sliew.carp.framework.dag.repository.entity.orca.CarpDagOrcaPipelineStage;
import cn.sliew.carp.framework.dag.repository.mapper.orca.CarpDagOrcaPipelineMapper;
import cn.sliew.carp.framework.dag.repository.mapper.orca.CarpDagOrcaPipelineStageMapper;
import cn.sliew.carp.framework.dag.service.CarpDagOrcaPipelineService;
import cn.sliew.carp.framework.dag.service.convert.orca.CarpDagOrcaPipelineConvert;
import cn.sliew.carp.framework.dag.service.convert.orca.CarpDagOrcaPipelineStageConvert;
import cn.sliew.carp.framework.dag.service.dto.orca.CarpDagOrcaPipelineDTO;
import cn.sliew.carp.framework.dag.service.dto.orca.CarpDagOrcaPipelineStageDTO;
import cn.sliew.carp.framework.dag.service.param.orca.*;
import cn.sliew.carp.framework.mybatis.DataSourceConstants;
import cn.sliew.carp.framework.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
@Service
public class CarpDagOrcaPipelineServiceImpl implements CarpDagOrcaPipelineService {

    @Autowired
    private CarpDagOrcaPipelineMapper carpDagOrcaPipelineMapper;
    @Autowired
    private CarpDagOrcaPipelineStageMapper carpDagOrcaPipelineStageMapper;

    @Override
    public PageResult<CarpDagOrcaPipelineDTO> page(CarpDagOrcaPipelinePageParam param) {
        Page<CarpDagOrcaPipeline> page = PageUtil.buildPageParam(param);
        LambdaQueryWrapper<CarpDagOrcaPipeline> queryChainWrapper = Wrappers.lambdaQuery(CarpDagOrcaPipeline.class)
                .eq(CarpDagOrcaPipeline::getNamespace, param.getNamespace())
                .eq(StringUtils.hasText(param.getType()), CarpDagOrcaPipeline::getType, param.getType())
                .eq(StringUtils.hasText(param.getConfigId()), CarpDagOrcaPipeline::getConfigId, param.getConfigId())
                .like(StringUtils.hasText(param.getName()), CarpDagOrcaPipeline::getName, param.getName())
                .eq(StringUtils.hasText(param.getStatus()), CarpDagOrcaPipeline::getStatus, param.getStatus());

        Page<CarpDagOrcaPipeline> carpDagOrcaPipelinePage = carpDagOrcaPipelineMapper.selectPage(page, queryChainWrapper);
        PageResult<CarpDagOrcaPipelineDTO> pageResult = new PageResult<>(carpDagOrcaPipelinePage.getCurrent(), carpDagOrcaPipelinePage.getSize(), carpDagOrcaPipelinePage.getTotal());
        pageResult.setRecords(CarpDagOrcaPipelineConvert.INSTANCE.toDto(carpDagOrcaPipelinePage.getRecords()));
        return pageResult;
    }

    @Override
    public List<CarpDagOrcaPipelineDTO> listAll(CarpDagOrcaPipelinePageParam param) {
        LambdaQueryWrapper<CarpDagOrcaPipeline> queryChainWrapper = Wrappers.lambdaQuery(CarpDagOrcaPipeline.class)
                .eq(CarpDagOrcaPipeline::getNamespace, param.getNamespace())
                .eq(StringUtils.hasText(param.getType()), CarpDagOrcaPipeline::getType, param.getType())
                .eq(StringUtils.hasText(param.getConfigId()), CarpDagOrcaPipeline::getConfigId, param.getConfigId())
                .like(StringUtils.hasText(param.getName()), CarpDagOrcaPipeline::getName, param.getName())
                .eq(StringUtils.hasText(param.getStatus()), CarpDagOrcaPipeline::getStatus, param.getStatus());

        List<CarpDagOrcaPipeline> records = carpDagOrcaPipelineMapper.selectList(queryChainWrapper);
        return CarpDagOrcaPipelineConvert.INSTANCE.toDto(records);
    }

    @Override
    public CarpDagOrcaPipelineDTO get(Long id) {
        CarpDagOrcaPipeline entity = carpDagOrcaPipelineMapper.selectById(id);
        checkNotNull(entity, "carp dag orca pipeline not exists for id: " + id);
        return CarpDagOrcaPipelineConvert.INSTANCE.toDto(entity);
    }

    @Override
    public Long add(CarpDagOrcaPipelineAddParam param) {
        CarpDagOrcaPipeline entity = new CarpDagOrcaPipeline();
        BeanUtils.copyProperties(param, entity);
        if (Objects.nonNull(param.getBody())) {
            entity.setBody(param.getBody().toString());
        }
        entity.setStatus("NOT_STARTED");
        entity.setBuildTime(System.currentTimeMillis());
        entity.setCanceled(false);
        carpDagOrcaPipelineMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public boolean update(CarpDagOrcaPipelineUpdateParam param) {
        CarpDagOrcaPipeline entity = new CarpDagOrcaPipeline();
        BeanUtils.copyProperties(param, entity);
        if (Objects.nonNull(param.getBody())) {
            entity.setBody(param.getBody().toString());
        }
        return SqlHelper.retBool(carpDagOrcaPipelineMapper.updateById(entity));
    }

    @Override
    @Transactional(DataSourceConstants.TRANSACTION_MANAGER_FACTORY)
    public boolean delete(Long id) {
        carpDagOrcaPipelineMapper.deleteById(id);
        deleteStageByPipeline(id);
        return true;
    }

    @Override
    @Transactional(DataSourceConstants.TRANSACTION_MANAGER_FACTORY)
    public boolean deleteBatch(Collection<Long> ids) {
        carpDagOrcaPipelineMapper.deleteByIds(ids);
        deleteStageByPipelines(ids);
        return true;
    }

    @Override
    public CarpDagOrcaPipelineStageDTO getStage(Long stageId) {
        CarpDagOrcaPipelineStage entity = carpDagOrcaPipelineStageMapper.selectById(stageId);
        checkNotNull(entity, "carp dag orca pipeline stage not exists for id: " + stageId);
        return CarpDagOrcaPipelineStageConvert.INSTANCE.toDto(entity);
    }

    @Override
    public Long addStage(CarpDagOrcaPipelineStageAddParam param) {
        CarpDagOrcaPipelineStage entity = new CarpDagOrcaPipelineStage();
        BeanUtils.copyProperties(param, entity);
        carpDagOrcaPipelineStageMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public boolean updateStage(CarpDagOrcaPipelineStageUpdateParam param) {
        CarpDagOrcaPipelineStage entity = new CarpDagOrcaPipelineStage();
        BeanUtils.copyProperties(param, entity);
        return SqlHelper.retBool(carpDagOrcaPipelineStageMapper.updateById(entity));
    }

    @Override
    public boolean deleteStage(Long stageId) {
        return SqlHelper.retBool(carpDagOrcaPipelineStageMapper.deleteById(stageId));
    }

    @Override
    public boolean deleteStageByPipeline(Long pipelineId) {
        LambdaUpdateWrapper<CarpDagOrcaPipelineStage> lambdaUpdateWrapper = Wrappers.lambdaUpdate(CarpDagOrcaPipelineStage.class)
                .eq(CarpDagOrcaPipelineStage::getPipelineId, pipelineId);
        return SqlHelper.retBool(carpDagOrcaPipelineStageMapper.delete(lambdaUpdateWrapper));
    }

    @Override
    public boolean deleteStageByPipelines(Collection<Long> pipelineIds) {
        LambdaUpdateWrapper<CarpDagOrcaPipelineStage> lambdaUpdateWrapper = Wrappers.lambdaUpdate(CarpDagOrcaPipelineStage.class)
                .in(CarpDagOrcaPipelineStage::getPipelineId, pipelineIds);
        return SqlHelper.retBool(carpDagOrcaPipelineStageMapper.delete(lambdaUpdateWrapper));
    }
}
