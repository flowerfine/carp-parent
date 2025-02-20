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

import cn.sliew.carp.framework.common.model.BaseDTO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(name = "DagLink", description = "DAG 连线")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class DagLinkDTO extends BaseDTO {

    @Schema(description = "namespace")
    private String namespace;

    @Schema(description = "DAG 实例")
    private DagInstanceDTO dagInstance;

    @Schema(description = "连线id")
    private DagConfigLinkDTO dagConfigLink;

    @Schema(description = "instance id")
    private String uuid;

    @Schema(description = "body")
    private JsonNode body;

    @Schema(description = "输入参数")
    private JsonNode inputs;

    @Schema(description = "输出参数")
    private JsonNode outputs;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "启动时间")
    private Date startTime;

    @Schema(description = "结束时间")
    private Date endTime;
}
