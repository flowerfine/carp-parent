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

import cn.sliew.carp.framework.dag.repository.entity.DagConfigStep;
import cn.sliew.carp.framework.dag.repository.mapper.DagConfigStepMapper;
import cn.sliew.carp.framework.dag.service.DagConfigStepService;
import cn.sliew.carp.framework.dag.service.convert.DagConfigStepConvert;
import cn.sliew.carp.framework.dag.service.dto.DagConfigStepDTO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class DagConfigStepServiceImpl extends ServiceImpl<DagConfigStepMapper, DagConfigStep> implements DagConfigStepService {

    @Override
    public List<DagConfigStepDTO> listSteps(Long dagId) {
        LambdaQueryWrapper<DagConfigStep> queryWrapper = Wrappers.lambdaQuery(DagConfigStep.class)
                .eq(DagConfigStep::getDagId, dagId);
        List<DagConfigStep> dagConfigSteps = list(queryWrapper);
        return DagConfigStepConvert.INSTANCE.toDto(dagConfigSteps);
    }

    @Override
    public DagConfigStepDTO get(Long id) {
        DagConfigStep entity = getOptById(id).orElseThrow(() -> new IllegalArgumentException("dag config step not exists for id: " + id));
        return DagConfigStepConvert.INSTANCE.toDto(entity);
    }

    @Override
    public boolean add(DagConfigStepDTO stepDTO) {
        DagConfigStep record = DagConfigStepConvert.INSTANCE.toDo(stepDTO);
        return save(record);
    }

    @Override
    public boolean update(DagConfigStepDTO stepDTO) {
        LambdaUpdateWrapper<DagConfigStep> updateWrapper = Wrappers.lambdaUpdate(DagConfigStep.class)
                .eq(DagConfigStep::getDagId, stepDTO.getDagId())
                .eq(DagConfigStep::getStepId, stepDTO.getStepId());
        DagConfigStep record = DagConfigStepConvert.INSTANCE.toDo(stepDTO);
        return update(record, updateWrapper);
    }

    @Override
    public void upsert(DagConfigStepDTO stepDTO) {
        LambdaQueryWrapper<DagConfigStep> queryWrapper = Wrappers.lambdaQuery(DagConfigStep.class)
                .eq(DagConfigStep::getDagId, stepDTO.getDagId())
                .eq(DagConfigStep::getStepId, stepDTO.getStepId());
        if (exists(queryWrapper)) {
            update(stepDTO);
        } else {
            add(stepDTO);
        }
    }

    @Override
    public boolean deleteByDag(Long dagId) {
        LambdaUpdateWrapper<DagConfigStep> updateWrapper = Wrappers.lambdaUpdate(DagConfigStep.class)
                .eq(DagConfigStep::getDagId, dagId);
        return remove(updateWrapper);
    }

    @Override
    public boolean deleteByDag(List<Long> dagIds) {
        LambdaUpdateWrapper<DagConfigStep> updateWrapper = Wrappers.lambdaUpdate(DagConfigStep.class)
                .in(DagConfigStep::getDagId, dagIds);
        return remove(updateWrapper);
    }

    @Override
    public boolean deleteSurplusSteps(Long dagId, List<String> stepIds) {
        LambdaUpdateWrapper<DagConfigStep> updateWrapper = Wrappers.lambdaUpdate(DagConfigStep.class)
                .eq(DagConfigStep::getDagId, dagId)
                .notIn(CollectionUtils.isEmpty(stepIds) == false, DagConfigStep::getStepId, stepIds);
        return remove(updateWrapper);
    }

    @Override
    public int clone(Long sourceDagId, Long targetDagId) {
        List<DagConfigStepDTO> sourceSteps = listSteps(sourceDagId);
        sourceSteps.forEach(stepDTO -> {
            stepDTO.setDagId(targetDagId);
            stepDTO.setId(null);
            stepDTO.setCreator(null);
            stepDTO.setCreateTime(null);
            stepDTO.setEditor(null);
            stepDTO.setUpdateTime(null);
            add(stepDTO);
        });
        return sourceSteps.size();
    }
}
