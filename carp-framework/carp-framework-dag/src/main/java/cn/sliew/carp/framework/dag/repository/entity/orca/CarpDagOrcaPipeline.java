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
package cn.sliew.carp.framework.dag.repository.entity.orca;

import cn.sliew.carp.framework.mybatis.entity.BaseAuditDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

/**
 * dag orca pipeline
 */
@Data
@TableName(value = "carp_dag_orca_pipeline", resultMap = "BaseResultMap")
public class CarpDagOrcaPipeline extends BaseAuditDO {

    private static final long serialVersionUID = 1L;

    @TableField("namespace")
    private String namespace;

    @TableField("`name`")
    private String name;

    @TableField("`status`")
    private String status;

    @TableField("build_time")
    private Long buildTime;

    @TableField("start_time")
    private Long startTime;

    @TableField("end_time")
    private Long endTime;

    @TableField("canceled")
    private Boolean canceled;

    @TableField("body")
    private String body;

    @TableField("remark")
    private String remark;

    @TableField(value = "stages", exist = false)
    private List<CarpDagOrcaPipelineStage> stages;
}
