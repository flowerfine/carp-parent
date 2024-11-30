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

/**
 * DAG 配置连线
 */
@Data
@TableName("carp_dag_config_link")
public class DagConfigLink extends BaseAuditDO {

    private static final long serialVersionUID = 1L;

    @TableField("dag_id")
    private Long dagId;

    @TableField("link_id")
    private String linkId;

    @TableField("link_name")
    private String linkName;

    @TableField("from_step_id")
    private String fromStepId;

    @TableField("to_step_id")
    private String toStepId;

    @TableField("shape")
    private String shape;

    @TableField("`style`")
    private String style;

    @TableField("link_meta")
    private String linkMeta;

    @TableField("link_attrs")
    private String linkAttrs;
}
