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
package cn.sliew.carp.framework.log.service.dto;

import cn.sliew.carp.framework.common.model.BaseDTO;
import cn.sliew.carp.framework.log.model.LogRequest;
import cn.sliew.carp.framework.log.model.LogResponse;
import cn.sliew.carp.framework.log.model.UserInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(name = "CarpSystemLogAction", description = "system user action log")
public class CarpSystemLogActionDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "链路ID")
    private String traceId;

    @Schema(description = "模块")
    private String module;

    @Schema(description = "描述")
    private String desc;

    @Schema(description = "http 请求")
    private LogRequest request;

    @Schema(description = "http 响应")
    private LogResponse response;

    @Schema(description = "http 用户信息")
    private UserInfo user;

    @Schema(description = "http 请求开始时间")
    private Date startTime;

    @Schema(description = "http 请求结束时间")
    private Date endTime;

    @Schema(description = "http 请求耗时")
    private Long duration;

    @Schema(description = "http 请求状态")
    private Integer status;
}
