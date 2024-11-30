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
import cn.sliew.carp.framework.dag.repository.entity.DagLink;
import cn.sliew.carp.framework.dag.repository.entity.DagLinkVO;
import cn.sliew.carp.framework.dag.repository.mapper.DagLinkMapper;
import cn.sliew.carp.framework.dag.service.DagLinkService;
import cn.sliew.carp.framework.dag.service.convert.DagLinkConvert;
import cn.sliew.carp.framework.dag.service.convert.DagLinkVOConvert;
import cn.sliew.carp.framework.dag.service.dto.DagLinkDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DagLinkServiceImpl extends ServiceImpl<DagLinkMapper, DagLink> implements DagLinkService {

    @Override
    public List<DagLinkDTO> listLinks(Long dagInstanceId) {
        List<DagLinkVO> dagLinkVOS = baseMapper.listByDagInstanceId(dagInstanceId);
        return DagLinkVOConvert.INSTANCE.toDto(dagLinkVOS);
    }

    @Override
    public boolean add(DagLinkDTO linkDTO) {
        DagLink record = DagLinkConvert.INSTANCE.toDo(linkDTO);
        record.setUuid(UUIDUtil.randomUUId());
        return save(record);
    }

    @Override
    public boolean update(DagLinkDTO linkDTO) {
        DagLink record = DagLinkConvert.INSTANCE.toDo(linkDTO);
        return updateById(record);
    }
}
