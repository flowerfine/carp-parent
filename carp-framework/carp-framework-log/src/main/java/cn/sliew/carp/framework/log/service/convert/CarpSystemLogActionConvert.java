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
package cn.sliew.carp.framework.log.service.convert;

import cn.sliew.carp.framework.common.convert.BaseConvert;
import cn.sliew.carp.framework.log.model.LogRequest;
import cn.sliew.carp.framework.log.model.LogResponse;
import cn.sliew.carp.framework.log.model.UserInfo;
import cn.sliew.carp.framework.log.repository.entity.CarpSystemLogAction;
import cn.sliew.carp.framework.log.service.dto.CarpSystemLogActionDTO;
import cn.sliew.milky.common.util.JacksonUtil;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CarpSystemLogActionConvert extends BaseConvert<CarpSystemLogAction, CarpSystemLogActionDTO> {
    CarpSystemLogActionConvert INSTANCE = Mappers.getMapper(CarpSystemLogActionConvert.class);

    @Override
    default CarpSystemLogAction toDo(CarpSystemLogActionDTO dto) {
        CarpSystemLogAction entity = new CarpSystemLogAction();
        BeanUtils.copyProperties(dto, entity);
        if (Objects.nonNull(dto.getRequest())) {
            entity.setRequest(JacksonUtil.toJsonString(dto.getRequest()));
        }
        if (Objects.nonNull(dto.getResponse())) {
            entity.setResponse(JacksonUtil.toJsonString(dto.getResponse()));
        }
        if (Objects.nonNull(dto.getUser())) {
            entity.setUser(JacksonUtil.toJsonString(dto.getUser()));
        }
        return entity;
    }

    @Override
    default CarpSystemLogActionDTO toDto(CarpSystemLogAction entity) {
        CarpSystemLogActionDTO dto = new CarpSystemLogActionDTO();
        BeanUtils.copyProperties(entity, dto);
        if (StringUtils.hasText(entity.getRequest())) {
            dto.setRequest(JacksonUtil.parseJsonString(entity.getRequest(), LogRequest.class));
        }
        if (StringUtils.hasText(entity.getResponse())) {
            dto.setResponse(JacksonUtil.parseJsonString(entity.getResponse(), LogResponse.class));
        }
        if (StringUtils.hasText(entity.getUser())) {
            dto.setUser(JacksonUtil.parseJsonString(entity.getUser(), UserInfo.class));
        }
        return dto;
    }
}
