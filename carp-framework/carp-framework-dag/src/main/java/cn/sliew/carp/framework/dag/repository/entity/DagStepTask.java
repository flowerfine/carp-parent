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
package cn.sliew.carp.framework.dag.repository.entity;

import cn.sliew.carp.framework.mybatis.entity.BaseAuditDO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("carp_dag_step_task")
public class DagStepTask extends BaseAuditDO {

    private static final long serialVersionUID = 1L;

    @TableField("namespace")
    private String namespace;

    @TableField("dag_instance_id")
    private Long dagInstanceId;

    @TableField("dag_step_id")
    private Long dagStepId;

    @TableField("task_id")
    private Integer taskId;

    @TableField("uuid")
    private String uuid;

    @TableField("`name`")
    private String name;

    @TableField("implementing_class")
    private String implementingClass;

    @TableField("stage_start")
    private boolean stageStart;

    @TableField("stage_end")
    private boolean stageEnd;

    @TableField("loop_start")
    private boolean loopStart;

    @TableField("loop_end")
    private boolean loopEnd;

    @TableField("`status`")
    private String status;

    @TableField("start_time")
    private Date startTime;

    @TableField("end_time")
    private Date endTime;

    @TableField("task_exception_details")
    private String taskExceptionDetails;
}
