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
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@TableName("carp_dag_config")
public class DagConfig extends BaseAuditDO {

    private static final long serialVersionUID = 1L;

    @TableField("`type`")
    private String type;

    @Schema(description = "DAG名称")
    @TableField("`name`")
    private String name;

    @TableField("uuid")
    private String uuid;

    @TableField("dag_meta")
    private String dagMeta;

    @TableField("dag_attrs")
    private String dagAttrs;

    @TableField("intput_options")
    private String intputOptions;

    @TableField("output_options")
    private String outputOptions;

    @TableField("version")
    private Integer version;

    @TableField("remark")
    private String remark;
}
