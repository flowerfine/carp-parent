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
package cn.sliew.carp.framework.dag.service.convert.orca;

import cn.sliew.carp.framework.common.convert.BaseConvert;
import cn.sliew.carp.framework.dag.repository.entity.orca.CarpDagOrcaPipeline;
import cn.sliew.carp.framework.dag.service.dto.orca.CarpDagOrcaPipelineDTO;
import cn.sliew.milky.common.util.JacksonUtil;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Objects;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CarpDagOrcaPipelineConvert extends BaseConvert<CarpDagOrcaPipeline, CarpDagOrcaPipelineDTO> {
    CarpDagOrcaPipelineConvert INSTANCE = Mappers.getMapper(CarpDagOrcaPipelineConvert.class);

    @Override
    default CarpDagOrcaPipeline toDo(CarpDagOrcaPipelineDTO dto) {
        CarpDagOrcaPipeline entity = new CarpDagOrcaPipeline();
        BeanUtils.copyProperties(dto, entity);
        if (Objects.nonNull(dto.getBuildTime())) {
            entity.setBuildTime(dto.getBuildTime().toEpochMilli());
        }
        if (Objects.nonNull(dto.getStartTime())) {
            entity.setStartTime(dto.getStartTime().toEpochMilli());
        }
        if (Objects.nonNull(dto.getEndTime())) {
            entity.setEndTime(dto.getEndTime().toEpochMilli());
        }
        if (Objects.nonNull(dto.getBody())) {
            entity.setBody(dto.getBody().toString());
        }
        if (CollectionUtils.isEmpty(dto.getStages()) == false) {
            entity.setStages(CarpDagOrcaPipelineStageConvert.INSTANCE.toDo(dto.getStages()));
        }
        return entity;
    }

    @Override
    default CarpDagOrcaPipelineDTO toDto(CarpDagOrcaPipeline entity) {
        CarpDagOrcaPipelineDTO dto = new CarpDagOrcaPipelineDTO();
        BeanUtils.copyProperties(entity, dto);
        if (Objects.nonNull(entity.getBuildTime())) {
            dto.setBuildTime(Instant.ofEpochMilli(entity.getBuildTime()));
        }
        if (Objects.nonNull(entity.getStartTime())) {
            dto.setStartTime(Instant.ofEpochMilli(entity.getStartTime()));
        }
        if (Objects.nonNull(entity.getEndTime())) {
            dto.setEndTime(Instant.ofEpochMilli(entity.getEndTime()));
        }
        if (StringUtils.hasText(entity.getBody())) {
            dto.setBody(JacksonUtil.toJsonNode(entity.getBody()));
        }
        if (CollectionUtils.isEmpty(entity.getStages()) == false) {
            dto.setStages(CarpDagOrcaPipelineStageConvert.INSTANCE.toDto(entity.getStages()));
        }
        return dto;
    }
}
