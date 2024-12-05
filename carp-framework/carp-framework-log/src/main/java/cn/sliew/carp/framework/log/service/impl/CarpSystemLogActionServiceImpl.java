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
package cn.sliew.carp.framework.log.service.impl;

import cn.sliew.carp.framework.common.model.PageResult;
import cn.sliew.carp.framework.log.repository.entity.CarpSystemLogAction;
import cn.sliew.carp.framework.log.repository.mapper.CarpSystemLogActionMapper;
import cn.sliew.carp.framework.log.service.CarpSystemLogActionService;
import cn.sliew.carp.framework.log.service.convert.CarpSystemLogActionConvert;
import cn.sliew.carp.framework.log.service.dto.CarpSystemLogActionDTO;
import cn.sliew.carp.framework.log.service.param.CarpSystemLogActionPageParam;
import cn.sliew.carp.framework.mybatis.DataSourceConstants;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collection;

@Service
public class CarpSystemLogActionServiceImpl extends ServiceImpl<CarpSystemLogActionMapper, CarpSystemLogAction> implements CarpSystemLogActionService {

    @Override
    public PageResult<CarpSystemLogActionDTO> page(CarpSystemLogActionPageParam param) {
        Page<CarpSystemLogAction> page = new Page<>(param.getCurrent(), param.getPageSize());
        LambdaQueryWrapper<CarpSystemLogAction> queryChainWrapper = Wrappers.lambdaQuery(CarpSystemLogAction.class)
                .gt(CarpSystemLogAction::getStartTime, param.getStartTime())
                .eq(StringUtils.hasText(param.getModule()), CarpSystemLogAction::getModule, param.getModule())
                .eq(param.getStatus() != null, CarpSystemLogAction::getStatus, param.getStatus())
                .orderByAsc(CarpSystemLogAction::getStartTime, CarpSystemLogAction::getId);

        Page<CarpSystemLogAction> secRolePage = page(page, queryChainWrapper);
        PageResult<CarpSystemLogActionDTO> pageResult = new PageResult<>(secRolePage.getCurrent(), secRolePage.getSize(), secRolePage.getTotal());
        pageResult.setRecords(CarpSystemLogActionConvert.INSTANCE.toDto(secRolePage.getRecords()));
        return pageResult;
    }

    @Override
    public boolean add(CarpSystemLogActionDTO param) {
        CarpSystemLogAction entity = CarpSystemLogActionConvert.INSTANCE.toDo(param);
        return save(entity);
    }

    @Override
    public boolean delete(Long id) {
        return removeById(id);
    }

    @Transactional(rollbackFor = {Exception.class}, transactionManager = DataSourceConstants.TRANSACTION_MANAGER_FACTORY)
    @Override
    public boolean deleteBatch(Collection<Long> ids) {
        return removeByIds(ids);
    }
}
