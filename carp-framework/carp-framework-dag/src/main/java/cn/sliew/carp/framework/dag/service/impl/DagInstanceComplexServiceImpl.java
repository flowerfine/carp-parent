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
import cn.sliew.carp.framework.dag.algorithm.DAG;
import cn.sliew.carp.framework.dag.algorithm.DagUtil;
import cn.sliew.carp.framework.dag.service.*;
import cn.sliew.carp.framework.dag.service.dto.*;
import cn.sliew.carp.framework.dag.service.param.DagInstanceSimplePageParam;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class DagInstanceComplexServiceImpl implements DagInstanceComplexService {

    @Autowired
    private DagConfigComplexService dagConfigComplexService;
    @Autowired
    private DagInstanceService dagInstanceService;
    @Autowired
    private DagLinkService dagLinkService;
    @Autowired
    private DagStepService dagStepService;

    @Override
    public PageResult<DagInstanceDTO> page(DagInstanceSimplePageParam param) {
        return dagInstanceService.page(param);
    }

    @Override
    public DagInstanceComplexDTO selectOne(Long dagInstanceId) {
        DagInstanceComplexDTO dagInstanceComplexDTO = new DagInstanceComplexDTO();
        DagInstanceDTO instanceDTO = dagInstanceService.getWithConfig(dagInstanceId);
        BeanUtils.copyProperties(instanceDTO, dagInstanceComplexDTO);
        List<DagLinkDTO> links = dagLinkService.listLinks(dagInstanceId);
        if (CollectionUtils.isEmpty(links) == false) {
            links.forEach(link -> link.setDagInstance(instanceDTO));
        }
        dagInstanceComplexDTO.setLinks(links);
        List<DagStepDTO> steps = dagStepService.listSteps(dagInstanceId);
        if (CollectionUtils.isEmpty(steps) == false) {
            steps.forEach(step -> step.setDagInstance(instanceDTO));
        }
        dagInstanceComplexDTO.setSteps(steps);
        return dagInstanceComplexDTO;
    }

    @Override
    public DagInstanceDTO selectSimpleOne(Long dagInstanceId) {
        return dagInstanceService.get(dagInstanceId);
    }

    @Override
    public Graph<DagStepDTO> getDag(Long dagInstanceId, Graph<DagConfigStepDTO> configDag) {
        DAG<DagStepDTO> dag = getDagNew(dagInstanceId);
        MutableGraph<DagStepDTO> graph = GraphBuilder.directed().build();
        dag.nodes().forEach(graph::addNode);
        dag.edges().forEach(edge -> graph.putEdge(edge.getSource(), edge.getTarget()));
        return graph;
    }

    @Override
    public DAG<DagStepDTO> getDagNew(Long dagInstanceId) {
        return DagUtil.buildDag(selectOne(dagInstanceId));
    }

    @Override
    public Long initialize(Long dagConfigId) {
        DagConfigComplexDTO dagConfigComplexDTO = dagConfigComplexService.selectOne(dagConfigId);
        List<DagConfigStepDTO> steps = dagConfigComplexDTO.getSteps();
        List<DagConfigLinkDTO> links = dagConfigComplexDTO.getLinks();
        dagConfigComplexDTO.setSteps(null);
        dagConfigComplexDTO.setLinks(null);

        // 插入 dag_instance
        DagInstanceDTO dagInstanceDTO = new DagInstanceDTO();
        dagInstanceDTO.setNamespace(dagConfigComplexDTO.getNamespace());
        dagInstanceDTO.setDagConfig(dagConfigComplexDTO);
        dagInstanceDTO.setUuid(UUIDUtil.randomUUId());
        Long dagInstanceId = dagInstanceService.add(dagInstanceDTO);
        dagInstanceDTO.setId(dagInstanceId);
        // 插入 dag_step
        if (CollectionUtils.isEmpty(steps) == false) {
            for (DagConfigStepDTO dagConfigStepDTO : steps) {
                DagStepDTO dagStepDTO = new DagStepDTO();
                dagStepDTO.setNamespace(dagConfigComplexDTO.getNamespace());
                dagStepDTO.setDagInstance(dagInstanceDTO);
                dagStepDTO.setDagConfigStep(dagConfigStepDTO);
                dagStepDTO.setUuid(UUIDUtil.randomUUId());
                dagStepService.add(dagStepDTO);
            }
        }
        // 插入 dag_link
        if (CollectionUtils.isEmpty(links) == false) {
            for (DagConfigLinkDTO dagConfigLinkDTO : links) {
                DagLinkDTO dagLinkDTO = new DagLinkDTO();
                dagLinkDTO.setNamespace(dagConfigComplexDTO.getNamespace());
                dagLinkDTO.setDagInstance(dagInstanceDTO);
                dagLinkDTO.setDagConfigLink(dagConfigLinkDTO);
                dagLinkDTO.setUuid(UUIDUtil.randomUUId());
//                dagLinkDTO.setInputs(dagConfigLinkDTO.getLinkAttrs());
                dagLinkService.add(dagLinkDTO);
            }
        }
        return dagInstanceId;
    }
}
