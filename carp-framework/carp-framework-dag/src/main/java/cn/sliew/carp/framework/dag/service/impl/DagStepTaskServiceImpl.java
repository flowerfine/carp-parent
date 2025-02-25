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

import cn.sliew.carp.framework.dag.repository.entity.DagStepTask;
import cn.sliew.carp.framework.dag.repository.mapper.DagStepTaskMapper;
import cn.sliew.carp.framework.dag.service.DagStepTaskService;
import cn.sliew.carp.framework.dag.service.convert.DagStepTaskConvert;
import cn.sliew.carp.framework.dag.service.dto.DagStepTaskDTO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DagStepTaskServiceImpl extends ServiceImpl<DagStepTaskMapper, DagStepTask> implements DagStepTaskService {

    @Override
    public List<DagStepTaskDTO> listTasks(Long dagStepId) {
        LambdaQueryWrapper<DagStepTask> queryWrapper = Wrappers.lambdaQuery(DagStepTask.class)
                .eq(DagStepTask::getDagStepId, dagStepId);
        List<DagStepTask> entities = list(queryWrapper);
        return DagStepTaskConvert.INSTANCE.toDto(entities);
    }

    @Override
    public DagStepTaskDTO get(Long id) {
        DagStepTask entity = getOptById(id).orElseThrow(() -> new IllegalArgumentException("dag step task not exists for id: " + id));
        return DagStepTaskConvert.INSTANCE.toDto(entity);
    }

    @Override
    public boolean add(DagStepTaskDTO param) {
        DagStepTask entity = DagStepTaskConvert.INSTANCE.toDo(param);
        return save(entity);
    }

    @Override
    public boolean update(DagStepTaskDTO param) {
        DagStepTask entity = DagStepTaskConvert.INSTANCE.toDo(param);
        return updateById(entity);
    }
}
