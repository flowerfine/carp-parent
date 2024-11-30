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
import cn.sliew.carp.framework.dag.repository.entity.DagStep;
import cn.sliew.carp.framework.dag.repository.entity.DagStepVO;
import cn.sliew.carp.framework.dag.repository.mapper.DagStepMapper;
import cn.sliew.carp.framework.dag.service.DagStepService;
import cn.sliew.carp.framework.dag.service.convert.DagStepConvert;
import cn.sliew.carp.framework.dag.service.convert.DagStepVOConvert;
import cn.sliew.carp.framework.dag.service.dto.DagStepDTO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class DagStepServiceImpl extends ServiceImpl<DagStepMapper, DagStep> implements DagStepService {

    @Override
    public List<DagStepDTO> listSteps(Long dagInstanceId) {
        List<DagStepVO> dagStepVOS = baseMapper.listByDagInstanceId(dagInstanceId);
        return DagStepVOConvert.INSTANCE.toDto(dagStepVOS);
    }

    @Override
    public DagStepDTO get(Long id) {
        DagStep entity = getOptById(id).orElseThrow(() -> new IllegalArgumentException("dag step not exists for id: " + id));
        return DagStepConvert.INSTANCE.toDto(entity);
    }

    @Override
    public boolean add(DagStepDTO stepDTO) {
        DagStep record = DagStepConvert.INSTANCE.toDo(stepDTO);
        record.setUuid(UUIDUtil.randomUUId());
        return save(record);
    }

    @Override
    public boolean update(DagStepDTO stepDTO) {
        DagStep record = DagStepConvert.INSTANCE.toDo(stepDTO);
        return updateById(record);
    }

    @Override
    public boolean updateStatus(Long id, String fromStatus, String toStatus) {
        LambdaUpdateWrapper<DagStep> wrapper = Wrappers.lambdaUpdate(DagStep.class)
                .eq(DagStep::getId, id)
                .eq(StringUtils.hasText(fromStatus), DagStep::getStatus, fromStatus)
                .set(DagStep::getStatus, toStatus);
        return update(wrapper);
    }
}
