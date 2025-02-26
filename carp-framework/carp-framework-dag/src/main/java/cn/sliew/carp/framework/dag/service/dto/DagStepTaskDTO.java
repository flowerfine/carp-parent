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
package cn.sliew.carp.framework.dag.service.dto;

import cn.sliew.carp.framework.mybatis.entity.BaseAuditDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
@Schema(name = "CarpDagStepTask", description = "dag instance step task")
public class DagStepTaskDTO extends BaseAuditDO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "namespace")
    private String namespace;

    @Schema(description = "DAG id")
    private Long dagInstanceId;

    @Schema(description = "step id")
    private Long dagStepId;

    @Schema(description = "task id")
    private Long taskId;

    @Schema(description = "task id")
    private String uuid;

    @Schema(description = "task name")
    private String name;

    @Schema(description = "task implementing class name")
    private String implementingClass;

    @Schema(description = "stage task start ? 1 : 0")
    private boolean stageStart;

    @Schema(description = "stage end ? 1 : 0")
    private boolean stageEnd;

    @Schema(description = "loop task start ? 1 : 0")
    private boolean loopStart;

    @Schema(description = "loop end ? 1 : 0")
    private boolean loopEnd;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "启动时间")
    private Date startTime;

    @Schema(description = "结束时间")
    private Date endTime;

    private Map<String, Object> taskExceptionDetails;
}
